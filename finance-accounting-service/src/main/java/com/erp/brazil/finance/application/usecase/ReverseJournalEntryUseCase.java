package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.JournalEntry;
import com.erp.brazil.finance.domain.model.JournalEntryStatus;
import com.erp.brazil.finance.domain.model.ReversalEntry;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import com.erp.brazil.finance.domain.repository.GLEntryRepository;
import com.erp.brazil.finance.domain.repository.JournalEntryRepository;
import com.erp.brazil.finance.domain.repository.ReversalEntryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class ReverseJournalEntryUseCase {

    private final JournalEntryRepository journalEntryRepository;
    private final ReversalEntryRepository reversalEntryRepository;
    private final GLEntryRepository glEntryRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final EventPublisher eventPublisher;

    @Inject
    public ReverseJournalEntryUseCase(JournalEntryRepository journalEntryRepository,
                                      ReversalEntryRepository reversalEntryRepository,
                                      GLEntryRepository glEntryRepository,
                                      FiscalPeriodRepository fiscalPeriodRepository,
                                      EventPublisher eventPublisher) {
        this.journalEntryRepository = journalEntryRepository;
        this.reversalEntryRepository = reversalEntryRepository;
        this.glEntryRepository = glEntryRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Result execute(UUID journalEntryId, LocalDate reversalDate, String reversalReason, String reversedBy) {
        JournalEntry originalEntry = journalEntryRepository.findById(journalEntryId)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found: " + journalEntryId));

        return switch (originalEntry.getStatus()) {
            case POSTED -> createReversalEntry(originalEntry, reversalDate, reversalReason, reversedBy);
            case DRAFT -> Result.failure("Cannot reverse a draft entry");
            case REVERSED -> Result.failure("Journal entry already reversed");
            case CANCELLED -> Result.failure("Journal entry is cancelled");
        };
    }

    private Result createReversalEntry(JournalEntry originalEntry, LocalDate reversalDate, String reversalReason, String reversedBy) {
        // Validate fiscal period for reversal date
        FiscalPeriod reversalPeriod = fiscalPeriodRepository.findByFiscalYearAndDate(
                originalEntry.getFiscalPeriod().getFiscalYear(), reversalDate)
                .orElseThrow(() -> new IllegalArgumentException("No fiscal period found for reversal date: " + reversalDate));

        if (reversalPeriod.isLocked()) {
            return Result.failure("Reversal fiscal period is locked: " + reversalPeriod.getPeriodNumber());
        }

        if (reversalPeriod.isClosed()) {
            return Result.failure("Reversal fiscal period is closed: " + reversalPeriod.getPeriodNumber());
        }

        // Create reversal journal entry
        JournalEntry reversalEntry = new JournalEntry(
                "REV-" + originalEntry.getReference(),
                reversalDate,
                reversalPeriod,
                "Reversal of: " + originalEntry.getReference() + " - " + reversalReason
        );

        // Copy lines with swapped debit/credit
        for (var line : originalEntry.getLines()) {
            reversalEntry.addLine(new com.erp.brazil.finance.domain.model.JournalEntryLine(
                    reversalEntry,
                    line.getAccount(),
                    line.getLineNumber(),
                    line.getCreditAmount(),
                    line.getDebitAmount()
            ));
            reversalEntry.getLines().getLast().setCostCenter(line.getCostCenter());
            reversalEntry.getLines().getLast().setDescription("Reversal: " + line.getDescription());
        }

        reversalEntry.setStatus(JournalEntryStatus.POSTED);
        reversalEntry.setPostedAt(LocalDateTime.now());
        reversalEntry.setPostedBy(reversedBy);
        reversalEntry = journalEntryRepository.save(reversalEntry);

        // Create GL entries for reversal
        for (var line : reversalEntry.getLines()) {
            if (line.getDebitAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                createGLEntry(reversalEntry, line, line.getDebitAmount(), com.erp.brazil.finance.domain.model.EntryType.DEBIT);
            }
            if (line.getCreditAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                createGLEntry(reversalEntry, line, line.getCreditAmount(), com.erp.brazil.finance.domain.model.EntryType.CREDIT);
            }
        }

        // Create reversal record
        ReversalEntry reversal = new ReversalEntry(originalEntry, reversalEntry, reversalDate, reversalReason, reversedBy);
        reversalEntryRepository.save(reversal);

        // Update original entry
        originalEntry.setReversed(true);
        originalEntry.setReversedEntry(reversalEntry);
        originalEntry.setReversalReason(reversalReason);
        originalEntry.setStatus(JournalEntryStatus.REVERSED);
        journalEntryRepository.save(originalEntry);

        return Result.success(reversalEntry);
    }

    private void createGLEntry(JournalEntry journalEntry, com.erp.brazil.finance.domain.model.JournalEntryLine line,
                               java.math.BigDecimal amount, com.erp.brazil.finance.domain.model.EntryType entryType) {
        var account = line.getAccount();
        var fiscalPeriod = journalEntry.getFiscalPeriod();

        java.math.BigDecimal runningBalance = glEntryRepository.getRunningBalanceForAccount(account, journalEntry.getEntryDate());
        if (runningBalance == null) {
            runningBalance = java.math.BigDecimal.ZERO;
        }

        if (entryType == com.erp.brazil.finance.domain.model.EntryType.DEBIT) {
            runningBalance = runningBalance.add(amount);
        } else {
            runningBalance = runningBalance.subtract(amount);
        }

        com.erp.brazil.finance.domain.model.GLEntry glEntry = new com.erp.brazil.finance.domain.model.GLEntry(
                journalEntry,
                account,
                fiscalPeriod,
                journalEntry.getEntryDate(),
                entryType,
                amount
        );
        glEntry.setRunningBalance(runningBalance);
        glEntry.setCostCenter(line.getCostCenter());
        glEntry.setDescription(line.getDescription());

        glEntryRepository.save(glEntry);
    }

    public sealed interface Result permits Result.Success, Result.Failure {
        boolean isSuccess();
        static Success success(JournalEntry journalEntry) { return new Success(journalEntry); }
        static Failure failure(String message) { return new Failure(message); }

        record Success(JournalEntry journalEntry) implements Result {
            @Override public boolean isSuccess() { return true; }
        }
        record Failure(String message) implements Result {
            @Override public boolean isSuccess() { return false; }
        }
    }

    interface EventPublisher {
        void publish(Object event);
    }
}