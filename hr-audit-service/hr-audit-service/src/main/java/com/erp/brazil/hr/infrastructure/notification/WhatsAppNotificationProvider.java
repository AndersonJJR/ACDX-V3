package com.erp.brazil.hr.infrastructure.notification;

import com.erp.brazil.hr.application.usecase.SendNotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WhatsAppNotificationProvider implements SendNotificationUseCase.WhatsAppNotificationProvider {

    private final RestTemplate restTemplate;

    @Value("${app.notifications.whatsapp.api-url:https://graph.facebook.com/v18.0}")
    private String apiUrl;

    @Value("${app.notifications.whatsapp.api-token:}")
    private String apiToken;

    @Value("${app.notifications.whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Override
    public void send(String phone, String content) {
        if (phone == null || phone.isBlank()) {
            log.warn("Cannot send WhatsApp: phone number is empty");
            return;
        }

        if (apiToken.isBlank() || phoneNumberId.isBlank()) {
            log.warn("WhatsApp API not configured, skipping send");
            return;
        }

        String cleanPhone = phone.replaceAll("[^0-9]", "");
        if (!cleanPhone.startsWith("55")) {
            cleanPhone = "55" + cleanPhone; // Add Brazil country code
        }

        try {
            String url = apiUrl + "/" + phoneNumberId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiToken);

            Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", cleanPhone,
                "type", "text",
                "text", Map.of("body", content)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(url, request, Map.class);

            log.debug("WhatsApp message sent to {}", cleanPhone);

        } catch (Exception e) {
            log.error("Failed to send WhatsApp to {}", phone, e);
            throw new RuntimeException("Failed to send WhatsApp message", e);
        }
    }

    public void sendTemplate(String phone, String templateName, Map<String, String> parameters) {
        if (phone == null || phone.isBlank()) {
            log.warn("Cannot send WhatsApp template: phone number is empty");
            return;
        }

        if (apiToken.isBlank() || phoneNumberId.isBlank()) {
            log.warn("WhatsApp API not configured, skipping send");
            return;
        }

        String cleanPhone = phone.replaceAll("[^0-9]", "");
        if (!cleanPhone.startsWith("55")) {
            cleanPhone = "55" + cleanPhone;
        }

        try {
            String url = apiUrl + "/" + phoneNumberId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiToken);

            // Build template components
            var components = parameters.entrySet().stream()
                .map(entry -> Map.of(
                    "type", "body",
                    "parameters", List.of(Map.of("type", "text", "text", entry.getValue()))
                ))
                .toList();

            Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", cleanPhone,
                "type", "template",
                "template", Map.of(
                    "name", templateName,
                    "language", Map.of("code", "pt_BR"),
                    "components", components
                )
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(url, request, Map.class);

            log.debug("WhatsApp template sent to {}", cleanPhone);

        } catch (Exception e) {
            log.error("Failed to send WhatsApp template to {}", phone, e);
            throw new RuntimeException("Failed to send WhatsApp template", e);
        }
    }
}