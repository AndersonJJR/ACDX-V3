package com.erp.brazil.hr.infrastructure.persistence;

import com.erp.brazil.hr.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NotificationQueueRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String QUEUE_KEY = "hr:notifications:queue";
    private static final String PROCESSING_KEY = "hr:notifications:processing";
    private static final String SCHEDULED_KEY = "hr:notifications:scheduled";
    private static final String DLQ_KEY = "hr:notifications:dlq";

    public void enqueue(Notification notification) {
        String message = String.format("%s:%d", notification.getId(), System.currentTimeMillis());
        redisTemplate.opsForList().rightPush(QUEUE_KEY, message);
        log.debug("Enqueued notification {}", notification.getId());
    }

    public void enqueuePriority(Notification notification) {
        String message = String.format("%s:%d", notification.getId(), System.currentTimeMillis());
        redisTemplate.opsForList().leftPush(QUEUE_KEY, message);
        log.debug("Enqueued priority notification {}", notification.getId());
    }

    public void scheduleForLater(Notification notification, Instant scheduledAt) {
        long delay = Duration.between(Instant.now(), scheduledAt).toMillis();
        if (delay <= 0) {
            enqueue(notification);
            return;
        }
        
        String key = String.format("%s:%d", SCHEDULED_KEY, scheduledAt.toEpochMilli());
        String message = String.format("%s:%d", notification.getId(), System.currentTimeMillis());
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, Duration.ofMillis(delay + 60000)); // Expire 1 minute after scheduled time
        log.debug("Scheduled notification {} for {}", notification.getId(), scheduledAt);
    }

    public Optional<Notification> dequeue() {
        String message = (String) redisTemplate.opsForList().leftPop(QUEUE_KEY);
        if (message == null) {
            return Optional.empty();
        }
        
        String[] parts = message.split(":");
        if (parts.length != 2) {
            log.warn("Invalid message format: {}", message);
            return Optional.empty();
        }
        
        Long notificationId = Long.parseLong(parts[0]);
        // Move to processing set with timestamp
        redisTemplate.opsForZSet().add(PROCESSING_KEY, message, System.currentTimeMillis());
        
        log.debug("Dequeued notification {}", notificationId);
        Notification notification = new Notification();
        notification.setId(notificationId);
        return Optional.of(notification);
    }

    public void markProcessing(Long notificationId) {
        String message = String.format("%s:%d", notificationId, System.currentTimeMillis());
        redisTemplate.opsForZSet().add(PROCESSING_KEY, message, System.currentTimeMillis());
    }

    public void markCompleted(Long notificationId) {
        // Remove from processing
        Set<Object> messages = redisTemplate.opsForZSet().rangeByScore(PROCESSING_KEY, 0, System.currentTimeMillis());
        if (messages != null) {
            for (Object msg : messages) {
                String[] parts = msg.toString().split(":");
                if (parts.length == 2 && Long.parseLong(parts[0]).equals(notificationId)) {
                    redisTemplate.opsForZSet().remove(PROCESSING_KEY, msg);
                    break;
                }
            }
        }
    }

    public void moveToDlq(Long notificationId, String reason) {
        String message = String.format("%s:%d:%s", notificationId, System.currentTimeMillis(), reason);
        redisTemplate.opsForList().rightPush(DLQ_KEY, message);
        markCompleted(notificationId);
        log.warn("Moved notification {} to DLQ: {}", notificationId, reason);
    }

    public void requeueFromDlq(Long notificationId) {
        Set<Object> messages = redisTemplate.opsForZSet().range(PROCESSING_KEY, 0, -1);
        if (messages != null) {
            for (Object msg : messages) {
                String[] parts = msg.toString().split(":");
                if (parts.length >= 1) {
                    long parsedId = Long.parseLong(parts[0]);
                    if (parsedId == notificationId) {
                        String message = String.format("%s:%d", notificationId, System.currentTimeMillis());
                        redisTemplate.opsForList().rightPush(QUEUE_KEY, message);
                        redisTemplate.opsForZSet().remove(PROCESSING_KEY, msg);
                        log.info("Requeued notification {} from DLQ", notificationId);
                        break;
                    }
                }
            }
        }
    }

    public long getQueueSize() {
        Long size = redisTemplate.opsForList().size(QUEUE_KEY);
        return size != null ? size : 0;
    }

    public long getProcessingSize() {
        Long size = redisTemplate.opsForZSet().size(PROCESSING_KEY);
        return size != null ? size : 0;
    }

    public long getDlqSize() {
        Long size = redisTemplate.opsForList().size(DLQ_KEY);
        return size != null ? size : 0;
    }

    public List<NotificationQueueItem> getScheduledNotifications() {
        Set<String> keys = redisTemplate.keys(SCHEDULED_KEY + ":*");
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        
        return keys.stream()
                .flatMap(key -> {
                    List<Object> messages = redisTemplate.opsForList().range(key, 0, -1);
                    if (messages == null) return java.util.stream.Stream.empty();
                    return messages.stream()
                            .map(msg -> {
                                String[] parts = msg.toString().split(":");
                                if (parts.length != 2) return null;
                                Long notificationId = Long.parseLong(parts[0]);
                                String timestampStr = key.replace(SCHEDULED_KEY + ":", "");
                                Instant scheduledAt = Instant.ofEpochMilli(Long.parseLong(timestampStr));
                                return new NotificationQueueItem(notificationId, scheduledAt);
                            })
                            .filter(java.util.Objects::nonNull);
                })
                .collect(Collectors.toList());
    }

    public List<NotificationQueueItem> getDlqItems() {
        List<Object> messages = redisTemplate.opsForList().range(DLQ_KEY, 0, -1);
        if (messages == null) return List.of();
        
        return messages.stream()
                .map(msg -> {
                    String[] parts = msg.toString().split(":");
                    if (parts.length < 3) return null;
                    Long notificationId = Long.parseLong(parts[0]);
                    Instant failedAt = Instant.ofEpochMilli(Long.parseLong(parts[1]));
                    String reason = parts.length > 2 ? parts[2] : "Unknown";
                    return new NotificationQueueItem(notificationId, failedAt, reason);
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    public record NotificationQueueItem(Long notificationId, Instant scheduledAt, String reason) {
        public NotificationQueueItem(Long notificationId, Instant scheduledAt) {
            this(notificationId, scheduledAt, null);
        }
    }
}