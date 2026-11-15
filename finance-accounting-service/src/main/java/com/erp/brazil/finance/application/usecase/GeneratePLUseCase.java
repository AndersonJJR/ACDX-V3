package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.domain.model.ChartOfAccounts;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.FiscalYear;
import com.erp.brazil.finance.domain.repository.ChartOfAccountsRepository;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import com.erp.brazil.finance.domain.repository.FiscalYearRepository;
import com.erp.brazil.finance.domain.repository.GLEntryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class GeneratePLUseCase {

    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final FiscalYearRepository fiscalYearRepository;
    private final GLEntryRepository glEntryRepository;

    @Inject
    public GeneratePLUseCase(ChartOfAccountsRepository chartOfAccountsRepository,
                             FiscalPeriodRepository fiscalPeriodRepository,
                             FiscalYearRepository fiscalYearRepository,
                             GLEntryRepository glEntryRepository) {
        this.chartOfAccountsRepository = chartOfAccountsRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.fiscalYearRepository = fiscalYearRepository;
        this.glEntryRepository = glEntryRepository;
    }

    public Result execute(UUID fiscalPeriodId, boolean yearToDate) {
        FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(fiscalPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("Fiscal period not found: " + fiscalPeriodId));

        FiscalYear fiscalYear = fiscalPeriod.getFiscalYear();
        List<ChartOfAccounts> accounts = chartOfAccountsRepository.findByFiscalYearOrderByCode(fiscalYear);

        List<PLLine> revenueLines = new ArrayList<>();
        List<PLLine> expenseLines = new ArrayList<>();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        List<FiscalPeriod> periods = yearToDate
                ? fiscalPeriodRepository.findByFiscalYearOrderByPeriodNumber(fiscalYear).stream()
                    .filter(p -> p.getPeriodNumber() <= fiscalPeriod.getPeriodNumber())
                    .toList()
                : List.of(fiscalPeriod);

        for (ChartOfAccounts account : accounts) {
            if (!account.isDetail()) {
                continue;
            }

            BigDecimal periodBalance = BigDecimal.ZERO;
            for (FiscalPeriod period : periods) {
                BigDecimal balance = glEntryRepository.getBalanceForAccountInPeriod(account, period);
                if (balance != null) {
                    periodBalance = periodBalance.add(balance);
                }
            }

            if (periodBalance.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            PLLine line = new PLLine(
                    account.getCode(),
                    account.getName(),
                    periodBalance.abs()
            );

            switch (account.getAccountType()) {
                case REVENUE -> {
                    revenueLines.add(line);
                    totalRevenue = totalRevenue.add(periodBalance.abs());
                }
                case EXPENSE -> {
                    expenseLines.add(line);
                    totalExpenses = totalExpenses.add(periodBalance.abs());
                }
                default -> {} // Asset, Liability, Equity don't appear on P&L
            }
        }

        BigDecimal netIncome = totalRevenue.subtract(totalExpenses);

        return Result.success(new ProfitLossStatement(
                fiscalPeriod,
                yearToDate,
                revenueLines,
                expenseLines,
                totalRevenue,
                totalExpenses,
                netIncome
        ));
    }

    public record ProfitLossStatement(
            FiscalPeriod fiscalPeriod,
            boolean yearToDate,
            List<PLLine> revenues,
            List<PLLine> expenses,
            BigDecimal totalRevenue,
            BigDecimal totalExpenses,
            BigDecimal netIncome
    ) {}

    public record PLLine(
            String accountCode,
            String accountName,
            BigDecimal amount
    ) {}

    public sealed interface Result permits Result.Success, Result.Failure {
        boolean isSuccess();
        static Success success(ProfitLossStatement pl) { return new Success(pl); }
        static Failure failure(String message) { return new Failure(message); }

        record Success(ProfitLossStatement pl) implements Result {
            @Override public boolean isSuccess() { return true; }
        }
        record Failure(String message) implements Result {
            @Override public boolean isSuccess() { return false; }
        }
    }
}