package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.*;
import com.erp.brazil.hr.infrastructure.notification.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class SendNotificationUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailProvider emailProvider;
    private final WhatsAppProvider whatsAppProvider;
    private final PushNotificationProvider pushProvider;
    private final AuditEventUseCase auditEventUseCase;

    public SendNotificationUseCase(NotificationRepository notificationRepository,
                                    NotificationTemplateRepository templateRepository,
                                    EmployeeRepository employeeRepository,
                                    EmailProvider emailProvider,
                                    WhatsAppProvider whatsAppProvider,
                                    PushNotificationProvider pushProvider,
                                    AuditEventUseCase auditEventUseCase) {
        this.notificationRepository = notificationRepository;
        this.templateRepository = templateRepository;
        this.employeeRepository = employeeRepository;
        this.emailProvider = emailProvider;
        this.whatsAppProvider = whatsAppProvider;
        this.pushProvider = pushProvider;
        this.auditEventUseCase = auditEventUseCase;
    }

    @Transactional
    public Notification send(Long templateId, Notification.RecipientType recipientType, Long recipientId,
                              Map<String, Object> context, Instant scheduledAt) {
        NotificationTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        if (!template.isActive()) {
            throw new IllegalStateException("Template is not active: " + templateId);
        }

        String renderedContent = template.render(context);
        String renderedSubject = template.renderSubject(context);

        Notification notification = new Notification(
            null, UUID.randomUUID(), null, templateId, recipientType, recipientId,
            null, null, template.channel(), renderedSubject, renderedContent,
            Notification.Priority.NORMAL, Notification.Status.QUEUED,
            scheduledAt, null, null, null, null, null, 0, 3, null, context
        );

        // Resolve recipient contact info
        if (recipientType == Notification.RecipientType.EMPLOYEE && recipientId != null) {
            Employee employee = employeeRepository.findById(recipientId).orElse(null);
            if (employee != null) {
                notification = new Notification(
                    notification.id(), notification.uuid(), employee.companyId(), notification.templateId(),
                    notification.recipientType(), notification.recipientId(), employee.email(),
                    null, notification.channel(), notification.subject(), notification.content(),
                    notification.priority(), notification.status(), notification.scheduledAt(),
                    notification.sentAt(), notification.deliveredAt(), notification.readAt(),
                    notification.failedAt(), notification.failureReason(), notification.retryCount(),
                    notification.maxRetries(), notification.externalId(), notification.metadata()
                );
            }
        }

        Notification saved = notificationRepository.save(notification);

        if (scheduledAt == null || scheduledAt.isBefore(Instant.now())) {
            sendAsync(saved);
        }

        return saved;
    }

    @Transactional
    public Notification sendDirect(Notification.Channel channel, String recipient, String subject,
                                    String content, Notification.Priority priority) {
        Notification notification = new Notification(
            null, UUID.randomUUID(), null, null, Notification.RecipientType.EXTERNAL, null,
            channel == Notification.Channel.EMAIL ? recipient : null,
            channel == Notification.Channel.WHATSAPP || channel == Notification.Channel.SMS ? recipient : null,
            channel, subject, content, priority, Notification.Status.QUEUED,
            null, null, null, null, null, null, 0, 3, null, Map.of()
        );

        Notification saved = notificationRepository.save(notification);
        sendAsync(saved);
        return saved;
    }

    private void sendAsync(Notification notification) {
        CompletableFuture.runAsync(() -> {
            try {
                notificationRepository.save(new Notification(
                    notification.id(), notification.uuid(), notification.companyId(), notification.templateId(),
                    notification.recipientType(), notification.recipientId(), notification.recipientEmail(),
                    notification.recipientPhone(), notification.channel(), notification.subject(),
                    notification.content(), notification.priority(), Notification.Status.SENDING,
                    notification.scheduledAt(), null, null, null, null, null, notification.retryCount(),
                    notification.maxRetries(), notification.externalId(), notification.metadata()
                ));

                String externalId = switch (notification.channel()) {
                    case EMAIL -> emailProvider.send(notification.recipientEmail(), notification.subject(), notification.content());
                    case WHATSAPP -> whatsAppProvider.send(notification.recipientPhone(), notification.content());
                    case PUSH -> pushProvider.send(notification.recipientId(), notification.subject(), notification.content());
                    case SMS -> ""; // SMS provider would be implemented similarly
                    case IN_APP -> ""; // In-app notifications stored in DB
                };

                Notification sent = new Notification(
                    notification.id(), notification.uuid(), notification.companyId(), notification.templateId(),
                    notification.recipientType(), notification.recipientId(), notification.recipientEmail(),
                    notification.recipientPhone(), notification.channel(), notification.subject(),
                    notification.content(), notification.priority(), Notification.Status.SENT,
                    notification.scheduledAt(), Instant.now(), null, null, null, null, notification.retryCount(),
                    notification.maxRetries(), externalId, notification.metadata()
                );

                notificationRepository.save(sent);
                auditEventUseCase.logEvent("NOTIFICATION_SENT", "Notification", notification.uuid().toString(),
                    notification.companyId(), null, sent);
            } catch (Exception e) {
                handleFailure(notification, e);
            }
        });
    }

    private void handleFailure(Notification notification, Exception e) {
        int newRetryCount = notification.retryCount() + 1;
        Notification.Status newStatus = newRetryCount >= notification.maxRetries()
            ? Notification.Status.FAILED : Notification.Status.QUEUED;

        Notification failed = new Notification(
            notification.id(), notification.uuid(), notification.companyId(), notification.templateId(),
            notification.recipientType(), notification.recipientId(), notification.recipientEmail(),
            notification.recipientPhone(), notification.channel(), notification.subject(),
            notification.content(), notification.priority(), newStatus,
            notification.scheduledAt(), notification.sentAt(), null, null,
            Instant.now(), e.getMessage(), newRetryCount, notification.maxRetries(),
            notification.externalId(), notification.metadata()
        );

        notificationRepository.save(failed);
        auditEventUseCase.logEvent("NOTIFICATION_FAILED", "Notification", notification.uuid().toString(),
            notification.companyId(), null, failed);
    }

    @Transactional
    public void retryFailed(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (!notification.canRetry()) {
            throw new IllegalStateException("Notification cannot be retried");
        }

        Notification retry = new Notification(
            notification.id(), notification.uuid(), notification.companyId(), notification.templateId(),
            notification.recipientType(), notification.recipientId(), notification.recipientEmail(),
            notification.recipientPhone(), notification.channel(), notification.subject(),
            notification.content(), notification.priority(), Notification.Status.QUEUED,
            notification.scheduledAt(), null, null, null, null, null, 0, notification.maxRetries(),
            null, notification.metadata()
        );

        notificationRepository.save(retry);
        sendAsync(retry);
    }
}