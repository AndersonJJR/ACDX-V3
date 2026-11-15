package com.erp.brazil.finance.application.usecase;

import com.erp.brazil.finance.application.dto.DepreciationScheduleDTO;
import com.erp.brazil.finance.application.dto.RunDepreciationRequest;
import com.erp.brazil.finance.application.event.AssetDepreciationPostedEvent;
import com.erp.brazil.finance.domain.model.*;
import com.erp.brazil.finance.domain.repository.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RunDepreciationUseCase {

    private final AssetRepository assetRepository;
    private final AssetDepreciationScheduleRepository scheduleRepository;
    private final DepreciationEntryRepository depreciationEntryRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final GLEntryRepository glEntryRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final RabbitTemplate rabbitTemplate;

    public RunDepreciationUseCase(AssetRepository assetRepository,
                                  AssetDepreciationScheduleRepository scheduleRepository,
                                  DepreciationEntryRepository depreciationEntryRepository,
                                  JournalEntryRepository journalEntryRepository,
                                  JournalEntryLineRepository journalEntryLineRepository,
                                  GLEntryRepository glEntryRepository,
                                  FiscalPeriodRepository fiscalPeriodRepository,
                                  ChartOfAccountsRepository chartOfAccountsRepository,
                                  RabbitTemplate rabbitTemplate) {
        this.assetRepository = assetRepository;
        this.scheduleRepository = scheduleRepository;
        this.depreciationEntryRepository = depreciationEntryRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.glEntryRepository = glEntryRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.chartOfAccountsRepository = chartOfAccountsRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<DepreciationScheduleDTO> execute(RunDepreciationRequest request, String postedBy) {
        LocalDate periodEndDate = request.periodEndDate();

        List<AssetDepreciationSchedule> dueSchedules = scheduleRepository.findDueSchedules(periodEndDate);

        if (dueSchedules.isEmpty()) {
            return List.of();
        }

        List<AssetDepreciationSchedule> processedSchedules = new ArrayList<>();
        List<AssetDepreciationPostedEvent.AssetDepreciationLineEvent> depreciationLines = new ArrayList<>();

        for (AssetDepreciationSchedule schedule : dueSchedules) {
            if (schedule.isPosted()) continue;

            Asset asset = schedule.getAsset();
            if (asset.getStatus() != AssetStatus.ACTIVE && asset.getStatus() != AssetStatus.UNDER_MAINTENANCE) {
                continue;
            }

            FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findByFiscalYearAndDate(
                    asset.getAccount().getFiscalYear(), periodEndDate)
                    .orElseThrow(() -> new IllegalStateException("No fiscal period found for date: " + periodEndDate));

            if (fiscalPeriod.isLocked() || fiscalPeriod.isClosed()) {
                continue;
            }

            JournalEntry journalEntry = createDepreciationJournalEntry(asset, schedule, fiscalPeriod, postedBy);
            journalEntryRepository.save(journalEntry);

            DepreciationEntry depreciationEntry = new DepreciationEntry(asset, schedule, schedule.getDepreciationAmount(),
                    schedule.getPeriodStart(), schedule.getPeriodEnd());
            depreciationEntry.setJournalEntry(journalEntry);
            depreciationEntryRepository.save(depreciationEntry);

            schedule.setPosted(true);
            schedule.setPostedAt(LocalDateTime.now());
            scheduleRepository.save(schedule);

            asset.setAccumulatedDepreciation(schedule.getAccumulatedDepreciation());
            asset.setBookValue(schedule.getBookValue());
            asset.setLastDepreciationDate(schedule.getPeriodEnd());
            if (asset.getBookValue().compareTo(asset.getResidualValue()) <= 0) {
                asset.setStatus(AssetStatus.FULLY_DEPRECIATED);
            }
            assetRepository.save(asset);

            processedSchedules.add(schedule);

            depreciationLines.add(new AssetDepreciationPostedEvent.AssetDepreciationLineEvent(
                    asset.getId(),
                    asset.getCode(),
                    asset.getName(),
                    schedule.getId(),
                    schedule.getPeriodStart(),
                    schedule.getPeriodEnd(),
                    schedule.getDepreciationAmount()
            ));
        }

        if (!depreciationLines.isEmpty()) {
            BigDecimal totalAmount = depreciationLines.stream()
                    .map(AssetDepreciationPostedEvent.AssetDepreciationLineEvent::amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            AssetDepreciationPostedEvent event = new AssetDepreciationPostedEvent(
                    UUID.randomUUID(),
                    processedSchedules.get(0).getAsset().getAccount().getId(), // reference
                    "DEPR-" + periodEndDate,
                    periodEndDate,
                    processedSchedules.get(0).getAsset().getAccount().getFiscalYear().getPeriods().get(0).getId(),
                    totalAmount,
                    depreciationLines,
                    postedBy,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            rabbitTemplate.convertAndSend("finance.exchange", "asset.depreciation.posted", event);
        }

        return processedSchedules.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private JournalEntry createDepreciationJournalEntry(Asset asset, AssetDepreciationSchedule schedule,
                                                        FiscalPeriod fiscalPeriod, String postedBy) {
        JournalEntry journalEntry = new JournalEntry(
                "DEPR-" + asset.getCode() + "-" + schedule.getPeriodStart(),
                schedule.getPeriodEnd(),
                fiscalPeriod,
                "Depreciation for asset " + asset.getCode() + " (" + asset.getName() + ")"
        );

        List<JournalEntryLine> lines = new ArrayList<>();
        List<GLEntry> glEntries = new ArrayList<>();

        JournalEntryLine expenseLine = new JournalEntryLine(
                journalEntry,
                asset.getDepreciationExpenseAccount(),
                1,
                schedule.getDepreciationAmount(),
                BigDecimal.ZERO
        );
        expenseLine.setDescription("Depreciation expense - " + asset.getCode());
        expenseLine.setCostCenter(asset.getCostCenter());
        lines.add(expenseLine);

        GLEntry expenseGL = new GLEntry(
                journalEntry,
                asset.getDepreciationExpenseAccount(),
                fiscalPeriod,
                schedule.getPeriodEnd(),
                EntryType.DEBIT,
                schedule.getDepreciationAmount()
        );
        expenseGL.setCostCenter(asset.getCostCenter());
        expenseGL.setDescription("Depreciation expense - " + asset.getCode());
        glEntries.add(expenseGL);

        JournalEntryLine accumLine = new JournalEntryLine(
                journalEntry,
                asset.getAccumulatedDepreciationAccount(),
                2,
                BigDecimal.ZERO,
                schedule.getDepreciationAmount()
        );
        accumLine.setDescription("Accumulated depreciation - " + asset.getCode());
        accumLine.setCostCenter(asset.getCostCenter());
        lines.add(accumLine);

        GLEntry accumGL = new GLEntry(
                journalEntry,
                asset.getAccumulatedDepreciationAccount(),
                fiscalPeriod,
                schedule.getPeriodEnd(),
                EntryType.CREDIT,
                schedule.getDepreciationAmount()
        );
        accumGL.setCostCenter(asset.getCostCenter());
        accumGL.setDescription("Accumulated depreciation - " + asset.getCode());
        glEntries.add(accumGL);

        journalEntry.setLines(lines);
        journalEntry.setGlEntries(glEntries);
        journalEntry.setStatus(JournalEntryStatus.POSTED);
        journalEntry.setPostedAt(LocalDateTime.now());
        journalEntry.setPostedBy(postedBy);

        return journalEntry;
    }

    private DepreciationScheduleDTO toDTO(AssetDepreciationSchedule schedule) {
        return new DepreciationScheduleDTO(
                schedule.getId(),
                schedule.getAsset().getId(),
                schedule.getPeriodStart(),
                schedule.getPeriodEnd(),
                schedule.getDepreciationAmount(),
                schedule.getAccumulatedDepreciation(),
                schedule.getBookValue(),
                schedule.isPosted(),
                schedule.getPostedAt()
        );
    }
}