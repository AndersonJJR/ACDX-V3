package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.application.dto.FiscalPeriodDTO;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UnlockFiscalPeriodUseCase {

    private final FiscalPeriodRepository fiscalPeriodRepository;

    public UnlockFiscalPeriodUseCase(FiscalPeriodRepository fiscalPeriodRepository) {
        this.fiscalPeriodRepository = fiscalPeriodRepository;
    }

    public FiscalPeriodDTO execute(UUID fiscalPeriodId) {
        FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(fiscalPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("Fiscal period not found: " + fiscalPeriodId));

        if (!fiscalPeriod.isLocked()) {
            throw new IllegalStateException("Fiscal period is not locked");
        }

        if (fiscalPeriod.isClosed()) {
            throw new IllegalStateException("Cannot unlock a closed fiscal period");
        }

        fiscalPeriod.setLocked(false);
        fiscalPeriod.setLockedAt(null);
        fiscalPeriod.setLockedBy(null);

        fiscalPeriodRepository.save(fiscalPeriod);

        return toDTO(fiscalPeriod);
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