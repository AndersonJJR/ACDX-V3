package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.application.dto.AccountBalanceDTO;
import com.erp.brazil.finance.application.dto.TrialBalanceRequest;
import com.erp.brazil.finance.application.dto.TrialBalanceResponse;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import com.erp.brazil.finance.domain.repository.GLEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GenerateTrialBalanceUseCase {

    private final GLEntryRepository glEntryRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;

    public GenerateTrialBalanceUseCase(GLEntryRepository glEntryRepository,
                                       FiscalPeriodRepository fiscalPeriodRepository) {
        this.glEntryRepository = glEntryRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
    }

    public TrialBalanceResponse execute(TrialBalanceRequest request) {
        FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(request.fiscalPeriodId())
                .orElseThrow(() -> new IllegalArgumentException("Fiscal period not found: " + request.fiscalPeriodId()));

        LocalDate asOfDate = request.asOfDate() != null ? request.asOfDate() : fiscalPeriod.getEndDate();

        List<AccountBalanceDTO> accounts = glEntryRepository.getTrialBalance(fiscalPeriod);

        if (!request.includeZeroBalances()) {
            accounts = accounts.stream()
                    .filter(a -> a.getBalance().compareTo(java.math.BigDecimal.ZERO) != 0)
                    .toList();
        }

        return TrialBalanceResponse.of(fiscalPeriod.getId(), asOfDate, accounts);
    }
}