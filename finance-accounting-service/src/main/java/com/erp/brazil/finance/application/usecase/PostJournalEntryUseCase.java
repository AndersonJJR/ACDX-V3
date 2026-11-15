package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.domain.event.JournalEntryPostedEvent;
import com.erp.brazil.finance.domain.model.ChartOfAccounts;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.JournalEntry;
import com.erp.brazil.finance.domain.model.JournalEntryLine;
import com.erp.brazil.finance.domain.model.JournalEntryStatus;
import com.erp.brazil.finance.domain.repository.ChartOfAccountsRepository;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import com.erp.brazil.finance.domain.repository.GLEntryRepository;
import com.erp.brazil.finance.domain.repository.JournalEntryRepository;
import com.erp.brazil.finance.domain.repository.JournalEntryLineRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PostJournalEntryUseCase {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final GLEntryRepository glEntryRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final EventPublisher eventPublisher;

    @Inject
    public PostJournalEntryUseCase(JournalEntryRepository journalEntryRepository,
                                   JournalEntryLineRepository journalEntryLineRepository,
                                   GLEntryRepository glEntryRepository,
                                   FiscalPeriodRepository fiscalPeriodRepository,
                                   ChartOfAccountsRepository chartOfAccountsRepository,
                                   EventPublisher eventPublisher) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.glEntryRepository = glEntryRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.chartOfAccountsRepository = chartOfAccountsRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Result execute(UUID journalEntryId, String postedBy) {
        JournalEntry journalEntry = journalEntryRepository.findById(journalEntryId)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found: " + journalEntryId));

        return switch (journalEntry.getStatus()) {
            case DRAFT -> postDraftEntry(journalEntry, postedBy);
            case POSTED -> Result.failure("Journal entry already posted");
            case REVERSED -> Result.failure("Journal entry has been reversed");
            case CANCELLED -> Result.failure("Journal entry is cancelled");
        };
    }

    private Result postDraftEntry(JournalEntry journalEntry, String postedBy) {
        // Validate fiscal period is not locked
        FiscalPeriod fiscalPeriod = journalEntry.getFiscalPeriod();
        if (fiscalPeriod.isLocked()) {
            return Result.failure("Fiscal period is locked: " + fiscalPeriod.getPeriodNumber());
        }

        // Validate entry is balanced
        if (!journalEntry.isBalanced()) {
            return Result.failure("Journal entry is not balanced: Debit=" + journalEntry.getTotalDebit() + ", Credit=" + journalEntry.getTotalCredit());
        }

        // Validate all lines have valid accounts
        List<JournalEntryLine> lines = journalEntryLineRepository.findByJournalEntryOrderByLineNumber(journalEntry);
        for (JournalEntryLine line : lines) {
            ChartOfAccounts account = line.getAccount();
            if (!account.isActive()) {
                return Result.failure("Account is inactive: " + account.getCode());
            }
            if (!account.isDetail()) {
                return Result.failure("Account is not a detail account: " + account.getCode());
            }
            if (!fiscalPeriod.getFiscalYear().equals(account.getFiscalYear())) {
                return Result.failure("Account belongs to different fiscal year: " + account.getCode());
            }
        }

        // Update journal entry status
        journalEntry.setStatus(JournalEntryStatus.POSTED);
        journalEntry.setPostedAt(LocalDateTime.now());
        journalEntry.setPostedBy(postedBy);
        journalEntry = journalEntryRepository.save(journalEntry);

        // Create GL entries
        createGLEntries(journalEntry, lines);

        // Publish event
        eventPublisher.publish(JournalEntryPostedEvent.from(journalEntry));

        return Result.success(journalEntry);
    }

    private void createGLEntries(JournalEntry journalEntry, List<JournalEntryLine> lines) {
        for (JournalEntryLine line : lines) {
            if (line.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
                createGLEntry(journalEntry, line, line.getDebitAmount(), com.erp.brazil.finance.domain.model.EntryType.DEBIT);
            }
            if (line.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                createGLEntry(journalEntry, line, line.getCreditAmount(), com.erp.brazil.finance.domain.model.EntryType.CREDIT);
            }
        }
    }

    private void createGLEntry(JournalEntry journalEntry, JournalEntryLine line, BigDecimal amount, com.erp.brazil.finance.domain.model.EntryType entryType) {
        ChartOfAccounts account = line.getAccount();
        FiscalPeriod fiscalPeriod = journalEntry.getFiscalPeriod();

        // Get running balance
        BigDecimal runningBalance = glEntryRepository.getRunningBalanceForAccount(account, journalEntry.getEntryDate());
        if (runningBalance == null) {
            runningBalance = BigDecimal.ZERO;
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
        void publish(JournalEntryPostedEvent event);
    }
}