package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.application.dto.CreateJournalEntryRequest;
import com.erp.brazil.finance.application.dto.JournalEntryDTO;
import com.erp.brazil.finance.application.event.JournalEntryPostedEvent;
import com.erp.brazil.finance.domain.model.*;
import com.erp.brazil.finance.domain.repository.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostJournalEntryUseCase {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final GLEntryRepository glEntryRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final CostCenterRepository costCenterRepository;
    private final RabbitTemplate rabbitTemplate;

    public PostJournalEntryUseCase(JournalEntryRepository journalEntryRepository,
                                   JournalEntryLineRepository journalEntryLineRepository,
                                   GLEntryRepository glEntryRepository,
                                   FiscalPeriodRepository fiscalPeriodRepository,
                                   ChartOfAccountsRepository chartOfAccountsRepository,
                                   CostCenterRepository costCenterRepository,
                                   RabbitTemplate rabbitTemplate) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.glEntryRepository = glEntryRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.chartOfAccountsRepository = chartOfAccountsRepository;
        this.costCenterRepository = costCenterRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public JournalEntryDTO execute(CreateJournalEntryRequest request, String postedBy) {
        FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(request.fiscalPeriodId())
                .orElseThrow(() -> new IllegalArgumentException("Fiscal period not found: " + request.fiscalPeriodId()));

        if (fiscalPeriod.isLocked()) {
            throw new IllegalStateException("Cannot post journal entry to locked fiscal period");
        }
        if (fiscalPeriod.isClosed()) {
            throw new IllegalStateException("Cannot post journal entry to closed fiscal period");
        }

        validateDoubleEntry(request.lines());

        JournalEntry journalEntry = new JournalEntry(
                request.reference(),
                request.entryDate(),
                fiscalPeriod,
                request.description()
        );

        List<JournalEntryLine> lines = new ArrayList<>();
        List<GLEntry> glEntries = new ArrayList<>();

        for (CreateJournalEntryRequest.CreateJournalEntryLineRequest lineRequest : request.lines()) {
            ChartOfAccounts account = chartOfAccountsRepository.findById(lineRequest.accountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found: " + lineRequest.accountId()));

            CostCenter costCenter = null;
            if (lineRequest.costCenterId() != null) {
                costCenter = costCenterRepository.findById(lineRequest.costCenterId())
                        .orElseThrow(() -> new IllegalArgumentException("Cost center not found: " + lineRequest.costCenterId()));
            }

            JournalEntryLine line = new JournalEntryLine(
                    journalEntry,
                    account,
                    lineRequest.lineNumber(),
                    lineRequest.debitAmount(),
                    lineRequest.creditAmount()
            );
            line.setDescription(lineRequest.description());
            line.setCostCenter(costCenter);
            lines.add(line);

            if (lineRequest.debitAmount().compareTo(BigDecimal.ZERO) > 0) {
                GLEntry glEntry = new GLEntry(
                        journalEntry,
                        account,
                        fiscalPeriod,
                        request.entryDate(),
                        EntryType.DEBIT,
                        lineRequest.debitAmount()
                );
                glEntry.setCostCenter(costCenter);
                glEntry.setDescription(lineRequest.description());
                glEntries.add(glEntry);
            }

            if (lineRequest.creditAmount().compareTo(BigDecimal.ZERO) > 0) {
                GLEntry glEntry = new GLEntry(
                        journalEntry,
                        account,
                        fiscalPeriod,
                        request.entryDate(),
                        EntryType.CREDIT,
                        lineRequest.creditAmount()
                );
                glEntry.setCostCenter(costCenter);
                glEntry.setDescription(lineRequest.description());
                glEntries.add(glEntry);
            }
        }

        journalEntry.setLines(lines);
        journalEntry.setGlEntries(glEntries);
        journalEntry.setStatus(JournalEntryStatus.POSTED);
        journalEntry.setPostedAt(LocalDateTime.now());
        journalEntry.setPostedBy(postedBy);

        journalEntryRepository.save(journalEntry);

        calculateRunningBalances(glEntries);

        publishEvent(journalEntry, postedBy);

        return toDTO(journalEntry);
    }

    private void validateDoubleEntry(List<CreateJournalEntryRequest.CreateJournalEntryLineRequest> lines) {
        BigDecimal totalDebit = lines.stream()
                .map(CreateJournalEntryRequest.CreateJournalEntryLineRequest::debitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = lines.stream()
                .map(CreateJournalEntryRequest.CreateJournalEntryLineRequest::creditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalArgumentException("Journal entry is not balanced: total debit (" + totalDebit +
                    ") != total credit (" + totalCredit + ")");
        }

        for (CreateJournalEntryRequest.CreateJournalEntryLineRequest line : lines) {
            if (!line.isValid()) {
                throw new IllegalArgumentException("Line " + line.lineNumber() +
                        " must have either debit or credit amount, not both or neither");
            }
        }
    }

    private void calculateRunningBalances(List<GLEntry> glEntries) {
        glEntries.sort((a, b) -> {
            int dateCompare = a.getEntryDate().compareTo(b.getEntryDate());
            if (dateCompare != 0) return dateCompare;
            return a.getCreatedAt().compareTo(b.getCreatedAt());
        });

        for (GLEntry entry : glEntries) {
            ChartOfAccounts account = entry.getAccount();
            FiscalPeriod period = entry.getFiscalPeriod();

            BigDecimal lastBalance = glEntryRepository.findFirstByAccountAndFiscalPeriodOrderByEntryDateDescCreatedAtDesc(account, period)
                    .map(GLEntry::getRunningBalance)
                    .orElse(BigDecimal.ZERO);

            BigDecimal newBalance;
            if (entry.getEntryType() == EntryType.DEBIT) {
                newBalance = lastBalance.add(entry.getAmount());
            } else {
                newBalance = lastBalance.subtract(entry.getAmount());
            }
            entry.setRunningBalance(newBalance);
        }
    }

    private void publishEvent(JournalEntry journalEntry, String postedBy) {
        List<JournalEntryPostedEvent.JournalEntryLineEvent> lineEvents = journalEntry.getLines().stream()
                .map(line -> new JournalEntryPostedEvent.JournalEntryLineEvent(
                        line.getAccount().getId(),
                        line.getAccount().getCode(),
                        line.getAccount().getName(),
                        line.getCostCenter() != null ? line.getCostCenter().getId() : null,
                        line.getCostCenter() != null ? line.getCostCenter().getCode() : null,
                        line.getDebitAmount(),
                        line.getCreditAmount()
                ))
                .collect(Collectors.toList());

        JournalEntryPostedEvent event = new JournalEntryPostedEvent(
                UUID.randomUUID(),
                journalEntry.getId(),
                journalEntry.getReference(),
                journalEntry.getEntryDate(),
                journalEntry.getFiscalPeriod().getId(),
                journalEntry.getFiscalPeriod().getFiscalYear().getYear(),
                journalEntry.getFiscalPeriod().getPeriodNumber(),
                journalEntry.getTotalDebit(),
                journalEntry.getTotalCredit(),
                lineEvents,
                postedBy,
                journalEntry.getPostedAt(),
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend("finance.exchange", "journal.entry.posted", event);
    }

    private JournalEntryDTO toDTO(JournalEntry journalEntry) {
        List<JournalEntryDTO.JournalEntryLineDTO> lineDTOs = journalEntry.getLines().stream()
                .map(line -> new JournalEntryDTO.JournalEntryLineDTO(
                        line.getId(),
                        line.getAccount().getId(),
                        line.getCostCenter() != null ? line.getCostCenter().getId() : null,
                        line.getLineNumber(),
                        line.getDescription(),
                        line.getDebitAmount(),
                        line.getCreditAmount()
                ))
                .collect(Collectors.toList());

        return new JournalEntryDTO(
                journalEntry.getId(),
                journalEntry.getReference(),
                journalEntry.getEntryDate(),
                journalEntry.getFiscalPeriod().getId(),
                journalEntry.getDescription(),
                journalEntry.getStatus(),
                journalEntry.getTotalDebit(),
                journalEntry.getTotalCredit(),
                journalEntry.isReversed(),
                journalEntry.getReversedEntry() != null ? journalEntry.getReversedEntry().getId() : null,
                journalEntry.getReversalReason(),
                lineDTOs
        );
    }
}