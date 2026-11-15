package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.domain.model.ChartOfAccounts;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.repository.ChartOfAccountsRepository;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import com.erp.brazil.finance.domain.repository.GLEntryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class GenerateBalanceSheetUseCase {

    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final GLEntryRepository glEntryRepository;

    @Inject
    public GenerateBalanceSheetUseCase(ChartOfAccountsRepository chartOfAccountsRepository,
                                       FiscalPeriodRepository fiscalPeriodRepository,
                                       GLEntryRepository glEntryRepository) {
        this.chartOfAccountsRepository = chartOfAccountsRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.glEntryRepository = glEntryRepository;
    }

    public Result execute(UUID fiscalPeriodId) {
        FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(fiscalPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("Fiscal period not found: " + fiscalPeriodId));

        List<ChartOfAccounts> accounts = chartOfAccountsRepository.findByFiscalYearOrderByCode(fiscalPeriod.getFiscalYear());

        List<BalanceSheetLine> assetLines = new ArrayList<>();
        List<BalanceSheetLine> liabilityLines = new ArrayList<>();
        List<BalanceSheetLine> equityLines = new ArrayList<>();

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;

        for (ChartOfAccounts account : accounts) {
            if (!account.isDetail()) {
                continue;
            }

            BigDecimal balance = glEntryRepository.getBalanceForAccountInPeriod(account, fiscalPeriod);
            if (balance == null) {
                balance = BigDecimal.ZERO;
            }

            if (balance.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            BalanceSheetLine line = new BalanceSheetLine(
                    account.getCode(),
                    account.getName(),
                    balance.abs()
            );

            switch (account.getAccountType()) {
                case ASSET -> {
                    assetLines.add(line);
                    totalAssets = totalAssets.add(balance.abs());
                }
                case LIABILITY -> {
                    liabilityLines.add(line);
                    totalLiabilities = totalLiabilities.add(balance.abs());
                }
                case EQUITY -> {
                    equityLines.add(line);
                    totalEquity = totalEquity.add(balance.abs());
                }
                default -> {} // Revenue and Expense accounts don't appear on balance sheet
            }
        }

        BigDecimal totalLiabilitiesAndEquity = totalLiabilities.add(totalEquity);

        return Result.success(new BalanceSheet(
                fiscalPeriod,
                assetLines,
                liabilityLines,
                equityLines,
                totalAssets,
                totalLiabilities,
                totalEquity,
                totalLiabilitiesAndEquity
        ));
    }

    public record BalanceSheet(
            FiscalPeriod fiscalPeriod,
            List<BalanceSheetLine> assets,
            List<BalanceSheetLine> liabilities,
            List<BalanceSheetLine> equity,
            BigDecimal totalAssets,
            BigDecimal totalLiabilities,
            BigDecimal totalEquity,
            BigDecimal totalLiabilitiesAndEquity
    ) {
        public boolean isBalanced() {
            return totalAssets.compareTo(totalLiabilitiesAndEquity) == 0;
        }
    }

    public record BalanceSheetLine(
            String accountCode,
            String accountName,
            BigDecimal amount
    ) {}

    public sealed interface Result permits Result.Success, Result.Failure {
        boolean isSuccess();
        static Success success(BalanceSheet balanceSheet) { return new Success(balanceSheet); }
        static Failure failure(String message) { return new Failure(message); }

        record Success(BalanceSheet balanceSheet) implements Result {
            @Override public boolean isSuccess() { return true; }
        }
        record Failure(String message) implements Result {
            @Override public boolean isSuccess() { return false; }
        }
    }
}