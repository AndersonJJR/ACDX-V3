package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.domain.model.ChartOfAccounts;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.GLEntry;
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
public class GenerateTrialBalanceUseCase {

    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final GLEntryRepository glEntryRepository;

    @Inject
    public GenerateTrialBalanceUseCase(ChartOfAccountsRepository chartOfAccountsRepository,
                                       FiscalPeriodRepository fiscalPeriodRepository,
                                       GLEntryRepository glEntryRepository) {
        this.chartOfAccountsRepository = chartOfAccountsRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.glEntryRepository = glEntryRepository;
    }

    public Result execute(UUID fiscalPeriodId, boolean includeZeroBalances) {
        FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(fiscalPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("Fiscal period not found: " + fiscalPeriodId));

        List<ChartOfAccounts> accounts = chartOfAccountsRepository.findByFiscalYearOrderByCode(fiscalPeriod.getFiscalYear());

        List<TrialBalanceLine> lines = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (ChartOfAccounts account : accounts) {
            if (!account.isDetail()) {
                continue; // Skip header accounts
            }

            BigDecimal debitBalance = glEntryRepository.getBalanceForAccountInPeriod(account, fiscalPeriod);
            if (debitBalance == null) {
                debitBalance = BigDecimal.ZERO;
            }

            BigDecimal debitAmount = BigDecimal.ZERO;
            BigDecimal creditAmount = BigDecimal.ZERO;

            if (account.getAccountNature() == com.erp.brazil.finance.domain.model.AccountNature.DEBIT) {
                if (debitBalance.compareTo(BigDecimal.ZERO) >= 0) {
                    debitAmount = debitBalance;
                } else {
                    creditAmount = debitBalance.abs();
                }
            } else {
                if (debitBalance.compareTo(BigDecimal.ZERO) <= 0) {
                    creditAmount = debitBalance.abs();
                } else {
                    debitAmount = debitBalance;
                }
            }

            if (includeZeroBalances || debitAmount.compareTo(BigDecimal.ZERO) != 0 || creditAmount.compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new TrialBalanceLine(
                        account.getCode(),
                        account.getName(),
                        account.getAccountType(),
                        debitAmount,
                        creditAmount
                ));
                totalDebit = totalDebit.add(debitAmount);
                totalCredit = totalCredit.add(creditAmount);
            }
        }

        return Result.success(new TrialBalance(fiscalPeriod, lines, totalDebit, totalCredit));
    }

    public record TrialBalance(
            FiscalPeriod fiscalPeriod,
            List<TrialBalanceLine> lines,
            BigDecimal totalDebit,
            BigDecimal totalCredit
    ) {
        public boolean isBalanced() {
            return totalDebit.compareTo(totalCredit) == 0;
        }
    }

    public record TrialBalanceLine(
            String accountCode,
            String accountName,
            com.erp.brazil.finance.domain.model.AccountType accountType,
            BigDecimal debitAmount,
            BigDecimal creditAmount
    ) {}

    public sealed interface Result permits Result.Success, Result.Failure {
        boolean isSuccess();
        static Success success(TrialBalance trialBalance) { return new Success(trialBalance); }
        static Failure failure(String message) { return new Failure(message); }

        record Success(TrialBalance trialBalance) implements Result {
            @Override public boolean isSuccess() { return true; }
        }
        record Failure(String message) implements Result {
            @Override public boolean isSuccess() { return false; }
        }
    }
}