package com.erp.brazil.sales.presentation.rest;

import com.erp.brazil.sales.domain.model.LGPDConsent;
import com.erp.brazil.sales.domain.model.LGPDConsentStatus;
import com.erp.brazil.sales.domain.model.LGPDConsentType;
import com.erp.brazil.sales.domain.repository.LGPDConsentRepository;
import com.erp.brazil.sales.presentation.dto.ApiResponse;
import com.erp.brazil.sales.presentation.dto.LGPDConsentDTO;
import com.erp.brazil.sales.presentation.dto.PageResponse;
import com.erp.brazil.sales.presentation.dto.request.GrantConsentRequest;
import com.erp.brazil.sales.presentation.dto.request.RevokeConsentRequest;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lgpd-consents")
public class LGPDConsentController {

    private final LGPDConsentRepository lgpdConsentRepository;

    public LGPDConsentController(LGPDConsentRepository lgpdConsentRepository) {
        this.lgpdConsentRepository = lgpdConsentRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LGPDConsentDTO>> grant(@RequestHeader("X-Company-ID") UUID companyId,
                                                              @RequestBody GrantConsentRequest request) {
        LGPDConsent consent = new LGPDConsent();
        consent.setCompanyId(companyId);
        consent.setEntityType(request.entityType());
        consent.setEntityId(request.entityId());
        consent.setType(request.type());
        consent.setStatus(LGPDConsentStatus.GRANTED);
        consent.setGrantedAt(LocalDateTime.now());
        consent.setLegalBasis(request.legalBasis());
        consent.setPurpose(request.purpose());
        consent.setVersion(request.version());
        consent.setExpiresAt(request.expiresAt());
        consent.setIpAddress(request.ipAddress());
        consent.setUserAgent(request.userAgent());
        consent.setNotes(request.notes());

        consent = lgpdConsentRepository.save(consent);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(consent)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LGPDConsentDTO>> getById(@RequestHeader("X-Company-ID") UUID companyId,
                                                                @PathVariable UUID id) {
        LGPDConsent consent = lgpdConsentRepository.findById(id)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("LGPD consent not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(consent)));
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<ApiResponse<LGPDConsentDTO>> revoke(@RequestHeader("X-Company-ID") UUID companyId,
                                                               @PathVariable UUID id,
                                                               @RequestBody RevokeConsentRequest request) {
        LGPDConsent consent = lgpdConsentRepository.findById(id)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("LGPD consent not found: " + id));

        if (consent.getStatus() == LGPDConsentStatus.REVOKED) {
            throw new IllegalStateException("Consent already revoked");
        }

        consent.setStatus(LGPDConsentStatus.REVOKED);
        consent.setRevokedAt(LocalDateTime.now());
        consent.setNotes((consent.getNotes() != null ? consent.getNotes() + "\n" : "") + "Revoked: " + request.reason());

        consent = lgpdConsentRepository.save(consent);
        return ResponseEntity.ok(ApiResponse.ok(mapToDTO(consent)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LGPDConsentDTO>>> list(@RequestHeader("X-Company-ID") UUID companyId,
                                                                           @RequestParam(required = false) LGPDConsent.EntityType entityType,
                                                                           @RequestParam(required = false) UUID entityId,
                                                                           @RequestParam(required = false) LGPDConsentType type,
                                                                           @RequestParam(required = false) LGPDConsentStatus status,
                                                                           Pageable pageable) {
        Page<LGPDConsent> page;
        if (entityType != null && entityId != null) {
            page = lgpdConsentRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        } else {
            page = lgpdConsentRepository.findAll(pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(mapToPageResponse(page)));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<LGPDConsentDTO>>> getByEntity(@RequestHeader("X-Company-ID") UUID companyId,
                                                                          @PathVariable LGPDConsent.EntityType entityType,
                                                                          @PathVariable UUID entityId) {
        List<LGPDConsent> consents = lgpdConsentRepository.findByEntityTypeAndEntityId(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.ok(consents.stream().map(this::mapToDTO).toList()));
    }

    private LGPDConsentDTO mapToDTO(LGPDConsent c) {
        return new LGPDConsentDTO(
                c.getId(), c.getEntityType(), c.getEntityId(), getEntityName(c),
                c.getType(), c.getStatus(), c.getGrantedAt(), c.getRevokedAt(),
                c.getExpiresAt(), c.getLegalBasis(), c.getPurpose(), c.getVersion(),
                c.getIpAddress(), c.getUserAgent(), c.getNotes(), c.getCreatedAt(), c.getUpdatedAt()
        );
    }

    private String getEntityName(LGPDConsent c) {
        // In a real implementation, this would fetch the entity name based on type and ID
        return c.getEntityType() + "-" + c.getEntityId().toString().substring(0, 8);
    }

    private PageResponse<LGPDConsentDTO> mapToPageResponse(Page<LGPDConsent> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapToDTO).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}