package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.application.dto.AccountBalanceDTO;
import com.erp.brazil.finance.application.dto.BalanceSheetRequest;
import com.erp.brazil.finance.application.dto.BalanceSheetResponse;
import com.erp.brazil.finance.domain.model.AccountType;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import com.erp.brazil.finance.domain.repository.GLEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GenerateBalanceSheetUseCase {

    private final GLEntryRepository glEntryRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;

    public GenerateBalanceSheetUseCase(GLEntryRepository glEntryRepository,
                                       FiscalPeriodRepository fiscalPeriodRepository) {
        this.glEntryRepository = glEntryRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
    }

    public BalanceSheetResponse execute(BalanceSheetRequest request) {
        FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(request.fiscalPeriodId())
                .orElseThrow(() -> new IllegalArgumentException("Fiscal period not found: " + request.fiscalPeriodId()));

        LocalDate asOfDate = request.asOfDate() != null ? request.asOfDate() : fiscalPeriod.getEndDate();

        List<AccountBalanceDTO> allAccounts = glEntryRepository.getTrialBalance(fiscalPeriod);

        List<AccountBalanceDTO> assetAccounts = allAccounts.stream()
                .filter(a -> a.isDebitBalance() || a.debitTotal().compareTo(BigDecimal.ZERO) > 0 || a.creditTotal().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        List<BalanceSheetResponse.BalanceSheetSectionDTO> assets = buildHierarchy(assetAccounts, AccountType.ASSET);
        List<BalanceSheetResponse.BalanceSheetSectionDTO> liabilities = buildHierarchy(
                allAccounts.stream().filter(a -> a.isCreditBalance()).toList(), AccountType.LIABILITY);
        List<BalanceSheetResponse.BalanceSheetSectionDTO> equity = buildHierarchy(
                allAccounts.stream().filter(a -> a.isCreditBalance() && a.code().startsWith("3")).toList(), AccountType.EQUITY);

        BigDecimal totalAssets = assets.stream()
                .map(BalanceSheetResponse.BalanceSheetSectionDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLiabilities = liabilities.stream()
                .map(BalanceSheetResponse.BalanceSheetSectionDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEquity = equity.stream()
                .map(BalanceSheetResponse.BalanceSheetSectionDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BalanceSheetResponse(
                fiscalPeriod.getId(),
                asOfDate,
                assets,
                liabilities,
                equity,
                totalAssets,
                totalLiabilities,
                totalEquity,
                totalAssets.compareTo(totalLiabilities.add(totalEquity)) == 0
        );
    }

    private List<BalanceSheetResponse.BalanceSheetSectionDTO> buildHierarchy(List<AccountBalanceDTO> accounts, AccountType type) {
        List<BalanceSheetResponse.BalanceSheetSectionDTO> result = new ArrayList<>();

        for (AccountBalanceDTO account : accounts) {
            if (type == AccountType.ASSET && account.getAccountType() != AccountType.ASSET) continue;
            if (type == AccountType.LIABILITY && account.getAccountType() != AccountType.LIABILITY) continue;
            if (type == AccountType.EQUITY && account.getAccountType() != AccountType.EQUITY) continue;

            BalanceSheetResponse.BalanceSheetSectionDTO section = new BalanceSheetResponse.BalanceSheetSectionDTO(
                    account.code(),
                    account.name(),
                    BalanceSheetResponse.BalanceSheetSectionDTO.AccountType.valueOf(type.name()),
                    account.getBalance(),
                    List.of()
            );
            result.add(section);
        }

        return result.stream()
                .sorted(Comparator.comparing(BalanceSheetResponse.BalanceSheetSectionDTO::code))
                .collect(Collectors.toList());
    }
}