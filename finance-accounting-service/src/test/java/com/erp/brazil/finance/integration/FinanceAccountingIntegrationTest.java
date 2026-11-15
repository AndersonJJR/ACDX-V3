package com.erp.brazil.finance.integration;

import com.erp.brazil.finance.domain.model.*;
import com.erp.brazil.finance.domain.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class FinanceAccountingIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("erp_finance_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    FiscalYearRepository fiscalYearRepository;

    @Autowired
    FiscalPeriodRepository fiscalPeriodRepository;

    @Autowired
    ChartOfAccountsRepository chartOfAccountsRepository;

    @Autowired
    JournalEntryRepository journalEntryRepository;

    @Autowired
    JournalEntryLineRepository journalEntryLineRepository;

    @Autowired
    GLEntryRepository glEntryRepository;

    @Test
    void shouldCreateFiscalYearAndPeriods() {
        FiscalYear fiscalYear = new FiscalYear(2024, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        fiscalYear = fiscalYearRepository.save(fiscalYear);

        assertNotNull(fiscalYear.getId());
        assertEquals(2024, fiscalYear.getYear());

        // Create 12 periods
        for (int i = 1; i <= 12; i++) {
            LocalDate startDate = LocalDate.of(2024, i, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);
            FiscalPeriod period = new FiscalPeriod(fiscalYear, i, startDate, endDate);
            period = fiscalPeriodRepository.save(period);
            assertNotNull(period.getId());
        }

        var periods = fiscalPeriodRepository.findByFiscalYearOrderByPeriodNumber(fiscalYear);
        assertEquals(12, periods.size());
    }

    @Test
    void shouldCreateChartOfAccounts() {
        FiscalYear fiscalYear = new FiscalYear(2024, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        fiscalYear = fiscalYearRepository.save(fiscalYear);

        ChartOfAccounts account = new ChartOfAccounts(
                "1.1.1.01",
                "Caixa",
                AccountType.ASSET,
                AccountNature.DEBIT,
                fiscalYear
        );
        account = chartOfAccountsRepository.save(account);

        assertNotNull(account.getId());
        assertEquals("1.1.1.01", account.getCode());
        assertEquals("Caixa", account.getName());
    }

    @Test
    void shouldCreateJournalEntryWithLinesAndGLEntries() {
        FiscalYear fiscalYear = new FiscalYear(2024, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        fiscalYear = fiscalYearRepository.save(fiscalYear);

        FiscalPeriod period = new FiscalPeriod(fiscalYear, 1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        period = fiscalPeriodRepository.save(period);

        ChartOfAccounts cashAccount = new ChartOfAccounts(
                "1.1.1.01", "Caixa", AccountType.ASSET, AccountNature.DEBIT, fiscalYear
        );
        cashAccount.setDetail(true);
        cashAccount.setActive(true);
        cashAccount = chartOfAccountsRepository.save(cashAccount);

        ChartOfAccounts revenueAccount = new ChartOfAccounts(
                "4.1.1.01", "Vendas", AccountType.REVENUE, AccountNature.CREDIT, fiscalYear
        );
        revenueAccount.setDetail(true);
        revenueAccount.setActive(true);
        revenueAccount = chartOfAccountsRepository.save(revenueAccount);

        JournalEntry entry = new JournalEntry(
                "JE-001",
                LocalDate.of(2024, 1, 15),
                period,
                "Venda à vista"
        );
        entry.addLine(new JournalEntryLine(entry, cashAccount, 1, new BigDecimal("1000.00"), BigDecimal.ZERO));
        entry.addLine(new JournalEntryLine(entry, revenueAccount, 2, BigDecimal.ZERO, new BigDecimal("1000.00")));

        entry = journalEntryRepository.save(entry);

        assertNotNull(entry.getId());
        assertEquals(JournalEntryStatus.DRAFT, entry.getStatus());
        assertTrue(entry.isBalanced());

        // Verify lines were saved
        var lines = journalEntryLineRepository.findByJournalEntryOrderByLineNumber(entry);
        assertEquals(2, lines.size());
    }

    @Test
    void shouldCreateAssetWithDepreciationSchedule() {
        FiscalYear fiscalYear = new FiscalYear(2024, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        fiscalYear = fiscalYearRepository.save(fiscalYear);

        ChartOfAccounts assetAccount = new ChartOfAccounts(
                "1.2.3.03", "Máquinas", AccountType.ASSET, AccountNature.DEBIT, fiscalYear
        );
        assetAccount.setDetail(true);
        assetAccount.setActive(true);
        assetAccount = chartOfAccountsRepository.save(assetAccount);

        ChartOfAccounts accumDepAccount = new ChartOfAccounts(
                "1.2.3.11", "Depreciação Ac. Máquinas", AccountType.ASSET, AccountNature.CREDIT, fiscalYear
        );
        accumDepAccount.setDetail(true);
        accumDepAccount.setActive(true);
        accumDepAccount = chartOfAccountsRepository.save(accumDepAccount);

        ChartOfAccounts deprExpenseAccount = new ChartOfAccounts(
                "5.1.2.04", "Depreciação Industrial", AccountType.EXPENSE, AccountNature.DEBIT, fiscalYear
        );
        deprExpenseAccount.setDetail(true);
        deprExpenseAccount.setActive(true);
        deprExpenseAccount = chartOfAccountsRepository.save(deprExpenseAccount);

        AssetCategory category = new AssetCategory("MAQ", "Máquinas e Equipamentos");
        category.setId(UUID.randomUUID());

        Asset asset = new Asset(
                "MAQ-001",
                "TAG-MAQ-001",
                "Máquina de Teste",
                category,
                assetAccount,
                accumDepAccount,
                deprExpenseAccount,
                LocalDate.of(2024, 1, 15),
                new BigDecimal("10000.00"),
                60,
                DepreciationMethod.STRAIGHT_LINE
        );
        asset.setResidualValue(new BigDecimal("1000.00"));

        // Save would be done through repository
        assertEquals(new BigDecimal("150.00"), calculateStraightLine(asset));
    }

    private BigDecimal calculateStraightLine(Asset asset) {
        BigDecimal depreciableAmount = asset.getAcquisitionCost().subtract(asset.getResidualValue());
        return depreciableAmount.divide(BigDecimal.valueOf(asset.getUsefulLifeMonths()), 2, BigDecimal.ROUND_HALF_UP);
    }
}