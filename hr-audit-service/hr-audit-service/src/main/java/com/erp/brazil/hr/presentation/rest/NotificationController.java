package com.erp.brazil.hr.presentation.rest;

import com.erp.brazil.hr.application.dto.NotificationDTO;
import com.erp.brazil.hr.application.dto.NotificationTemplateDTO;
import com.erp.brazil.hr.application.usecase.SendNotificationUseCase;
import com.erp.brazil.hr.domain.model.Notification;
import com.erp.brazil.hr.domain.model.NotificationTemplate;
import com.erp.brazil.hr.domain.repository.NotificationRepository;
import com.erp.brazil.hr.domain.repository.NotificationTemplateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notification management API")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final SendNotificationUseCase sendNotificationUseCase;

    public NotificationController(NotificationRepository notificationRepository,
                                   NotificationTemplateRepository templateRepository,
                                   SendNotificationUseCase sendNotificationUseCase) {
        this.notificationRepository = notificationRepository;
        this.templateRepository = templateRepository;
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    // Template endpoints
    @PostMapping("/templates")
    @Operation(summary = "Create a notification template")
    public ResponseEntity<NotificationTemplateDTO> createTemplate(@Valid @RequestBody NotificationTemplateDTO dto) {
        NotificationTemplate template = new NotificationTemplate(
            null, UUID.randomUUID(), dto.companyId(), dto.code(), dto.name(),
            dto.channel(), dto.subject(), dto.contentTemplate(), dto.contentType(),
            dto.variables(), dto.locale(), true, false
        );

        NotificationTemplate saved = templateRepository.save(template);
        return ResponseEntity.status(HttpStatus.CREATED).body(NotificationTemplateDTO.from(saved));
    }

    @GetMapping("/templates/{id}")
    @Operation(summary = "Get notification template by ID")
    public ResponseEntity<NotificationTemplateDTO> getTemplate(@PathVariable UUID id) {
        return templateRepository.findByUuid(id)
            .map(NotificationTemplateDTO::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/templates")
    @Operation(summary = "List notification templates")
    public ResponseEntity<Page<NotificationTemplateDTO>> listTemplates(
            @Parameter(description = "Company ID") @RequestParam(required = false) Long companyId,
            @Parameter(description = "Channel") @RequestParam(required = false) NotificationTemplate.Channel channel,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationTemplateDTO> page;
        if (companyId != null && channel != null) {
            page = templateRepository.findByCompanyIdAndChannel(companyId, channel, pageable)
                .map(NotificationTemplateDTO::from);
        } else if (companyId != null) {
            page = templateRepository.findByCompanyId(companyId, pageable)
                .map(NotificationTemplateDTO::from);
        } else if (channel != null) {
            page = templateRepository.findByIsActiveTrueAndChannel(channel, pageable)
                .map(NotificationTemplateDTO::from);
        } else {
            page = templateRepository.findAll(pageable).map(NotificationTemplateDTO::from);
        }
        return ResponseEntity.ok(page);
    }

    // Notification endpoints
    @PostMapping
    @Operation(summary = "Send a notification using a template")
    public ResponseEntity<NotificationDTO> sendNotification(
            @RequestParam Long templateId,
            @RequestParam Notification.RecipientType recipientType,
            @RequestParam Long recipientId,
            @RequestParam(required = false) Map<String, Object> context,
            @RequestParam(required = false) Instant scheduledAt) {
        Notification notification = sendNotificationUseCase.send(templateId, recipientType, recipientId, context, scheduledAt);
        return ResponseEntity.status(HttpStatus.CREATED).body(NotificationDTO.from(notification));
    }

    @PostMapping("/send")
    @Operation(summary = "Send a direct notification (without template)")
    public ResponseEntity<NotificationDTO> sendDirect(
            @RequestParam Notification.Channel channel,
            @RequestParam String recipient,
            @RequestParam String subject,
            @RequestParam String content,
            @RequestParam(defaultValue = "NORMAL") Notification.Priority priority) {
        Notification notification = sendNotificationUseCase.sendDirect(channel, recipient, subject, content, priority);
        return ResponseEntity.status(HttpStatus.CREATED).body(NotificationDTO.from(notification));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<NotificationDTO> getNotification(@PathVariable UUID id) {
        return notificationRepository.findByUuid(id)
            .map(NotificationDTO::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List notifications with filters")
    public ResponseEntity<Page<NotificationDTO>> listNotifications(
            @Parameter(description = "Recipient type") @RequestParam(required = false) Notification.RecipientType recipientType,
            @Parameter(description = "Recipient ID") @RequestParam(required = false) Long recipientId,
            @Parameter(description = "Status") @RequestParam(required = false) Notification.Status status,
            @Parameter(description = "Company ID") @RequestParam(required = false) Long companyId,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<NotificationDTO> page;
        if (recipientType != null && recipientId != null) {
            page = notificationRepository.findByRecipientTypeAndRecipientId(recipientType, recipientId, pageable)
                .map(NotificationDTO::from);
        } else if (companyId != null && status != null) {
            page = notificationRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                .map(NotificationDTO::from);
        } else if (status != null) {
            page = notificationRepository.findByStatus(status, pageable)
                .map(NotificationDTO::from);
        } else {
            page = notificationRepository.findAll(pageable).map(NotificationDTO::from);
        }
        return ResponseEntity.ok(page);
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry a failed notification")
    public ResponseEntity<Void> retryNotification(@PathVariable UUID id) {
        sendNotificationUseCase.retryFailed(id);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a queued notification")
    public ResponseEntity<Void> cancelNotification(@PathVariable UUID id) {
        Notification notification = notificationRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));

        if (!notification.isPending()) {
            throw new IllegalStateException("Only pending notifications can be cancelled");
        }

        Notification cancelled = new Notification(
            notification.id(), notification.uuid(), notification.companyId(), notification.templateId(),
            notification.recipientType(), notification.recipientId(), notification.recipientEmail(),
            notification.recipientPhone(), notification.channel(), notification.subject(),
            notification.content(), notification.priority(), Notification.Status.CANCELLED,
            notification.scheduledAt(), notification.sentAt(), notification.deliveredAt(),
            notification.readAt(), notification.failedAt(), notification.failureReason(),
            notification.retryCount(), notification.maxRetries(), notification.externalId(),
            notification.metadata()
        );

        notificationRepository.save(cancelled);
        return ResponseEntity.noContent().build();
    }
}