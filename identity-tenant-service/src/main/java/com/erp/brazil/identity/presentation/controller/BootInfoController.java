package com.erp.brazil.identity.presentation.controller;

import com.erp.brazil.common.presentation.controller.BaseController;
import com.erp.brazil.identity.domain.model.BootInfo;
import com.erp.brazil.identity.application.service.BootInfoService;
import com.erp.brazil.identity.presentation.response.BootInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Boot Info Controller.
 * Provides system initialization information for frontend.
 */
@RestController
@RequestMapping("/api/bootinfo")
@RequiredArgsConstructor
@Tag(name = "Boot Info", description = "System initialization and configuration information")
public class BootInfoController extends BaseController {

    private final BootInfoService bootInfoService;

    @GetMapping
    @Operation(summary = "Get boot info", description = "Returns system configuration for frontend initialization")
    public ResponseEntity<BootInfoResponse> getBootInfo() {
        BootInfo bootInfo = bootInfoService.getBootInfo();
        return ok(BootInfoResponse.from(bootInfo));
    }

    @PutMapping
    @Operation(summary = "Update boot info", description = "Updates system configuration (admin only)")
    public ResponseEntity<BootInfoResponse> updateBootInfo(@RequestBody BootInfoResponse request) {
        BootInfo updated = bootInfoService.updateBootInfo(request.toEntity(), getUserId());
        return ok(BootInfoResponse.from(updated));
    }
}