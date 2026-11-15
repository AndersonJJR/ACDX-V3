package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.application.dto.AccountBalanceDTO;
import com.erp.brazil.finance.application.dto.TrialBalanceRequest;
import com.erp.brazil.finance.application.dto.TrialBalanceResponse;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.FiscalYear;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import com.erp.brazil.finance.domain.repository.GLEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateTrialBalanceUseCaseTest {

    @Mock
    private GLEntryRepository glEntryRepository;
    @Mock
    private FiscalPeriodRepository fiscalPeriodRepository;

    private GenerateTrialBalanceUseCase useCase;

    private UUID fiscalPeriodId;
    private FiscalPeriod fiscalPeriod;

    @BeforeEach
    void setUp() {
        useCase = new GenerateTrialBalanceUseCase(glEntryRepository, fiscalPeriodRepository);

        fiscalPeriodId = UUID.randomUUID();
        FiscalYear fiscalYear = new FiscalYear();
        fiscalYear.setYear(2024);

        fiscalPeriod = new FiscalPeriod();
        fiscalPeriod.setId(fiscalPeriodId);
        fiscalPeriod.setFiscalYear(fiscalYear);
        fiscalPeriod.setEndDate(LocalDate.of(2024, 12, 31));
    }

    @Test
    void shouldGenerateTrialBalance() {
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();

        AccountBalanceDTO account1 = new AccountBalanceDTO(
                accountId1, "1.1.1.01", "Caixa",
                new BigDecimal("10000.00"), BigDecimal.ZERO);
        AccountBalanceDTO account2 = new AccountBalanceDTO(
                accountId2, "2.1.1.01", "Fornecedores",
                BigDecimal.ZERO, new BigDecimal("10000.00"));

        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.of(fiscalPeriod));
        when(glEntryRepository.getTrialBalance(fiscalPeriod)).thenReturn(List.of(account1, account2));

        TrialBalanceRequest request = new TrialBalanceRequest(fiscalPeriodId, null, null, false);
        TrialBalanceResponse response = useCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.fiscalPeriodId()).isEqualTo(fiscalPeriodId);
        assertThat(response.accounts()).hasSize(2);
        assertThat(response.totalDebits()).isEqualByComparingTo("10000.00");
        assertThat(response.totalCredits()).isEqualByComparingTo("10000.00");
        assertThat(response.balanced()).isTrue();
    }

    @Test
    void shouldFilterZeroBalancesWhenRequested() {
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();
        UUID accountId3 = UUID.randomUUID();

        AccountBalanceDTO account1 = new AccountBalanceDTO(
                accountId1, "1.1.1.01", "Caixa",
                new BigDecimal("10000.00"), BigDecimal.ZERO);
        AccountBalanceDTO account2 = new AccountBalanceDTO(
                accountId2, "2.1.1.01", "Fornecedores",
                BigDecimal.ZERO, new BigDecimal("10000.00"));
        AccountBalanceDTO account3 = new AccountBalanceDTO(
                accountId3, "3.1.1.01", "Capital Social",
                BigDecimal.ZERO, BigDecimal.ZERO);

        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.of(fiscalPeriod));
        when(glEntryRepository.getTrialBalance(fiscalPeriod)).thenReturn(List.of(account1, account2, account3));

        TrialBalanceRequest request = new TrialBalanceRequest(fiscalPeriodId, null, null, false);
        TrialBalanceResponse response = useCase.execute(request);

        assertThat(response.accounts()).hasSize(2);
        assertThat(response.accounts()).noneMatch(a -> a.code().equals("3.1.1.01"));
    }

    @Test
    void shouldIncludeZeroBalancesWhenRequested() {
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();
        UUID accountId3 = UUID.randomUUID();

        AccountBalanceDTO account1 = new AccountBalanceDTO(
                accountId1, "1.1.1.01", "Caixa",
                new BigDecimal("10000.00"), BigDecimal.ZERO);
        AccountBalanceDTO account2 = new AccountBalanceDTO(
                accountId2, "2.1.1.01", "Fornecedores",
                BigDecimal.ZERO, new BigDecimal("10000.00"));
        AccountBalanceDTO account3 = new AccountBalanceDTO(
                accountId3, "3.1.1.01", "Capital Social",
                BigDecimal.ZERO, BigDecimal.ZERO);

        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.of(fiscalPeriod));
        when(glEntryRepository.getTrialBalance(fiscalPeriod)).thenReturn(List.of(account1, account2, account3));

        TrialBalanceRequest request = new TrialBalanceRequest(fiscalPeriodId, null, null, true);
        TrialBalanceResponse response = useCase.execute(request);

        assertThat(response.accounts()).hasSize(3);
        assertThat(response.accounts()).anyMatch(a -> a.code().equals("3.1.1.01"));
    }

    @Test
    void shouldThrowExceptionWhenFiscalPeriodNotFound() {
        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.empty());

        TrialBalanceRequest request = new TrialBalanceRequest(fiscalPeriodId, null, null, false);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}