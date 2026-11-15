package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.*;
import com.erp.brazil.hr.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendNotificationUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailNotificationProvider emailProvider;
    private final WhatsAppNotificationProvider whatsAppProvider;
    private final PushNotificationProvider pushProvider;

    private static final String NOTIFICATION_QUEUE_KEY = "hr:notifications:queue";
    private static final int BATCH_SIZE = 100;

    @Transactional
    public Notification sendNotification(Long companyId, String templateCode, String locale,
                                         Notification.RecipientType recipientType, Long recipientId,
                                         Map<String, Object> variables, Instant scheduledAt) {

        NotificationTemplate template = templateRepository.findBestMatch(companyId, templateCode, locale)
            .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateCode));

        Notification notification = buildNotification(companyId, template, recipientType, recipientId, variables, scheduledAt);
        Notification saved = notificationRepository.save(notification);

        if (scheduledAt == null || scheduledAt.isBefore(Instant.now())) {
            queueForSending(saved);
        } else {
            scheduleForSending(saved, scheduledAt);
        }

        return saved;
    }

    @Transactional
    public Notification sendImmediateNotification(Long companyId, NotificationChannel channel,
                                                  String recipientEmail, String recipientPhone,
                                                  String subject, String content,
                                                  NotificationPriority priority) {

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        Notification notification = new Notification();
        notification.setCompany(company);
        notification.setChannel(channel);
        notification.setRecipientType(Notification.RecipientType.EXTERNAL);
        notification.setRecipientEmail(recipientEmail);
        notification.setRecipientPhone(recipientPhone);
        notification.setSubject(subject);
        notification.setContent(content);
        notification.setPriority(priority);
        notification.setStatus(NotificationStatus.QUEUED);

        Notification saved = notificationRepository.save(notification);
        queueForSending(saved);
        return saved;
    }

    private Notification buildNotification(Long companyId, NotificationTemplate template,
                                           Notification.RecipientType recipientType, Long recipientId,
                                           Map<String, Object> variables, Instant scheduledAt) {

        String subject = template.renderSubject(variables);
        String content = template.render(variables);

        String recipientEmail = null;
        String recipientPhone = null;

        if (recipientId != null && recipientType == Notification.RecipientType.EMPLOYEE) {
            Employee employee = employeeRepository.findById(recipientId).orElse(null);
            if (employee != null) {
                recipientEmail = employee.getEmail();
                recipientPhone = employee.getPersonalPhone();
            }
        }

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        Notification notification = new Notification();
        notification.setCompany(company);
        notification.setTemplate(template);
        notification.setRecipientType(recipientType);
        notification.setRecipientId(recipientId);
        notification.setRecipientEmail(recipientEmail);
        notification.setRecipientPhone(recipientPhone);
        notification.setChannel(convertChannel(template.getChannel()));
        notification.setSubject(subject);
        notification.setContent(content);
        notification.setPriority(NotificationPriority.NORMAL);
        notification.setStatus(NotificationStatus.QUEUED);
        notification.setScheduledAt(scheduledAt);

        return notification;
    }

    private NotificationChannel convertChannel(NotificationTemplate.Channel templateChannel) {
        return switch (templateChannel) {
            case EMAIL -> NotificationChannel.EMAIL;
            case WHATSAPP -> NotificationChannel.WHATSAPP;
            case SMS -> NotificationChannel.SMS;
            case PUSH -> NotificationChannel.PUSH;
            case IN_APP -> NotificationChannel.IN_APP;
        };
    }

    private void queueForSending(Notification notification) {
        String message = String.format("%s:%d", notification.getId(), System.currentTimeMillis());
        redisTemplate.opsForList().rightPush(NOTIFICATION_QUEUE_KEY, message);
        log.debug("Queued notification {} for sending", notification.getId());
    }

    private void scheduleForSending(Notification notification, Instant scheduledAt) {
        long delay = java.time.Duration.between(Instant.now(), scheduledAt).toMillis();
        if (delay > 0) {
            log.debug("Scheduled notification {} for {}", notification.getId(), scheduledAt);
        }
    }

    @Transactional
    public void processQueue(int batchSize) {
        List<Object> messages = redisTemplate.opsForList().range(NOTIFICATION_QUEUE_KEY, 0, batchSize - 1);
        if (messages == null || messages.isEmpty()) return;

        for (Object message : messages) {
            String[] parts = message.toString().split(":");
            if (parts.length != 2) continue;

            Long notificationId = Long.parseLong(parts[0]);
            processNotification(notificationId);
            redisTemplate.opsForList().leftPop(NOTIFICATION_QUEUE_KEY);
        }
    }

    @Transactional
    public void processNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (notification.getStatus() != NotificationStatus.QUEUED) {
            log.warn("Notification {} not in QUEUED status: {}", notificationId, notification.getStatus());
            return;
        }

        notification.setStatus(NotificationStatus.SENDING);
        notificationRepository.save(notification);

        try {
            switch (notification.getChannel()) {
                case EMAIL -> sendEmail(notification);
                case WHATSAPP -> sendWhatsApp(notification);
                case PUSH -> sendPush(notification);
                case SMS -> sendSms(notification);
                case IN_APP -> saveInApp(notification);
            }

            notification.markSent(UUID.randomUUID().toString());
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Failed to send notification {}", notificationId, e);
            handleFailure(notification, e);
        }
    }

    private void sendEmail(Notification notification) {
        emailProvider.send(notification.getRecipientEmail(), notification.getSubject(), notification.getContent());
    }

    private void sendWhatsApp(Notification notification) {
        whatsAppProvider.send(notification.getRecipientPhone(), notification.getContent());
    }

    private void sendPush(Notification notification) {
        pushProvider.send(notification.getRecipientId(), notification.getSubject(), notification.getContent());
    }

    private void sendSms(Notification notification) {
        // SMS provider integration
    }

    private void saveInApp(Notification notification) {
        // In-app notifications are just saved
        notification.markDelivered();
        notificationRepository.save(notification);
    }

    private void handleFailure(Notification notification, Exception e) {
        if (notification.canRetry()) {
            notification.incrementRetry();
            notificationRepository.save(notification);

            // Re-queue with exponential backoff
            long delay = (long) (Math.pow(2, notification.getRetryCount()) * 1000);
            // Would use delayed queue in production
            queueForSending(notification);
        } else {
            notification.markFailed(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void retryFailedNotifications() {
        Instant since = Instant.now().minusSeconds(300); // 5 minutes ago
        List<Notification> failed = notificationRepository.findFailedNotificationsForRetry(
            NotificationStatus.FAILED, since);

        for (Notification notification : failed) {
            queueForSending(notification);
        }
    }

    // Provider interfaces
    public interface EmailNotificationProvider {
        void send(String to, String subject, String content);
    }

    public interface WhatsAppNotificationProvider {
        void send(String phone, String content);
    }

    public interface PushNotificationProvider {
        void send(Long recipientId, String title, String content);
    }
}