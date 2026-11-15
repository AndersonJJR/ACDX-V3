package com.erp.brazil.finance.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ChartOfAccountsTest {

    @Test
    void shouldCreateChartOfAccountsWithValidData() {
        FiscalYear fiscalYear = new FiscalYear(2024, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        fiscalYear.setId(UUID.randomUUID());

        ChartOfAccounts account = new ChartOfAccounts(
                "1.1.1.01",
                "Caixa",
                AccountType.ASSET,
                AccountNature.DEBIT,
                fiscalYear
        );

        assertNotNull(account.getId());
        assertEquals("1.1.1.01", account.getCode());
        assertEquals("Caixa", account.getName());
        assertEquals(AccountType.ASSET, account.getAccountType());
        assertEquals(AccountNature.DEBIT, account.getAccountNature());
        assertTrue(account.isActive());
        assertTrue(account.isDetail());
        assertEquals(1, account.getLevel());
        assertEquals(fiscalYear, account.getFiscalYear());
        assertNotNull(account.getCreatedAt());
    }

    @Test
    void shouldCreateChildAccountWithCorrectLevel() {
        FiscalYear fiscalYear = new FiscalYear(2024, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        fiscalYear.setId(UUID.randomUUID());

        ChartOfAccounts parent = new ChartOfAccounts(
                "1.1",
                "Ativo Circulante",
                AccountType.ASSET,
                AccountNature.DEBIT,
                fiscalYear
        );
        parent.setLevel(2);

        ChartOfAccounts child = new ChartOfAccounts(
                "1.1.1",
                "Disponibilidades",
                AccountType.ASSET,
                AccountNature.DEBIT,
                fiscalYear
        );
        child.setParent(parent);
        child.setLevel(parent.getLevel() + 1);

        assertEquals(3, child.getLevel());
        assertEquals(parent, child.getParent());
        assertTrue(parent.getChildren().contains(child));
    }

    @Test
    void shouldUpdateTimestampOnPreUpdate() {
        FiscalYear fiscalYear = new FiscalYear(2024, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        fiscalYear.setId(UUID.randomUUID());

        ChartOfAccounts account = new ChartOfAccounts(
                "1.1.1.01",
                "Caixa",
                AccountType.ASSET,
                AccountNature.DEBIT,
                fiscalYear
        );

        LocalDate originalUpdatedAt = account.getUpdatedAt();
        account.setName("Caixa Geral");
        account.preUpdate();

        assertNotNull(account.getUpdatedAt());
        assertNotEquals(originalUpdatedAt, account.getUpdatedAt());
    }
}