package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.WebhookSubscription;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record WebhookSubscriptionDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
    @NotNull Long companyId,
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 500) @Pattern(regexp = "^https?://.+") String url,
    @NotBlank @Size(max = 255) String secret,
    @NotEmpty List<String> events,
    Map<String, String> headers,
    boolean isActive,
    WebhookSubscription.RetryPolicy retryPolicy
) {
    public static WebhookSubscriptionDTO from(WebhookSubscription sub) {
        return new WebhookSubscriptionDTO(
            sub.uuid(), sub.companyId(), sub.name(), sub.url(), sub.secret(), sub.events(),
            sub.headers(), sub.isActive(), sub.retryPolicy()
        );
    }
}