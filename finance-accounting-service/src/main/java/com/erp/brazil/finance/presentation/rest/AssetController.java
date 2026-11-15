package com.erp.brazil.finance.presentation.rest;

import com.erp.brazil.finance.application.usecase.RunDepreciationUseCase;
import com.erp.brazil.finance.domain.model.Asset;
import com.erp.brazil.finance.domain.model.AssetCategory;
import com.erp.brazil.finance.domain.model.AssetLocation;
import com.erp.brazil.finance.domain.model.AssetStatus;
import com.erp.brazil.finance.domain.model.ChartOfAccounts;
import com.erp.brazil.finance.domain.model.CostCenter;
import com.erp.brazil.finance.domain.model.DepreciationMethod;
import com.erp.brazil.finance.domain.repository.AssetCategoryRepository;
import com.erp.brazil.finance.domain.repository.AssetLocationRepository;
import com.erp.brazil.finance.domain.repository.AssetRepository;
import com.erp.brazil.finance.domain.repository.ChartOfAccountsRepository;
import com.erp.brazil.finance.domain.repository.CostCenterRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assets")
@Tag(name = "Assets", description = "Fixed Asset management APIs")
public class AssetController {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final AssetLocationRepository assetLocationRepository;
    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final CostCenterRepository costCenterRepository;
    private final RunDepreciationUseCase runDepreciationUseCase;

    public AssetController(AssetRepository assetRepository,
                           AssetCategoryRepository assetCategoryRepository,
                           AssetLocationRepository assetLocationRepository,
                           ChartOfAccountsRepository chartOfAccountsRepository,
                           CostCenterRepository costCenterRepository,
                           RunDepreciationUseCase runDepreciationUseCase) {
        this.assetRepository = assetRepository;
        this.assetCategoryRepository = assetCategoryRepository;
        this.assetLocationRepository = assetLocationRepository;
        this.chartOfAccountsRepository = chartOfAccountsRepository;
        this.costCenterRepository = costCenterRepository;
        this.runDepreciationUseCase = runDepreciationUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a new asset")
    public ResponseEntity<AssetResponse> create(@Valid @RequestBody CreateAssetRequest request) {
        AssetCategory category = assetCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.categoryId()));

        AssetLocation location = request.locationId() != null
                ? assetLocationRepository.findById(request.locationId()).orElse(null)
                : null;

        CostCenter costCenter = request.costCenterId() != null
                ? costCenterRepository.findById(request.costCenterId()).orElse(null)
                : null;

        ChartOfAccounts account = chartOfAccountsRepository.findById(request.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.accountId()));

        ChartOfAccounts accumDepAccount = chartOfAccountsRepository.findById(request.accumulatedDepreciationAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Accumulated depreciation account not found: " + request.accumulatedDepreciationAccountId()));

        ChartOfAccounts deprExpenseAccount = chartOfAccountsRepository.findById(request.depreciationExpenseAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Depreciation expense account not found: " + request.depreciationExpenseAccountId()));

        Asset asset = new Asset(
                request.code(),
                request.assetTag(),
                request.name(),
                category,
                account,
                accumDepAccount,
                deprExpenseAccount,
                request.acquisitionDate(),
                request.acquisitionCost(),
                request.usefulLifeMonths(),
                request.depreciationMethod()
        );
        asset.setDescription(request.description());
        asset.setLocation(location);
        asset.setCostCenter(costCenter);
        asset.setResidualValue(request.residualValue());
        asset.setDepreciationStartDate(request.depreciationStartDate());

        Asset saved = assetRepository.save(asset);
        return ResponseEntity.created(URI.create("/api/v1/assets/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get asset by ID")
    public ResponseEntity<AssetResponse> getById(@PathVariable UUID id) {
        return assetRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List assets")
    public ResponseEntity<Page<AssetResponse>> list(
            @Parameter(description = "Category ID") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Location ID") @RequestParam(required = false) UUID locationId,
            @Parameter(description = "Cost center ID") @RequestParam(required = false) UUID costCenterId,
            @Parameter(description = "Status filter") @RequestParam(required = false) AssetStatus status,
            @Parameter(description = "Acquisition date from") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate acquisitionFrom,
            @Parameter(description = "Acquisition date to") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate acquisitionTo,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Asset> assets;

        if (categoryId != null) {
            AssetCategory category = assetCategoryRepository.findById(categoryId).orElseThrow();
            assets = assetRepository.findByCategory(category, pageable);
        } else if (status != null) {
            assets = assetRepository.findByStatus(status, pageable);
        } else {
            assets = assetRepository.findAll(pageable);
        }

        return ResponseEntity.ok(assets.map(this::toResponse));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update asset")
    public ResponseEntity<AssetResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateAssetRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));

        if (asset.getStatus() == AssetStatus.DISPOSED || asset.getStatus() == AssetStatus.SOLD) {
            throw new IllegalStateException("Cannot update disposed or sold asset");
        }

        asset.setName(request.name());
        asset.setDescription(request.description());

        if (request.locationId() != null) {
            AssetLocation location = assetLocationRepository.findById(request.locationId()).orElseThrow();
            asset.setLocation(location);
        }

        if (request.costCenterId() != null) {
            CostCenter costCenter = costCenterRepository.findById(request.costCenterId()).orElseThrow();
            asset.setCostCenter(costCenter);
        }

        if (request.status() != null) {
            asset.setStatus(request.status());
        }

        Asset saved = assetRepository.save(asset);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PostMapping("/{id}/dispose")
    @Operation(summary = "Dispose asset")
    public ResponseEntity<AssetResponse> dispose(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate disposalDate,
            @RequestParam java.math.BigDecimal disposalValue,
            @RequestParam String disposalReason) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));

        asset.setStatus(AssetStatus.DISPOSED);
        asset.setDisposalDate(disposalDate);
        asset.setDisposalValue(disposalValue);
        asset.setDisposalReason(disposalReason);

        Asset saved = assetRepository.save(asset);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PostMapping("/depreciation/run")
    @Operation(summary = "Run depreciation for a fiscal period")
    public ResponseEntity<RunDepreciationUseCase.Result> runDepreciation(
            @RequestParam UUID fiscalPeriodId,
            @RequestParam String postedBy) {
        var result = runDepreciationUseCase.execute(fiscalPeriodId, postedBy);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    private AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getCode(),
                asset.getAssetTag(),
                asset.getName(),
                asset.getDescription(),
                asset.getCategory().getId(),
                asset.getCategory().getName(),
                asset.getLocation() != null ? asset.getLocation().getId() : null,
                asset.getLocation() != null ? asset.getLocation().getName() : null,
                asset.getCostCenter() != null ? asset.getCostCenter().getId() : null,
                asset.getAccount().getId(),
                asset.getAccount().getCode(),
                asset.getAccumulatedDepreciationAccount().getId(),
                asset.getDepreciationExpenseAccount().getId(),
                asset.getAcquisitionDate(),
                asset.getActivationDate(),
                asset.getAcquisitionCost(),
                asset.getResidualValue(),
                asset.getUsefulLifeMonths(),
                asset.getDepreciationMethod(),
                asset.getStatus(),
                asset.getDepreciationStartDate(),
                asset.getLastDepreciationDate(),
                asset.getDisposalDate(),
                asset.getDisposalValue(),
                asset.getDisposalReason(),
                asset.getAccumulatedDepreciation(),
                asset.getBookValue(),
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }

    // Request/Response Records
    public record CreateAssetRequest(
            @NotNull String code,
            @NotNull String assetTag,
            @NotNull String name,
            String description,
            @NotNull UUID categoryId,
            UUID locationId,
            UUID costCenterId,
            @NotNull UUID accountId,
            @NotNull UUID accumulatedDepreciationAccountId,
            @NotNull UUID depreciationExpenseAccountId,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate acquisitionDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate activationDate,
            @NotNull java.math.BigDecimal acquisitionCost,
            java.math.BigDecimal residualValue,
            @NotNull Integer usefulLifeMonths,
            @NotNull DepreciationMethod depreciationMethod,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate depreciationStartDate
    ) {}

    public record UpdateAssetRequest(
            @NotNull String name,
            String description,
            UUID locationId,
            UUID costCenterId,
            AssetStatus status
    ) {}

    public record AssetResponse(
            UUID id,
            String code,
            String assetTag,
            String name,
            String description,
            UUID categoryId,
            String categoryName,
            UUID locationId,
            String locationName,
            UUID costCenterId,
            UUID accountId,
            String accountCode,
            UUID accumulatedDepreciationAccountId,
            UUID depreciationExpenseAccountId,
            LocalDate acquisitionDate,
            LocalDate activationDate,
            java.math.BigDecimal acquisitionCost,
            java.math.BigDecimal residualValue,
            Integer usefulLifeMonths,
            DepreciationMethod depreciationMethod,
            AssetStatus status,
            LocalDate depreciationStartDate,
            LocalDate lastDepreciationDate,
            LocalDate disposalDate,
            java.math.BigDecimal disposalValue,
            String disposalReason,
            java.math.BigDecimal accumulatedDepreciation,
            java.math.BigDecimal bookValue,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {}
}