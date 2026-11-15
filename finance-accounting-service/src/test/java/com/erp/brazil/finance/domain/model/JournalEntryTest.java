package com.erp.brazil.finance.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JournalEntryTest {

    @Test
    void shouldCreateBalancedJournalEntry() {
        FiscalYear fiscalYear = new FiscalYear(2024, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        fiscalYear.setId(UUID.randomUUID());

        FiscalPeriod fiscalPeriod = new FiscalPeriod(fiscalYear, 1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        fiscalPeriod.setId(UUID.randomUUID());

        ChartOfAccounts cashAccount = new ChartOfAccounts("1.1.1.01", "Caixa", AccountType.ASSET, AccountNature.DEBIT, fiscalYear);
        cashAccount.setId(UUID.randomUUID());
        cashAccount.setDetail(true);
        cashAccount.setActive(true);

        ChartOfAccounts revenueAccount = new ChartOfAccounts("4.1.1.01", "Vendas", AccountType.REVENUE, AccountNature.CREDIT, fiscalYear);
        revenueAccount.setId(UUID.randomUUID());
        revenueAccount.setDetail(true);
        revenueAccount.setActive(true);

        JournalEntry entry = new JournalEntry("JE-001", LocalDate.of(2024, 1, 15), fiscalPeriod, "Venda à vista");
        entry.addLine(new JournalEntryLine(entry, cashAccount, 1, new BigDecimal("1000.00"), BigDecimal.ZERO));
        entry.addLine(new JournalEntryLine(entry, revenueAccount, 2, BigDecimal.ZERO, new BigDecimal("1000.00")));

        assertTrue(entry.isBalanced());
        assertEquals(new BigDecimal("1000.00"), entry.getTotalDebit());
        assertEquals(new BigDecimal("1000.00"), entry.getTotalCredit());
    }

    @Test
    void shouldDetectUnbalancedJournalEntry() {
        FiscalYear fiscalYear = new FiscalYear(2024, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        fiscalYear.setId(UUID.randomUUID());

        FiscalPeriod fiscalPeriod = new FiscalPeriod(fiscalYear, 1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        fiscalPeriod.setId(UUID.randomUUID());

        ChartOfAccounts cashAccount = new ChartOfAccounts("1.1.1.01", "Caixa", AccountType.ASSET, AccountNature.DEBIT, fiscalYear);
        cashAccount.setId(UUID.randomUUID());
        cashAccount.setDetail(true);
        cashAccount.setActive(true);

        ChartOfAccounts revenueAccount = new ChartOfAccounts("4.1.1.01", "Vendas", AccountType.REVENUE, AccountNature.CREDIT, fiscalYear);
        revenueAccount.setId(UUID.randomUUID());
        revenueAccount.setDetail(true);
        revenueAccount.setActive(true);

        JournalEntry entry = new JournalEntry("JE-002", LocalDate.of(2024, 1, 15), fiscalPeriod, "Venda incorreta");
        entry.addLine(new JournalEntryLine(entry, cashAccount, 1, new BigDecimal("1000.00"), BigDecimal.ZERO));
        entry.addLine(new JournalEntryLine(entry, revenueAccount, 2, BigDecimal.ZERO, new BigDecimal("500.00")));

        assertFalse(entry.isBalanced());
        assertEquals(new BigDecimal("1000.00"), entry.getTotalDebit());
        assertEquals(new BigDecimal("500.00"), entry.getTotalCredit());
    }

    @Test
    void shouldRecalculateTotalsWhenLineRemoved() {
        FiscalYear fiscalYear = new FiscalYear(2024, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        fiscalYear.setId(UUID.randomUUID());

        FiscalPeriod fiscalPeriod = new FiscalPeriod(fiscalYear, 1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        fiscalPeriod.setId(UUID.randomUUID());

        ChartOfAccounts cashAccount = new ChartOfAccounts("1.1.1.01", "Caixa", AccountType.ASSET, AccountNature.DEBIT, fiscalYear);
        cashAccount.setId(UUID.randomUUID());
        cashAccount.setDetail(true);
        cashAccount.setActive(true);

        ChartOfAccounts revenueAccount = new ChartOfAccounts("4.1.1.01", "Vendas", AccountType.REVENUE, AccountNature.CREDIT, fiscalYear);
        revenueAccount.setId(UUID.randomUUID());
        revenueAccount.setDetail(true);
        revenueAccount.setActive(true);

        JournalEntry entry = new JournalEntry("JE-003", LocalDate.of(2024, 1, 15), fiscalPeriod, "Teste remoção");
        
        JournalEntryLine line1 = new JournalEntryLine(entry, cashAccount, 1, new BigDecimal("1000.00"), BigDecimal.ZERO);
        JournalEntryLine line2 = new JournalEntryLine(entry, revenueAccount, 2, BigDecimal.ZERO, new BigDecimal("1000.00"));
        
        entry.addLine(line1);
        entry.addLine(line2);

        assertTrue(entry.isBalanced());

        entry.removeLine(line1);

        assertEquals(BigDecimal.ZERO, entry.getTotalDebit());
        assertEquals(new BigDecimal("1000.00"), entry.getTotalCredit());
        assertFalse(entry.isBalanced());
    }

    @Test
    void shouldSetDefaultStatusToDraft() {
        FiscalYear fiscalYear = new FiscalYear(2024, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        fiscalYear.setId(UUID.randomUUID());

        FiscalPeriod fiscalPeriod = new FiscalPeriod(fiscalYear, 1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        fiscalPeriod.setId(UUID.randomUUID());

        JournalEntry entry = new JournalEntry("JE-004", LocalDate.of(2024, 1, 15), fiscalPeriod, "Teste status");

        assertEquals(JournalEntryStatus.DRAFT, entry.getStatus());
        assertFalse(entry.isReversed());
    }
}