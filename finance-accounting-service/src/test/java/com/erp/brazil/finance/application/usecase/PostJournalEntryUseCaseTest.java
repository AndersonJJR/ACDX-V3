package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.application.dto.CreateJournalEntryRequest;
import com.erp.brazil.finance.application.dto.JournalEntryDTO;
import com.erp.brazil.finance.domain.model.*;
import com.erp.brazil.finance.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostJournalEntryUseCaseTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private JournalEntryLineRepository journalEntryLineRepository;
    @Mock
    private GLEntryRepository glEntryRepository;
    @Mock
    private FiscalPeriodRepository fiscalPeriodRepository;
    @Mock
    private ChartOfAccountsRepository chartOfAccountsRepository;
    @Mock
    private CostCenterRepository costCenterRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private PostJournalEntryUseCase useCase;

    private UUID fiscalPeriodId;
    private UUID accountId1;
    private UUID accountId2;
    private UUID costCenterId;
    private FiscalPeriod fiscalPeriod;
    private ChartOfAccounts account1;
    private ChartOfAccounts account2;
    private CostCenter costCenter;

    @BeforeEach
    void setUp() {
        useCase = new PostJournalEntryUseCase(
                journalEntryRepository, journalEntryLineRepository, glEntryRepository,
                fiscalPeriodRepository, chartOfAccountsRepository, costCenterRepository, rabbitTemplate
        );

        fiscalPeriodId = UUID.randomUUID();
        accountId1 = UUID.randomUUID();
        accountId2 = UUID.randomUUID();
        costCenterId = UUID.randomUUID();

        fiscalPeriod = new FiscalPeriod();
        fiscalPeriod.setId(fiscalPeriodId);
        fiscalPeriod.setLocked(false);
        fiscalPeriod.setClosed(false);

        FiscalYear fiscalYear = new FiscalYear();
        fiscalYear.setYear(2024);
        fiscalPeriod.setFiscalYear(fiscalYear);

        account1 = new ChartOfAccounts("1.1.1.01", "Caixa", AccountType.ASSET, AccountNature.DEBIT, fiscalYear);
        account1.setId(accountId1);

        account2 = new ChartOfAccounts("2.1.1.01", "Fornecedores", AccountType.LIABILITY, AccountNature.CREDIT, fiscalYear);
        account2.setId(accountId2);

        costCenter = new CostCenter("CC001", "Production", CostCenterType.PRODUCTION);
        costCenter.setId(costCenterId);
    }

    @Test
    void shouldPostValidJournalEntry() {
        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.of(fiscalPeriod));
        when(chartOfAccountsRepository.findById(accountId1)).thenReturn(Optional.of(account1));
        when(chartOfAccountsRepository.findById(accountId2)).thenReturn(Optional.of(account2));
        when(costCenterRepository.findById(costCenterId)).thenReturn(Optional.of(costCenter));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                "JE-001",
                LocalDate.of(2024, 1, 15),
                fiscalPeriodId,
                "Test entry",
                List.of(
                        new CreateJournalEntryRequest.CreateJournalEntryLineRequest(
                                accountId1, costCenterId, 1, "Debit line",
                                new BigDecimal("1000.00"), BigDecimal.ZERO),
                        new CreateJournalEntryRequest.CreateJournalEntryLineRequest(
                                accountId2, costCenterId, 2, "Credit line",
                                BigDecimal.ZERO, new BigDecimal("1000.00"))
                )
        );

        JournalEntryDTO result = useCase.execute(request, "user1");

        assertThat(result).isNotNull();
        assertThat(result.reference()).isEqualTo("JE-001");
        assertThat(result.status()).isEqualTo(JournalEntryStatus.POSTED);
        assertThat(result.totalDebit()).isEqualByComparingTo("1000.00");
        assertThat(result.totalCredit()).isEqualByComparingTo("1000.00");
        assertThat(result.isBalanced()).isTrue();
        assertThat(result.lines()).hasSize(2);

        verify(journalEntryRepository).save(any(JournalEntry.class));
        verify(rabbitTemplate).convertAndSend(eq("finance.exchange"), eq("journal.entry.posted"), any());
    }

    @Test
    void shouldThrowExceptionWhenEntryNotBalanced() {
        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.of(fiscalPeriod));
        when(chartOfAccountsRepository.findById(accountId1)).thenReturn(Optional.of(account1));
        when(chartOfAccountsRepository.findById(accountId2)).thenReturn(Optional.of(account2));

        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                "JE-002",
                LocalDate.of(2024, 1, 15),
                fiscalPeriodId,
                "Unbalanced entry",
                List.of(
                        new CreateJournalEntryRequest.CreateJournalEntryLineRequest(
                                accountId1, null, 1, "Debit line",
                                new BigDecimal("1000.00"), BigDecimal.ZERO),
                        new CreateJournalEntryRequest.CreateJournalEntryLineRequest(
                                accountId2, null, 2, "Credit line",
                                BigDecimal.ZERO, new BigDecimal("500.00"))
                )
        );

        assertThatThrownBy(() -> useCase.execute(request, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not balanced");
    }

    @Test
    void shouldThrowExceptionWhenLineHasBothDebitAndCredit() {
        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.of(fiscalPeriod));
        when(chartOfAccountsRepository.findById(accountId1)).thenReturn(Optional.of(account1));

        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                "JE-003",
                LocalDate.of(2024, 1, 15),
                fiscalPeriodId,
                "Invalid line",
                List.of(
                        new CreateJournalEntryRequest.CreateJournalEntryLineRequest(
                                accountId1, null, 1, "Invalid line",
                                new BigDecimal("100.00"), new BigDecimal("100.00"))
                )
        );

        assertThatThrownBy(() -> useCase.execute(request, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must have either debit or credit");
    }

    @Test
    void shouldThrowExceptionWhenFiscalPeriodLocked() {
        fiscalPeriod.setLocked(true);
        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.of(fiscalPeriod));

        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                "JE-004",
                LocalDate.of(2024, 1, 15),
                fiscalPeriodId,
                "Locked period",
                List.of(
                        new CreateJournalEntryRequest.CreateJournalEntryLineRequest(
                                accountId1, null, 1, "Debit",
                                new BigDecimal("100.00"), BigDecimal.ZERO),
                        new CreateJournalEntryRequest.CreateJournalEntryLineRequest(
                                accountId2, null, 2, "Credit",
                                BigDecimal.ZERO, new BigDecimal("100.00"))
                )
        );

        assertThatThrownBy(() -> useCase.execute(request, "user1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("locked");
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFound() {
        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.of(fiscalPeriod));
        when(chartOfAccountsRepository.findById(accountId1)).thenReturn(Optional.empty());

        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                "JE-005",
                LocalDate.of(2024, 1, 15),
                fiscalPeriodId,
                "Missing account",
                List.of(
                        new CreateJournalEntryRequest.CreateJournalEntryLineRequest(
                                accountId1, null, 1, "Debit",
                                new BigDecimal("100.00"), BigDecimal.ZERO),
                        new CreateJournalEntryRequest.CreateJournalEntryLineRequest(
                                accountId2, null, 2, "Credit",
                                BigDecimal.ZERO, new BigDecimal("100.00"))
                )
        );

        assertThatThrownBy(() -> useCase.execute(request, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account not found");
    }
}