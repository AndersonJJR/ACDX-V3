package com.erp.brazil.hr.infrastructure.messaging;

import com.erp.brazil.hr.domain.model.WebhookSubscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Component
public class WebhookDeliveryService {

    private final RestClient restClient;

    public WebhookDeliveryService(@Value("${webhook.timeout.seconds:30}") int timeoutSeconds) {
        this.restClient = RestClient.builder()
            .requestFactory(request -> {
                var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
                factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
                factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
                return factory.createRequest(null, null);
            })
            .build();
    }

    public DeliveryResult deliver(WebhookSubscription subscription, WebhookDelivery delivery) {
        try {
            String signature = generateSignature(subscription.secret(), delivery.payload());

            var response = restClient.post()
                .uri(subscription.url())
                .header("Content-Type", "application/json")
                .header("X-Webhook-Event", delivery.eventType())
                .header("X-Webhook-Signature", signature)
                .header("X-Webhook-Delivery", delivery.uuid().toString())
                .headers(headers -> {
                    if (subscription.headers() != null) {
                        subscription.headers().forEach(headers::add);
                    }
                })
                .body(delivery.payload())
                .retrieve()
                .toEntity(String.class);

            return new DeliveryResult(true, response.getStatusCode().value(), response.getBody(), response.getHeaders().toSingleValueMap());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            return new DeliveryResult(false, e.getStatusCode().value(), e.getResponseBodyAsString(), e.getResponseHeaders().toSingleValueMap());
        } catch (Exception e) {
            return new DeliveryResult(false, null, e.getMessage(), null);
        }
    }

    private String generateSignature(String secret, Map<String, Object> payload) {
        try {
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
            javax.crypto.Mac hmac = javax.crypto.Mac.getInstance("HmacSHA256");
            hmac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            byte[] hash = hmac.doFinal(json.getBytes());
            return "sha256=" + bytesToHex(hash);
        } catch (Exception e) {
            return "";
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public record DeliveryResult(boolean success, Integer statusCode, String body, Map<String, String> headers) {}
}