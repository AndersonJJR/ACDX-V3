package com.erp.brazil.hr.infrastructure.notification;

import com.erp.brazil.hr.domain.model.Notification;

public interface NotificationProvider {
    String send(String recipient, String subject, String content);
    Notification.Channel getChannel();
}