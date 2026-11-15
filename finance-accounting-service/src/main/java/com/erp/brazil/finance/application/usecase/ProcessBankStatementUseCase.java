package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.domain.model.BankAccount;
import com.erp.brazil.finance.domain.model.BankStatement;
import com.erp.brazil.finance.domain.model.BankStatementLine;
import com.erp.brazil.finance.domain.model.BankStatementStatus;
import com.erp.brazil.finance.domain.model.TransactionType;
import com.erp.brazil.finance.domain.repository.BankAccountRepository;
import com.erp.brazil.finance.domain.repository.BankStatementRepository;
import com.erp.brazil.finance.domain.repository.BankStatementLineRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProcessBankStatementUseCase {

    private final BankAccountRepository bankAccountRepository;
    private final BankStatementRepository bankStatementRepository;
    private final BankStatementLineRepository bankStatementLineRepository;
    private final BankStatementParser parser;

    @Inject
    public ProcessBankStatementUseCase(BankAccountRepository bankAccountRepository,
                                       BankStatementRepository bankStatementRepository,
                                       BankStatementLineRepository bankStatementLineRepository,
                                       BankStatementParser parser) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankStatementRepository = bankStatementRepository;
        this.bankStatementLineRepository = bankStatementLineRepository;
        this.parser = parser;
    }

    @Transactional
    public Result importStatement(UUID bankAccountId, String fileContent, String fileName, String importedBy) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found: " + bankAccountId));

        // Parse the bank statement file
        ParsedStatement parsed = parser.parse(fileContent, fileName);

        // Check for duplicate
        if (bankStatementRepository.findByBankAccountAndPeriodStart(bankAccount, parsed.periodStart()).isPresent()) {
            return Result.failure("Bank statement already exists for this period: " + parsed.periodStart());
        }

        // Create bank statement
        BankStatement statement = new BankStatement(
                bankAccount,
                parsed.periodStart(),
                parsed.periodEnd(),
                parsed.openingBalance(),
                parsed.closingBalance()
        );
        statement.setFilePath(fileName);
        statement.setStatus(BankStatementStatus.IMPORTED);
        statement.setImportedAt(LocalDateTime.now());
        statement.setImportedBy(importedBy);
        statement = bankStatementRepository.save(statement);

        // Create lines
        for (ParsedLine parsedLine : parsed.lines()) {
            BankStatementLine line = new BankStatementLine(
                    statement,
                    parsedLine.transactionDate(),
                    parsedLine.valueDate(),
                    parsedLine.description(),
                    parsedLine.amount(),
                    parsedLine.transactionType()
            );
            line.setCnabCode(parsedLine.cnabCode());
            line.setDocumentNumber(parsedLine.documentNumber());
            line.setPayerPayee(parsedLine.payerPayee());
            bankStatementLineRepository.save(line);
        }

        // Validate totals
        validateTotals(statement, parsed);

        return Result.success(statement);
    }

    private void validateTotals(BankStatement statement, ParsedStatement parsed) {
        java.math.BigDecimal calculatedClosing = parsed.openingBalance()
                .add(parsed.totalCredits())
                .subtract(parsed.totalDebits());

        if (calculatedClosing.compareTo(parsed.closingBalance()) != 0) {
            statement.setStatus(BankStatementStatus.ERROR); // Mark for review
            throw new IllegalStateException("Statement totals don't match: Calculated=" + calculatedClosing + ", Provided=" + parsed.closingBalance());
        }
    }

    @Transactional
    public Result autoReconcile(UUID statementId, String reconciledBy) {
        BankStatement statement = bankStatementRepository.findById(statementId)
                .orElseThrow(() -> new IllegalArgumentException("Bank statement not found: " + statementId));

        if (statement.getStatus() != BankStatementStatus.IMPORTED && statement.getStatus() != BankStatementStatus.PROCESSING) {
            return Result.failure("Statement cannot be reconciled in current status: " + statement.getStatus());
        }

        statement.setStatus(BankStatementStatus.PROCESSING);
        bankStatementRepository.save(statement);

        // Auto-match logic would go here
        // For now, just mark as reconciled
        statement.setStatus(BankStatementStatus.RECONCILED);
        statement = bankStatementRepository.save(statement);

        return Result.success(statement);
    }

    public sealed interface Result permits Result.Success, Result.Failure {
        boolean isSuccess();
        static Success success(BankStatement statement) { return new Success(statement); }
        static Failure failure(String message) { return new Failure(message); }

        record Success(BankStatement statement) implements Result {
            @Override public boolean isSuccess() { return true; }
        }
        record Failure(String message) implements Result {
            @Override public boolean isSuccess() { return false; }
        }
    }

    // Parsed data records
    record ParsedStatement(
            LocalDate periodStart,
            LocalDate periodEnd,
            java.math.BigDecimal openingBalance,
            java.math.BigDecimal closingBalance,
            java.math.BigDecimal totalDebits,
            java.math.BigDecimal totalCredits,
            List<ParsedLine> lines
    ) {}

    record ParsedLine(
            LocalDate transactionDate,
            LocalDate valueDate,
            String description,
            java.math.BigDecimal amount,
            TransactionType transactionType,
            String cnabCode,
            String documentNumber,
            String payerPayee
    ) {}

    interface BankStatementParser {
        ParsedStatement parse(String content, String fileName);
    }
}