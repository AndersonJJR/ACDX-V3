package com.erp.brazil.finance.presentation.rest;

import com.erp.brazil.finance.application.usecase.GenerateTrialBalanceUseCase;
import com.erp.brazil.finance.application.usecase.GenerateBalanceSheetUseCase;
import com.erp.brazil.finance.application.usecase.GeneratePLUseCase;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Financial Reports", description = "Financial reporting APIs")
public class ReportsController {

    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final GenerateTrialBalanceUseCase generateTrialBalanceUseCase;
    private final GenerateBalanceSheetUseCase generateBalanceSheetUseCase;
    private final GeneratePLUseCase generatePLUseCase;

    public ReportsController(FiscalPeriodRepository fiscalPeriodRepository,
                             GenerateTrialBalanceUseCase generateTrialBalanceUseCase,
                             GenerateBalanceSheetUseCase generateBalanceSheetUseCase,
                             GeneratePLUseCase generatePLUseCase) {
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.generateTrialBalanceUseCase = generateTrialBalanceUseCase;
        this.generateBalanceSheetUseCase = generateBalanceSheetUseCase;
        this.generatePLUseCase = generatePLUseCase;
    }

    @GetMapping("/trial-balance")
    @Operation(summary = "Generate Trial Balance")
    public ResponseEntity<GenerateTrialBalanceUseCase.Result> trialBalance(
            @Parameter(description = "Fiscal period ID") @RequestParam UUID fiscalPeriodId,
            @Parameter(description = "Include zero balance accounts") @RequestParam(defaultValue = "false") boolean includeZeroBalances) {
        var result = generateTrialBalanceUseCase.execute(fiscalPeriodId, includeZeroBalances);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/balance-sheet")
    @Operation(summary = "Generate Balance Sheet")
    public ResponseEntity<GenerateBalanceSheetUseCase.Result> balanceSheet(
            @Parameter(description = "Fiscal period ID") @RequestParam UUID fiscalPeriodId) {
        var result = generateBalanceSheetUseCase.execute(fiscalPeriodId);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/profit-loss")
    @Operation(summary = "Generate Profit & Loss Statement")
    public ResponseEntity<GeneratePLUseCase.Result> profitLoss(
            @Parameter(description = "Fiscal period ID") @RequestParam UUID fiscalPeriodId,
            @Parameter(description = "Year to date") @RequestParam(defaultValue = "false") boolean yearToDate) {
        var result = generatePLUseCase.execute(fiscalPeriodId, yearToDate);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }
}