package com.erp.brazil.finance.presentation.rest;

import com.erp.brazil.finance.application.usecase.GenerateBalanceSheetUseCase;
import com.erp.brazil.finance.application.usecase.GeneratePLUseCase;
import com.erp.brazil.finance.application.usecase.GenerateTrialBalanceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Financial Reports", description = "Financial report generation APIs")
public class ReportController {

    private final GenerateTrialBalanceUseCase trialBalanceUseCase;
    private final GenerateBalanceSheetUseCase balanceSheetUseCase;
    private final GeneratePLUseCase plUseCase;

    public ReportController(GenerateTrialBalanceUseCase trialBalanceUseCase,
                            GenerateBalanceSheetUseCase balanceSheetUseCase,
                            GeneratePLUseCase plUseCase) {
        this.trialBalanceUseCase = trialBalanceUseCase;
        this.balanceSheetUseCase = balanceSheetUseCase;
        this.plUseCase = plUseCase;
    }

    @GetMapping("/trial-balance")
    @Operation(summary = "Generate Trial Balance")
    public ResponseEntity<GenerateTrialBalanceUseCase.Result> trialBalance(
            @Parameter(description = "Fiscal period ID") @RequestParam UUID fiscalPeriodId,
            @Parameter(description = "Include zero balance accounts") @RequestParam(defaultValue = "false") boolean includeZeroBalances) {
        var result = trialBalanceUseCase.execute(fiscalPeriodId, includeZeroBalances);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/balance-sheet")
    @Operation(summary = "Generate Balance Sheet")
    public ResponseEntity<GenerateBalanceSheetUseCase.Result> balanceSheet(
            @Parameter(description = "Fiscal period ID") @RequestParam UUID fiscalPeriodId) {
        var result = balanceSheetUseCase.execute(fiscalPeriodId);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/profit-loss")
    @Operation(summary = "Generate Profit & Loss Statement")
    public ResponseEntity<GeneratePLUseCase.Result> profitLoss(
            @Parameter(description = "Fiscal period ID") @RequestParam UUID fiscalPeriodId,
            @Parameter(description = "Year to date") @RequestParam(defaultValue = "false") boolean yearToDate) {
        var result = plUseCase.execute(fiscalPeriodId, yearToDate);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }
}