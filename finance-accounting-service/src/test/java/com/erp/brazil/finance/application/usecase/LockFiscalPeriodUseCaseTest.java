package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.application.dto.FiscalPeriodDTO;
import com.erp.brazil.finance.application.dto.LockFiscalPeriodRequest;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.FiscalYear;
import com.erp.brazil.finance.domain.model.JournalEntryStatus;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import com.erp.brazil.finance.domain.repository.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LockFiscalPeriodUseCaseTest {

    @Mock
    private FiscalPeriodRepository fiscalPeriodRepository;
    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private LockFiscalPeriodUseCase useCase;

    private UUID fiscalPeriodId;
    private FiscalPeriod fiscalPeriod;

    @BeforeEach
    void setUp() {
        useCase = new LockFiscalPeriodUseCase(fiscalPeriodRepository, journalEntryRepository, rabbitTemplate);

        fiscalPeriodId = UUID.randomUUID();
        FiscalYear fiscalYear = new FiscalYear();
        fiscalYear.setYear(2024);

        fiscalPeriod = new FiscalPeriod();
        fiscalPeriod.setId(fiscalPeriodId);
        fiscalPeriod.setFiscalYear(fiscalYear);
        fiscalPeriod.setPeriodNumber(1);
        fiscalPeriod.setLocked(false);
        fiscalPeriod.setClosed(false);
    }

    @Test
    void shouldLockFiscalPeriodWhenNoDraftEntries() {
        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.of(fiscalPeriod));
        when(journalEntryRepository.countByFiscalPeriodAndStatus(any(), eq(JournalEntryStatus.DRAFT))).thenReturn(0L);
        when(fiscalPeriodRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LockFiscalPeriodRequest request = new LockFiscalPeriodRequest(LocalDate.of(2024, 1, 31), "user1");

        FiscalPeriodDTO result = useCase.execute(fiscalPeriodId, request, "user1");

        assertThat(result).isNotNull();
        assertThat(result.locked()).isTrue();
        assertThat(result.lockedAt()).isEqualTo(LocalDate.of(2024, 1, 31));
        assertThat(result.lockedBy()).isEqualTo("user1");

        verify(fiscalPeriodRepository).save(argThat(fp -> fp.isLocked()));
        verify(rabbitTemplate).convertAndSend(eq("finance.exchange"), eq("fiscal.period.locked"), any());
    }

    @Test
    void shouldThrowExceptionWhenPeriodAlreadyLocked() {
        fiscalPeriod.setLocked(true);
        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.of(fiscalPeriod));

        LockFiscalPeriodRequest request = new LockFiscalPeriodRequest(LocalDate.of(2024, 1, 31), "user1");

        assertThatThrownBy(() -> useCase.execute(fiscalPeriodId, request, "user1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already locked");
    }

    @Test
    void shouldThrowExceptionWhenPeriodClosed() {
        fiscalPeriod.setClosed(true);
        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.of(fiscalPeriod));

        LockFiscalPeriodRequest request = new LockFiscalPeriodRequest(LocalDate.of(2024, 1, 31), "user1");

        assertThatThrownBy(() -> useCase.execute(fiscalPeriodId, request, "user1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed fiscal period");
    }

    @Test
    void shouldThrowExceptionWhenDraftEntriesExist() {
        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.of(fiscalPeriod));
        when(journalEntryRepository.countByFiscalPeriodAndStatus(any(), eq(JournalEntryStatus.DRAFT))).thenReturn(5L);

        LockFiscalPeriodRequest request = new LockFiscalPeriodRequest(LocalDate.of(2024, 1, 31), "user1");

        assertThatThrownBy(() -> useCase.execute(fiscalPeriodId, request, "user1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("5 unposted");
    }

    @Test
    void shouldThrowExceptionWhenPeriodNotFound() {
        when(fiscalPeriodRepository.findById(fiscalPeriodId)).thenReturn(Optional.empty());

        LockFiscalPeriodRequest request = new LockFiscalPeriodRequest(LocalDate.of(2024, 1, 31), "user1");

        assertThatThrownBy(() -> useCase.execute(fiscalPeriodId, request, "user1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}