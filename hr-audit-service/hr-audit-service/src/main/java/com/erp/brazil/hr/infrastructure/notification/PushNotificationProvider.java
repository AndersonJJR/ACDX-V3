package com.erp.brazil.hr.infrastructure.notification;

import com.erp.brazil.hr.application.usecase.SendNotificationUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PushNotificationProvider implements SendNotificationUseCase.PushNotificationProvider {

    @Override
    public void send(Long recipientId, String title, String content) {
        // Integration with Firebase Cloud Messaging (FCM), OneSignal, or similar
        log.debug("Push notification would be sent to user {}: {}", recipientId, title);

        // TODO: Implement actual push notification sending
        // Example with FCM:
        // FirebaseMessaging.getInstance().send(Message.builder()
        //     .setToken(getUserFcmToken(recipientId))
        //     .setNotification(Notification.builder().setTitle(title).setBody(content).build())
        //     .build());
    }
}