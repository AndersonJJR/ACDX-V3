package com.erp.brazil.hr.infrastructure.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WhatsAppProvider implements NotificationProvider {

    private final RestClient restClient;
    private final String apiUrl;
    private final String apiToken;

    public WhatsAppProvider(@Value("${whatsapp.api.url:https://api.zenvia.com/v2}") String apiUrl,
                            @Value("${whatsapp.api.token:}") String apiToken) {
        this.apiUrl = apiUrl;
        this.apiToken = apiToken;
        this.restClient = RestClient.builder()
            .baseUrl(apiUrl)
            .defaultHeader("Authorization", "Bearer " + apiToken)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    @Override
    public String send(String to, String subject, String content) {
        // Clean phone number (remove non-digits)
        String phone = to.replaceAll("\\D", "");
        if (!phone.startsWith("55")) {
            phone = "55" + phone; // Add Brazil country code
        }

        try {
            WhatsAppRequest request = new WhatsAppRequest(
                phone,
                new WhatsAppMessage("text", content)
            );

            WhatsAppResponse response = restClient.post()
                .uri("/channels/whatsapp/messages")
                .body(request)
                .retrieve()
                .body(WhatsAppResponse.class);

            return response != null ? response.id() : UUID.randomUUID().toString();
        } catch (Exception e) {
            throw new NotificationException("Failed to send WhatsApp message", e);
        }
    }

    @Override
    public Notification.Channel getChannel() {
        return Notification.Channel.WHATSAPP;
    }

    private record WhatsAppRequest(String to, WhatsAppMessage contents) {}
    private record WhatsAppMessage(String type, String text) {}
    private record WhatsAppResponse(String id, String status) {}
}