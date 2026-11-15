package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.application.dto.BankStatementDTO;
import com.erp.brazil.finance.application.dto.BankStatementLineDTO;
import com.erp.brazil.finance.domain.model.*;
import com.erp.brazil.finance.domain.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class ProcessBankStatementUseCase {

    private final BankAccountRepository bankAccountRepository;
    private final BankStatementRepository bankStatementRepository;
    private final BankStatementLineRepository bankStatementLineRepository;
    private final GLEntryRepository glEntryRepository;
    private final ReconciliationRepository reconciliationRepository;

    public ProcessBankStatementUseCase(BankAccountRepository bankAccountRepository,
                                       BankStatementRepository bankStatementRepository,
                                       BankStatementLineRepository bankStatementLineRepository,
                                       GLEntryRepository glEntryRepository,
                                       ReconciliationRepository reconciliationRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankStatementRepository = bankStatementRepository;
        this.bankStatementLineRepository = bankStatementLineRepository;
        this.glEntryRepository = glEntryRepository;
        this.reconciliationRepository = reconciliationRepository;
    }

    public BankStatementDTO execute(UUID bankAccountId, MultipartFile file, String importedBy) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found: " + bankAccountId));

        String contentType = file.getContentType();
        List<BankStatementLine> lines;

        if (contentType != null && contentType.contains("ofx")) {
            lines = parseOFX(file, bankAccount);
        } else {
            lines = parseCNAB(file, bankAccount);
        }

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("No valid transactions found in file");
        }

        LocalDate periodStart = lines.stream()
                .map(BankStatementLine::getTransactionDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate periodEnd = lines.stream()
                .map(BankStatementLine::getTransactionDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        if (bankStatementRepository.existsByBankAccountAndPeriodStartAndPeriodEnd(bankAccount, periodStart, periodEnd)) {
            throw new IllegalStateException("Bank statement already exists for this period");
        }

        BigDecimal initialBalance = bankAccount.getCurrentBalance();
        BigDecimal finalBalance = calculateFinalBalance(initialBalance, lines);

        BankStatement statement = new BankStatement(bankAccount, periodStart, periodEnd, initialBalance, finalBalance);
        statement.setFilePath(file.getOriginalFilename());
        statement.setStatus(BankStatementStatus.IMPORTED);
        statement.setImportedAt(LocalDateTime.now());
        statement.setImportedBy(importedBy);

        bankStatementRepository.save(statement);

        for (BankStatementLine line : lines) {
            line.setStatement(statement);
            bankStatementLineRepository.save(line);
        }

        autoMatch(statement);

        bankAccount.setCurrentBalance(finalBalance);
        bankAccountRepository.save(bankAccount);

        statement.setStatus(BankStatementStatus.PARTIALLY_MATCHED);
        bankStatementRepository.save(statement);

        return toDTO(statement);
    }

    private List<BankStatementLine> parseCNAB(MultipartFile file, BankAccount bankAccount) {
        List<BankStatementLine> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() < 400) continue;

                BankStatementLine parsed = parseCNABLine(line, bankAccount);
                if (parsed != null) {
                    lines.add(parsed);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing CNAB file", e);
        }
        return lines;
    }

    private BankStatementLine parseCNABLine(String line, BankAccount bankAccount) {
        if (line.length() < 240) return null;

        try {
            String recordType = line.substring(0, 1);
            if (!"1".equals(recordType) && !"3".equals(recordType)) return null;

            String cnabCode = line.substring(1, 3).trim();
            LocalDate transactionDate = LocalDate.parse(line.substring(3, 11), DateTimeFormatter.BASIC_ISO_DATE);
            LocalDate valueDate = LocalDate.parse(line.substring(11, 19), DateTimeFormatter.BASIC_ISO_DATE);
            String description = line.substring(19, 49).trim();
            BigDecimal amount = new BigDecimal(line.substring(49, 67)).movePointLeft(2);
            char dcIndicator = line.charAt(67);
            TransactionType type = dcIndicator == 'C' ? TransactionType.CREDIT : TransactionType.DEBIT;
            String documentNumber = line.substring(68, 98).trim();
            String payerPayee = line.substring(98, 138).trim();

            BankStatementLine stmtLine = new BankStatementLine(null, transactionDate, valueDate, description, amount, type);
            stmtLine.setCnabCode(cnabCode);
            stmtLine.setDocumentNumber(documentNumber);
            stmtLine.setPayerPayee(payerPayee);
            return stmtLine;
        } catch (Exception e) {
            return null;
        }
    }

    private List<BankStatementLine> parseOFX(MultipartFile file, BankAccount bankAccount) {
        List<BankStatementLine> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            lines.addAll(parseOFXContent(content.toString()));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing OFX file", e);
        }
        return lines;
    }

    private List<BankStatementLine> parseOFXContent(String content) {
        List<BankStatementLine> lines = new ArrayList<>();
        Pattern pattern = Pattern.compile("<STMTTRN>.*?</STMTTRN>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String txn = matcher.group();
            try {
                String trntype = extractTag(txn, "TRNTYPE");
                LocalDate dtposted = LocalDate.parse(extractTag(txn, "DTPOSTED").substring(0, 8), DateTimeFormatter.BASIC_ISO_DATE);
                BigDecimal trnamt = new BigDecimal(extractTag(txn, "TRNAMT"));
                String memo = extractTag(txn, "MEMO");
                String fitid = extractTag(txn, "FITID");
                String name = extractTag(txn, "NAME");

                TransactionType type = "CREDIT".equalsIgnoreCase(trntype) || trnamt.compareTo(BigDecimal.ZERO) > 0
                        ? TransactionType.CREDIT : TransactionType.DEBIT;

                BankStatementLine stmtLine = new BankStatementLine(null, dtposted, dtposted, memo, trnamt.abs(), type);
                stmtLine.setDocumentNumber(fitid);
                stmtLine.setPayerPayee(name);
                lines.add(stmtLine);
            } catch (Exception e) {
            }
        }
        return lines;
    }

    private String extractTag(String content, String tag) {
        Pattern pattern = Pattern.compile("<" + tag + ">(.*?)</" + tag + ">");
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private BigDecimal calculateFinalBalance(BigDecimal initialBalance, List<BankStatementLine> lines) {
        BigDecimal balance = initialBalance;
        for (BankStatementLine line : lines) {
            if (line.getType() == TransactionType.CREDIT) {
                balance = balance.add(line.getAmount());
            } else {
                balance = balance.subtract(line.getAmount());
            }
        }
        return balance;
    }

    private void autoMatch(BankStatement statement) {
        for (BankStatementLine line : statement.getLines()) {
            if (line.getMatchedEntry() != null) continue;

            List<GLEntry> candidates = glEntryRepository.findByFiscalPeriodAndEntryDateBetweenOrderByEntryDateAsc(
                    statement.getBankAccount().getChartOfAccount().getFiscalYear().getPeriods().get(0).getFiscalPeriod(), // simplified
                    line.getTransactionDate(),
                    line.getTransactionDate()
            );

            for (GLEntry candidate : candidates) {
                if (candidate.getAmount().compareTo(line.getAmount()) == 0 &&
                        candidate.getEntryType() == (line.getType() == TransactionType.CREDIT ? EntryType.CREDIT : EntryType.DEBIT)) {

                    Reconciliation reconciliation = new Reconciliation(
                            statement.getBankAccount().getCompanyId(),
                            statement.getBankAccount(),
                            line,
                            candidate,
                            "AUTO"
                    );
                    reconciliationRepository.save(reconciliation);

                    line.setMatchedEntry(candidate);
                    line.setMatchedAt(LocalDateTime.now());
                    line.setMatchedBy("AUTO");
                    bankStatementLineRepository.save(line);
                    break;
                }
            }
        }
    }

    private BankStatementDTO toDTO(BankStatement statement) {
        return new BankStatementDTO(
                statement.getId(),
                statement.getBankAccount().getId(),
                statement.getPeriodStart(),
                statement.getPeriodEnd(),
                statement.getInitialBalance(),
                statement.getFinalBalance(),
                statement.getFilePath(),
                statement.getStatus(),
                statement.getImportedAt(),
                statement.getImportedBy()
        );
    }
}