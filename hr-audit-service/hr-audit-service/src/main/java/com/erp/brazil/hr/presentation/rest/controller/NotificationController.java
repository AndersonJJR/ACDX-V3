package com.erp.brazil.hr.presentation.rest.controller;

import com.erp.brazil.hr.application.usecase.SendNotificationUseCase;
import com.erp.brazil.hr.domain.model.Notification;
import com.erp.brazil.hr.domain.model.NotificationChannel;
import com.erp.brazil.hr.domain.model.NotificationPriority;
import com.erp.brazil.hr.domain.model.NotificationStatus;
import com.erp.brazil.hr.domain.model.NotificationTemplate;
import com.erp.brazil.hr.domain.repository.NotificationRepository;
import com.erp.brazil.hr.domain.repository.NotificationTemplateRepository;
import com.erp.brazil.hr.presentation.rest.dto.NotificationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Multi-channel notification APIs")
public class NotificationController {

    private final SendNotificationUseCase notificationUseCase;
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;

    @PostMapping("/templates")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Create notification template")
    public ResponseEntity<NotificationDto.TemplateResponse> createTemplate(@Valid @RequestBody NotificationDto.TemplateRequest request) {
        NotificationTemplate template = new NotificationTemplate();
        template.setCompanyId(request.getCompanyId());
        template.setCode(request.getCode());
        template.setName(request.getName());
        template.setChannel(request.getChannel());
        template.setSubject(request.getSubject());
        template.setContentTemplate(request.getContentTemplate());
        template.setContentType(NotificationTemplate.ContentType.valueOf(request.getContentType()));
        template.setVariables(request.getVariables());
        template.setLocale(request.getLocale() != null ? request.getLocale() : "pt_BR");
        template.setIsActive(true);
        template.setIsSystem(request.getCompanyId() == null);

        NotificationTemplate saved = templateRepository.save(template);
        return ResponseEntity.ok(toTemplateResponse(saved));
    }

    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "List notification templates")
    public ResponseEntity<List<NotificationDto.TemplateResponse>> listTemplates(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) NotificationChannel channel) {

        Long effectiveCompanyId = companyId != null ? companyId : getCurrentCompanyId();
        List<NotificationTemplate> templates = channel != null ?
            templateRepository.findByCompanyIdAndChannelAndIsActiveTrue(effectiveCompanyId, channel) :
            templateRepository.findByCompanyIdAndIsActiveTrue(effectiveCompanyId);

        return ResponseEntity.ok(templates.stream().map(this::toTemplateResponse).collect(Collectors.toList()));
    }

    @GetMapping("/templates/{code}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Get template by code")
    public ResponseEntity<NotificationDto.TemplateResponse> getTemplate(
            @PathVariable String code,
            @RequestParam(defaultValue = "pt_BR") String locale) {

        NotificationTemplate template = templateRepository.findBestMatch(getCurrentCompanyId(), code, locale)
            .orElseThrow(() -> new IllegalArgumentException("Template not found"));
        return ResponseEntity.ok(toTemplateResponse(template));
    }

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Send notification using template")
    public ResponseEntity<NotificationDto.NotificationResponse> sendNotification(@Valid @RequestBody NotificationDto.SendNotificationRequest request) {
        Notification notification = notificationUseCase.sendNotification(
            getCurrentCompanyId(),
            request.getTemplateCode(),
            request.getLocale(),
            Notification.RecipientType.valueOf(request.getRecipientType().name()),
            request.getRecipientId(),
            request.getVariables(),
            request.getScheduledAt()
        );
        return ResponseEntity.ok(toNotificationResponse(notification));
    }

    @PostMapping("/send-immediate")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Send immediate notification without template")
    public ResponseEntity<NotificationDto.NotificationResponse> sendImmediate(@Valid @RequestBody NotificationDto.ImmediateNotificationRequest request) {
        Notification notification = notificationUseCase.sendImmediateNotification(
            getCurrentCompanyId(),
            request.getChannel(),
            request.getRecipientEmail(),
            request.getRecipientPhone(),
            request.getSubject(),
            request.getContent(),
            request.getPriority()
        );
        return ResponseEntity.ok(toNotificationResponse(notification));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "List notifications")
    public ResponseEntity<NotificationDto.PageResponse> listNotifications(
            @RequestParam(required = false) Notification.RecipientType recipientType,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Long effectiveRecipientId = recipientId != null ? recipientId : getCurrentUserId();
        List<Notification> notifications = notificationRepository.findByRecipientTypeAndRecipientId(
            recipientType != null ? recipientType.name() : "EMPLOYEE",
            effectiveRecipientId
        );

        return ResponseEntity.ok(NotificationDto.PageResponse.builder()
            .content(notifications.stream().map(this::toNotificationResponse).collect(Collectors.toList()))
            .page(pageable.getPageNumber())
            .size(pageable.getPageSize())
            .totalElements(notifications.size())
            .totalPages(1)
            .first(true)
            .last(true)
            .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Get notification details")
    public ResponseEntity<NotificationDto.NotificationResponse> getNotification(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        return ResponseEntity.ok(toNotificationResponse(notification));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.markRead();
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }

    private NotificationDto.TemplateResponse toTemplateResponse(NotificationTemplate template) {
        return NotificationDto.TemplateResponse.builder()
            .id(template.getId())
            .uuid(template.getUuid())
            .companyId(template.getCompanyId())
            .code(template.getCode())
            .name(template.getName())
            .channel(template.getChannel())
            .subject(template.getSubject())
            .contentTemplate(template.getContentTemplate())
            .contentType(template.getContentType().name())
            .variables(template.getVariables())
            .locale(template.getLocale())
            .isActive(template.getIsActive())
            .isSystem(template.getIsSystem())
            .build();
    }

    private NotificationDto.NotificationResponse toNotificationResponse(Notification notification) {
        return NotificationDto.NotificationResponse.builder()
            .id(notification.getId())
            .uuid(notification.getUuid())
            .companyId(notification.getCompanyId())
            .templateId(notification.getTemplate() != null ? notification.getTemplate().getId() : null)
            .templateCode(notification.getTemplate() != null ? notification.getTemplate().getCode() : null)
            .recipientType(NotificationDto.RecipientType.valueOf(notification.getRecipientType().name()))
            .recipientId(notification.getRecipientId())
            .recipientEmail(notification.getRecipientEmail())
            .recipientPhone(notification.getRecipientPhone())
            .channel(notification.getChannel())
            .subject(notification.getSubject())
            .content(notification.getContent())
            .priority(notification.getPriority())
            .status(notification.getStatus())
            .scheduledAt(notification.getScheduledAt())
            .sentAt(notification.getSentAt())
            .deliveredAt(notification.getDeliveredAt())
            .readAt(notification.getReadAt())
            .failedAt(notification.getFailedAt())
            .failureReason(notification.getFailureReason())
            .retryCount(notification.getRetryCount())
            .maxRetries(notification.getMaxRetries())
            .externalId(notification.getExternalId())
            .metadata(notification.getMetadata())
            .build();
    }

    private Long getCurrentCompanyId() {
        return 1L;
    }

    private Long getCurrentUserId() {
        return 1L;
    }
}