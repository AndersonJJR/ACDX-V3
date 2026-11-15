package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.application.dto.FiscalPeriodDTO;
import com.erp.brazil.finance.application.dto.LockFiscalPeriodRequest;
import com.erp.brazil.finance.application.event.FiscalPeriodLockedEvent;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.JournalEntryStatus;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import com.erp.brazil.finance.domain.repository.JournalEntryRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class LockFiscalPeriodUseCase {

    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final RabbitTemplate rabbitTemplate;

    public LockFiscalPeriodUseCase(FiscalPeriodRepository fiscalPeriodRepository,
                                   JournalEntryRepository journalEntryRepository,
                                   RabbitTemplate rabbitTemplate) {
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public FiscalPeriodDTO execute(UUID fiscalPeriodId, LockFiscalPeriodRequest request, String lockedBy) {
        FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(fiscalPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("Fiscal period not found: " + fiscalPeriodId));

        if (fiscalPeriod.isLocked()) {
            throw new IllegalStateException("Fiscal period is already locked");
        }

        if (fiscalPeriod.isClosed()) {
            throw new IllegalStateException("Cannot lock a closed fiscal period");
        }

        long unpostedCount = journalEntryRepository.countByFiscalPeriodAndStatus(fiscalPeriod, JournalEntryStatus.DRAFT);
        if (unpostedCount > 0) {
            throw new IllegalStateException("Cannot lock fiscal period with " + unpostedCount + " unposted (draft) journal entries");
        }

        fiscalPeriod.setLocked(true);
        fiscalPeriod.setLockedAt(request.lockDate());
        fiscalPeriod.setLockedBy(lockedBy);

        fiscalPeriodRepository.save(fiscalPeriod);

        publishEvent(fiscalPeriod, request.lockDate(), lockedBy);

        return toDTO(fiscalPeriod);
    }

    private void publishEvent(FiscalPeriod fiscalPeriod, java.time.LocalDate lockDate, String lockedBy) {
        FiscalPeriodLockedEvent event = new FiscalPeriodLockedEvent(
                UUID.randomUUID(),
                fiscalPeriod.getId(),
                fiscalPeriod.getFiscalYear().getYear(),
                fiscalPeriod.getPeriodNumber(),
                lockDate,
                lockedBy,
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend("finance.exchange", "fiscal.period.locked", event);
    }

    private FiscalPeriodDTO toDTO(FiscalPeriod fiscalPeriod) {
        return new FiscalPeriodDTO(
                fiscalPeriod.getId(),
                fiscalPeriod.getFiscalYear().getId(),
                fiscalPeriod.getFiscalYear().getYear(),
                fiscalPeriod.getPeriodNumber(),
                fiscalPeriod.getName(),
                fiscalPeriod.getStartDate(),
                fiscalPeriod.getEndDate(),
                fiscalPeriod.isLocked(),
                fiscalPeriod.getLockedAt(),
                fiscalPeriod.getLockedBy(),
                fiscalPeriod.isClosed(),
                fiscalPeriod.getClosedAt()
        );
    }
}