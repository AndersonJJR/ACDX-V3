package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.domain.event.FiscalPeriodLockedEvent;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.JournalEntry;
import com.erp.brazil.finance.domain.model.JournalEntryStatus;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import com.erp.brazil.finance.domain.repository.JournalEntryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class LockFiscalPeriodUseCase {

    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final EventPublisher eventPublisher;

    @Inject
    public LockFiscalPeriodUseCase(FiscalPeriodRepository fiscalPeriodRepository,
                                   JournalEntryRepository journalEntryRepository,
                                   EventPublisher eventPublisher) {
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Result execute(UUID fiscalPeriodId, String lockedBy) {
        FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(fiscalPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("Fiscal period not found: " + fiscalPeriodId));

        return switch (fiscalPeriod) {
            case FiscalPeriod p when p.isLocked() -> Result.failure("Fiscal period already locked");
            case FiscalPeriod p when p.isClosed() -> Result.failure("Fiscal period already closed");
            case FiscalPeriod p -> lockPeriod(p, lockedBy);
        };
    }

    private Result lockPeriod(FiscalPeriod fiscalPeriod, String lockedBy) {
        // Check for unposted journal entries
        List<JournalEntry> unpostedEntries = journalEntryRepository.findByFiscalPeriodAndStatusInOrderByEntryDateDesc(
                fiscalPeriod, List.of(JournalEntryStatus.DRAFT));

        if (!unpostedEntries.isEmpty()) {
            return Result.failure("Cannot lock period: " + unpostedEntries.size() + " draft journal entries exist");
        }

        // Lock the period
        fiscalPeriod.setLocked(true);
        fiscalPeriod.setLockedAt(LocalDate.now());
        fiscalPeriod.setLockedBy(lockedBy);
        fiscalPeriod = fiscalPeriodRepository.save(fiscalPeriod);

        // Publish event
        eventPublisher.publish(FiscalPeriodLockedEvent.from(fiscalPeriod));

        return Result.success(fiscalPeriod);
    }

    @Transactional
    public Result unlock(UUID fiscalPeriodId, String unlockedBy) {
        FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(fiscalPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("Fiscal period not found: " + fiscalPeriodId));

        if (!fiscalPeriod.isLocked()) {
            return Result.failure("Fiscal period is not locked");
        }

        if (fiscalPeriod.isClosed()) {
            return Result.failure("Cannot unlock a closed period");
        }

        fiscalPeriod.setLocked(false);
        fiscalPeriod.setLockedAt(null);
        fiscalPeriod.setLockedBy(null);
        fiscalPeriod = fiscalPeriodRepository.save(fiscalPeriod);

        return Result.success(fiscalPeriod);
    }

    public sealed interface Result permits Result.Success, Result.Failure {
        boolean isSuccess();
        static Success success(FiscalPeriod fiscalPeriod) { return new Success(fiscalPeriod); }
        static Failure failure(String message) { return new Failure(message); }

        record Success(FiscalPeriod fiscalPeriod) implements Result {
            @Override public boolean isSuccess() { return true; }
        }
        record Failure(String message) implements Result {
            @Override public boolean isSuccess() { return false; }
        }
    }

    interface EventPublisher {
        void publish(FiscalPeriodLockedEvent event);
    }
}