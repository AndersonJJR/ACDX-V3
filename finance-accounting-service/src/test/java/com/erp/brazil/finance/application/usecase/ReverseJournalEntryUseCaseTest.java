package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.application.dto.JournalEntryDTO;
import com.erp.brazil.finance.application.dto.ReverseJournalEntryRequest;
import com.erp.brazil.finance.domain.model.*;
import com.erp.brazil.finance.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ReverseJournalEntryUseCaseTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private JournalEntryLineRepository journalEntryLineRepository;
    @Mock
    private GLEntryRepository glEntryRepository;
    @Mock
    private ReversalEntryRepository reversalEntryRepository;
    @Mock
    private FiscalPeriodRepository fiscalPeriodRepository;
    @Mock
    private ChartOfAccountsRepository chartOfAccountsRepository;
    @Mock
    private CostCenterRepository costCenterRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private ReverseJournalEntryUseCase useCase;

    private UUID originalEntryId;
    private UUID fiscalPeriodId;
    private UUID reversalPeriodId;
    private UUID accountId1;
    private UUID accountId2;
    private JournalEntry originalEntry;
    private FiscalPeriod originalPeriod;
    private FiscalPeriod reversalPeriod;
    private ChartOfAccounts account1;
    private ChartOfAccounts account2;

    @BeforeEach
    void setUp() {
        useCase = new ReverseJournalEntryUseCase(
                journalEntryRepository, journalEntryLineRepository, glEntryRepository,
                reversalEntryRepository, fiscalPeriodRepository, chartOfAccountsRepository,
                costCenterRepository, rabbitTemplate
        );

        originalEntryId = UUID.randomUUID();
        fiscalPeriodId = UUID.randomUUID();
        reversalPeriodId = UUID.randomUUID();
        accountId1 = UUID.randomUUID();
        accountId2 = UUID.randomUUID();

        FiscalYear fiscalYear = new FiscalYear();
        fiscalYear.setYear(2024);

        originalPeriod = new FiscalPeriod();
        originalPeriod.setId(fiscalPeriodId);
        originalPeriod.setFiscalYear(fiscalYear);
        originalPeriod.setLocked(false);
        originalPeriod.setClosed(false);
        originalPeriod.setPeriodNumber(1);
        originalPeriod.setStartDate(LocalDate.of(2024, 1, 1));
        originalPeriod.setEndDate(LocalDate.of(2024, 1, 31));

        reversalPeriod = new FiscalPeriod();
        reversalPeriod.setId(reversalPeriodId);
        reversalPeriod.setFiscalYear(fiscalYear);
        reversalPeriod.setLocked(false);
        reversalPeriod.setClosed(false);
        reversalPeriod.setPeriodNumber(2);
        reversalPeriod.setStartDate(LocalDate.of(2024, 2, 1));
        reversalPeriod.setEndDate(LocalDate.of(2024, 2, 29));

        account1 = new ChartOfAccounts("1.1.1.01", "Caixa", AccountType.ASSET, AccountNature.DEBIT, fiscalYear);
        account1.setId(accountId1);

        account2 = new ChartOfAccounts("2.1.1.01", "Fornecedores", AccountType.LIABILITY, AccountNature.CREDIT, fiscalYear);
        account2.setId(accountId2);

        originalEntry = new JournalEntry("JE-001", LocalDate.of(2024, 1, 15), originalPeriod, "Original entry");
        originalEntry.setId(originalEntryId);
        originalEntry.setStatus(JournalEntryStatus.POSTED);
        originalEntry.setPostedAt(LocalDateTime.of(2024, 1, 15, 10, 0));
        originalEntry.setPostedBy("user1");

        JournalEntryLine line1 = new JournalEntryLine(originalEntry, account1, 1, new BigDecimal("1000.00"), BigDecimal.ZERO);
        line1.setDescription("Debit line");
        JournalEntryLine line2 = new JournalEntryLine(originalEntry, account2, 2, BigDecimal.ZERO, new BigDecimal("1000.00"));
        line2.setDescription("Credit line");

        List<JournalEntryLine> lines = new ArrayList<>();
        lines.add(line1);
        lines.add(line2);
        originalEntry.setLines(lines);

        List<GLEntry> glEntries = new ArrayList<>();
        GLEntry gl1 = new GLEntry(originalEntry, account1, originalPeriod, LocalDate.of(2024, 1, 15), EntryType.DEBIT, new BigDecimal("1000.00"));
        GLEntry gl2 = new GLEntry(originalEntry, account2, originalPeriod, LocalDate.of(2024, 1, 15), EntryType.CREDIT, new BigDecimal("1000.00"));
        glEntries.add(gl1);
        glEntries.add(gl2);
        originalEntry.setGlEntries(glEntries);
    }

    @Test
    void shouldReversePostedJournalEntry() {
        when(journalEntryRepository.findById(originalEntryId)).thenReturn(Optional.of(originalEntry));
        when(fiscalPeriodRepository.findByFiscalYearAndDate(any(), any())).thenReturn(Optional.of(reversalPeriod));
        when(chartOfAccountsRepository.findById(accountId1)).thenReturn(Optional.of(account1));
        when(chartOfAccountsRepository.findById(accountId2)).thenReturn(Optional.of(account2));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reversalEntryRepository.save(any(ReversalEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        ReverseJournalEntryRequest request = new ReverseJournalEntryRequest(
                LocalDate.of(2024, 2, 15),
                "Error correction",
                "user2"
        );

        JournalEntryDTO result = useCase.execute(originalEntryId, request, "user2");

        assertThat(result).isNotNull();
        assertThat(result.reference()).startsWith("REV-");
        assertThat(result.status()).isEqualTo(JournalEntryStatus.POSTED);
        assertThat(result.reversed()).isTrue();
        assertThat(result.reversalReason()).isEqualTo("Error correction");
        assertThat(result.totalDebit()).isEqualByComparingTo("1000.00");
        assertThat(result.totalCredit()).isEqualByComparingTo("1000.00");

        verify(journalEntryRepository).save(argThat(je -> je.getReference().equals("JE-001") && je.isReversed()));
        verify(reversalEntryRepository).save(any(ReversalEntry.class));
        verify(rabbitTemplate).convertAndSend(eq("finance.exchange"), eq("journal.entry.reversed"), any());
    }

    @Test
    void shouldThrowExceptionWhenEntryNotPosted() {
        originalEntry.setStatus(JournalEntryStatus.DRAFT);
        when(journalEntryRepository.findById(originalEntryId)).thenReturn(Optional.of(originalEntry));

        ReverseJournalEntryRequest request = new ReverseJournalEntryRequest(
                LocalDate.of(2024, 2, 15),
                "Error correction",
                "user2"
        );

        assertThatThrownBy(() -> useCase.execute(originalEntryId, request, "user2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only reverse posted");
    }

    @Test
    void shouldThrowExceptionWhenAlreadyReversed() {
        originalEntry.setReversed(true);
        when(journalEntryRepository.findById(originalEntryId)).thenReturn(Optional.of(originalEntry));

        ReverseJournalEntryRequest request = new ReverseJournalEntryRequest(
                LocalDate.of(2024, 2, 15),
                "Error correction",
                "user2"
        );

        assertThatThrownBy(() -> useCase.execute(originalEntryId, request, "user2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been reversed");
    }

    @Test
    void shouldThrowExceptionWhenReversalPeriodLocked() {
        reversalPeriod.setLocked(true);
        when(journalEntryRepository.findById(originalEntryId)).thenReturn(Optional.of(originalEntry));
        when(fiscalPeriodRepository.findByFiscalYearAndDate(any(), any())).thenReturn(Optional.of(reversalPeriod));

        ReverseJournalEntryRequest request = new ReverseJournalEntryRequest(
                LocalDate.of(2024, 2, 15),
                "Error correction",
                "user2"
        );

        assertThatThrownBy(() -> useCase.execute(originalEntryId, request, "user2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("locked");
    }

    @Test
    void shouldThrowExceptionWhenOriginalEntryNotFound() {
        when(journalEntryRepository.findById(originalEntryId)).thenReturn(Optional.empty());

        ReverseJournalEntryRequest request = new ReverseJournalEntryRequest(
                LocalDate.of(2024, 2, 15),
                "Error correction",
                "user2"
        );

        assertThatThrownBy(() -> useCase.execute(originalEntryId, request, "user2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}