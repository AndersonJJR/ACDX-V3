package com.erp.brazil.hr.infrastructure.notification;

import org.springframework.stereotype.Component;

@Component
public class PushNotificationProvider implements NotificationProvider {

    @Override
    public String send(String recipient, String subject, String content) {
        // In a real implementation, this would integrate with Firebase, OneSignal, etc.
        // For now, we'll simulate sending a push notification
        System.out.println("PUSH NOTIFICATION - To: " + recipient + ", Subject: " + subject);
        return "push-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public Notification.Channel getChannel() {
        return Notification.Channel.PUSH;
    }
}