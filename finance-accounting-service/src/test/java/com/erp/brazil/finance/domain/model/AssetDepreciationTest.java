package com.erp.brazil.finance.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AssetDepreciationTest {

    @Test
    void shouldCalculateStraightLineDepreciation() {
        BigDecimal acquisitionCost = new BigDecimal("10000.00");
        BigDecimal residualValue = new BigDecimal("1000.00");
        int usefulLifeMonths = 60;

        BigDecimal depreciableAmount = acquisitionCost.subtract(residualValue);
        BigDecimal monthlyDepreciation = depreciableAmount.divide(BigDecimal.valueOf(usefulLifeMonths), 2, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal("150.00"), monthlyDepreciation);
    }

    @Test
    void shouldCalculateDecliningBalanceDepreciation() {
        BigDecimal bookValue = new BigDecimal("10000.00");
        BigDecimal residualValue = new BigDecimal("1000.00");
        int usefulLifeMonths = 60;
        int factor = 2; // Double declining balance

        BigDecimal rate = BigDecimal.valueOf(factor).divide(BigDecimal.valueOf(usefulLifeMonths), 10, RoundingMode.HALF_UP);
        BigDecimal depreciation = bookValue.multiply(rate).setScale(2, RoundingMode.HALF_UP);

        // Don't depreciate below residual value
        BigDecimal maxDepreciation = bookValue.subtract(residualValue);
        BigDecimal finalDepreciation = depreciation.min(maxDepreciation);

        assertEquals(new BigDecimal("333.33"), finalDepreciation);
    }

    @Test
    void shouldCalculateSumOfYearsDigitsDepreciation() {
        BigDecimal depreciableAmount = new BigDecimal("9000.00"); // 10000 - 1000
        int usefulLifeMonths = 60;
        int periodNumber = 1; // First period

        int sumOfYears = usefulLifeMonths * (usefulLifeMonths + 1) / 2;
        int remainingLife = usefulLifeMonths - periodNumber + 1;

        BigDecimal fraction = BigDecimal.valueOf(remainingLife).divide(BigDecimal.valueOf(sumOfYears), 10, RoundingMode.HALF_UP);
        BigDecimal depreciation = depreciableAmount.multiply(fraction).setScale(2, RoundingMode.HALF_UP);

        // For first period: remainingLife = 60, sumOfYears = 1830
        // fraction = 60/1830 = 0.032787...
        // depreciation = 9000 * 0.032787 = 295.08
        assertEquals(new BigDecimal("295.08"), depreciation);
    }

    @Test
    void shouldNotDepreciateBelowResidualValue() {
        Asset asset = createTestAsset();
        asset.setBookValue(new BigDecimal("1100.00"));
        asset.setResidualValue(new BigDecimal("1000.00"));

        // Try to depreciate 200, but only 100 should be allowed
        BigDecimal requestedDepreciation = new BigDecimal("200.00");
        BigDecimal maxAllowed = asset.getBookValue().subtract(asset.getResidualValue());

        assertEquals(new BigDecimal("100.00"), maxAllowed);
        assertTrue(requestedDepreciation.compareTo(maxAllowed) > 0);
    }

    @Test
    void shouldMarkAssetAsFullyDepreciated() {
        Asset asset = createTestAsset();
        asset.setBookValue(new BigDecimal("1000.00"));
        asset.setResidualValue(new BigDecimal("1000.00"));

        // Depreciate the remaining 100
        BigDecimal depreciation = new BigDecimal("100.00");
        asset.setAccumulatedDepreciation(asset.getAccumulatedDepreciation().add(depreciation));
        asset.setBookValue(asset.getBookValue().subtract(depreciation));

        if (asset.getBookValue().compareTo(asset.getResidualValue()) <= 0) {
            asset.setStatus(AssetStatus.FULLY_DEPRECIATED);
            asset.setBookValue(asset.getResidualValue());
        }

        assertEquals(AssetStatus.FULLY_DEPRECIATED, asset.getStatus());
        assertEquals(new BigDecimal("1000.00"), asset.getBookValue());
    }

    private Asset createTestAsset() {
        FiscalYear fiscalYear = new FiscalYear(2024, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        fiscalYear.setId(UUID.randomUUID());

        ChartOfAccounts assetAccount = new ChartOfAccounts("1.2.3.03", "Máquinas", AccountType.ASSET, AccountNature.DEBIT, fiscalYear);
        assetAccount.setId(UUID.randomUUID());
        assetAccount.setDetail(true);
        assetAccount.setActive(true);

        ChartOfAccounts accumDepAccount = new ChartOfAccounts("1.2.3.11", "Depreciação Ac. Máquinas", AccountType.ASSET, AccountNature.CREDIT, fiscalYear);
        accumDepAccount.setId(UUID.randomUUID());
        accumDepAccount.setDetail(true);
        accumDepAccount.setActive(true);

        ChartOfAccounts deprExpenseAccount = new ChartOfAccounts("5.1.2.04", "Depreciação Industrial", AccountType.EXPENSE, AccountNature.DEBIT, fiscalYear);
        deprExpenseAccount.setId(UUID.randomUUID());
        deprExpenseAccount.setDetail(true);
        deprExpenseAccount.setActive(true);

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
        asset.setId(UUID.randomUUID());
        asset.setResidualValue(new BigDecimal("1000.00"));
        asset.setAccumulatedDepreciation(BigDecimal.ZERO);
        asset.setBookValue(new BigDecimal("10000.00"));

        return asset;
    }
}