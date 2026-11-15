package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.application.dto.JournalEntryDTO;
import com.erp.brazil.finance.application.dto.ReverseJournalEntryRequest;
import com.erp.brazil.finance.application.event.JournalEntryReversedEvent;
import com.erp.brazil.finance.domain.model.*;
import com.erp.brazil.finance.domain.repository.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReverseJournalEntryUseCase {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final GLEntryRepository glEntryRepository;
    private final ReversalEntryRepository reversalEntryRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final CostCenterRepository costCenterRepository;
    private final RabbitTemplate rabbitTemplate;

    public ReverseJournalEntryUseCase(JournalEntryRepository journalEntryRepository,
                                      JournalEntryLineRepository journalEntryLineRepository,
                                      GLEntryRepository glEntryRepository,
                                      ReversalEntryRepository reversalEntryRepository,
                                      FiscalPeriodRepository fiscalPeriodRepository,
                                      ChartOfAccountsRepository chartOfAccountsRepository,
                                      CostCenterRepository costCenterRepository,
                                      RabbitTemplate rabbitTemplate) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.glEntryRepository = glEntryRepository;
        this.reversalEntryRepository = reversalEntryRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.chartOfAccountsRepository = chartOfAccountsRepository;
        this.costCenterRepository = costCenterRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public JournalEntryDTO execute(UUID originalEntryId, ReverseJournalEntryRequest request, String reversedBy) {
        JournalEntry originalEntry = journalEntryRepository.findById(originalEntryId)
                .orElseThrow(() -> new IllegalArgumentException("Original journal entry not found: " + originalEntryId));

        if (originalEntry.getStatus() != JournalEntryStatus.POSTED) {
            throw new IllegalStateException("Can only reverse posted journal entries");
        }

        if (originalEntry.isReversed()) {
            throw new IllegalStateException("Journal entry has already been reversed");
        }

        FiscalPeriod reversalPeriod = fiscalPeriodRepository.findByFiscalYearAndDate(
                originalEntry.getFiscalPeriod().getFiscalYear(), request.reversalDate())
                .orElseThrow(() -> new IllegalArgumentException("No fiscal period found for reversal date: " + request.reversalDate()));

        if (reversalPeriod.isLocked()) {
            throw new IllegalStateException("Cannot reverse entry in locked fiscal period");
        }
        if (reversalPeriod.isClosed()) {
            throw new IllegalStateException("Cannot reverse entry in closed fiscal period");
        }

        JournalEntry reversalEntry = new JournalEntry(
                "REV-" + originalEntry.getReference(),
                request.reversalDate(),
                reversalPeriod,
                "Reversal of " + originalEntry.getReference() + ": " + request.reason()
        );

        List<JournalEntryLine> reversalLines = new ArrayList<>();
        List<GLEntry> reversalGLEntries = new ArrayList<>();

        for (JournalEntryLine originalLine : originalEntry.getLines()) {
            JournalEntryLine reversalLine = new JournalEntryLine(
                    reversalEntry,
                    originalLine.getAccount(),
                    originalLine.getLineNumber(),
                    originalLine.getCreditAmount(),
                    originalLine.getDebitAmount()
            );
            reversalLine.setDescription("REV: " + originalLine.getDescription());
            reversalLine.setCostCenter(originalLine.getCostCenter());
            reversalLines.add(reversalLine);

            if (originalLine.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                GLEntry glEntry = new GLEntry(
                        reversalEntry,
                        originalLine.getAccount(),
                        reversalPeriod,
                        request.reversalDate(),
                        EntryType.DEBIT,
                        originalLine.getCreditAmount()
                );
                glEntry.setCostCenter(originalLine.getCostCenter());
                glEntry.setDescription("REV: " + originalLine.getDescription());
                reversalGLEntries.add(glEntry);
            }

            if (originalLine.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
                GLEntry glEntry = new GLEntry(
                        reversalEntry,
                        originalLine.getAccount(),
                        reversalPeriod,
                        request.reversalDate(),
                        EntryType.CREDIT,
                        originalLine.getDebitAmount()
                );
                glEntry.setCostCenter(originalLine.getCostCenter());
                glEntry.setDescription("REV: " + originalLine.getDescription());
                reversalGLEntries.add(glEntry);
            }
        }

        reversalEntry.setLines(reversalLines);
        reversalEntry.setGlEntries(reversalGLEntries);
        reversalEntry.setStatus(JournalEntryStatus.POSTED);
        reversalEntry.setPostedAt(LocalDateTime.now());
        reversalEntry.setPostedBy(reversedBy);
        reversalEntry.setReversed(true);
        reversalEntry.setReversedEntry(originalEntry);
        reversalEntry.setReversalReason(request.reason());

        originalEntry.setReversed(true);
        originalEntry.setReversedEntry(reversalEntry);
        originalEntry.setReversalReason(request.reason());
        originalEntry.setStatus(JournalEntryStatus.REVERSED);

        journalEntryRepository.save(reversalEntry);
        journalEntryRepository.save(originalEntry);

        ReversalEntry reversalRecord = new ReversalEntry(
                originalEntry,
                reversalEntry,
                request.reversalDate(),
                request.reason(),
                reversedBy
        );
        reversalEntryRepository.save(reversalRecord);

        calculateRunningBalances(reversalGLEntries);

        publishEvent(originalEntry, reversalEntry, request, reversedBy);

        return toDTO(reversalEntry);
    }

    private void calculateRunningBalances(List<GLEntry> glEntries) {
        glEntries.sort((a, b) -> {
            int dateCompare = a.getEntryDate().compareTo(b.getEntryDate());
            if (dateCompare != 0) return dateCompare;
            return a.getCreatedAt().compareTo(b.getCreatedAt());
        });

        for (GLEntry entry : glEntries) {
            ChartOfAccounts account = entry.getAccount();
            FiscalPeriod period = entry.getFiscalPeriod();

            BigDecimal lastBalance = glEntryRepository.findFirstByAccountAndFiscalPeriodOrderByEntryDateDescCreatedAtDesc(account, period)
                    .map(GLEntry::getRunningBalance)
                    .orElse(BigDecimal.ZERO);

            BigDecimal newBalance;
            if (entry.getEntryType() == EntryType.DEBIT) {
                newBalance = lastBalance.add(entry.getAmount());
            } else {
                newBalance = lastBalance.subtract(entry.getAmount());
            }
            entry.setRunningBalance(newBalance);
        }
    }

    private void publishEvent(JournalEntry originalEntry, JournalEntry reversalEntry,
                              ReverseJournalEntryRequest request, String reversedBy) {
        JournalEntryReversedEvent event = new JournalEntryReversedEvent(
                UUID.randomUUID(),
                originalEntry.getId(),
                reversalEntry.getId(),
                originalEntry.getReference(),
                request.reversalDate(),
                request.reason(),
                reversedBy,
                LocalDateTime.now()
        );

        rabbitTemplate.convertAndSend("finance.exchange", "journal.entry.reversed", event);
    }

    private JournalEntryDTO toDTO(JournalEntry journalEntry) {
        List<JournalEntryDTO.JournalEntryLineDTO> lineDTOs = journalEntry.getLines().stream()
                .map(line -> new JournalEntryDTO.JournalEntryLineDTO(
                        line.getId(),
                        line.getAccount().getId(),
                        line.getCostCenter() != null ? line.getCostCenter().getId() : null,
                        line.getLineNumber(),
                        line.getDescription(),
                        line.getDebitAmount(),
                        line.getCreditAmount()
                ))
                .collect(Collectors.toList());

        return new JournalEntryDTO(
                journalEntry.getId(),
                journalEntry.getReference(),
                journalEntry.getEntryDate(),
                journalEntry.getFiscalPeriod().getId(),
                journalEntry.getDescription(),
                journalEntry.getStatus(),
                journalEntry.getTotalDebit(),
                journalEntry.getTotalCredit(),
                journalEntry.isReversed(),
                journalEntry.getReversedEntry() != null ? journalEntry.getReversedEntry().getId() : null,
                journalEntry.getReversalReason(),
                lineDTOs
        );
    }
}