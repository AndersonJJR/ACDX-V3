package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.NotificationTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.Map;
import java.util.UUID;

public record NotificationTemplateDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
    Long companyId,
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 100) String name,
    @NotNull NotificationTemplate.Channel channel,
    String subject,
    @NotBlank String contentTemplate,
    NotificationTemplate.ContentType contentType,
    Map<String, String> variables,
    String locale,
    boolean isActive,
    boolean isSystem
) {
    public static NotificationTemplateDTO from(NotificationTemplate template) {
        return new NotificationTemplateDTO(
            template.uuid(), template.companyId(), template.code(), template.name(), template.channel(),
            template.subject(), template.contentTemplate(), template.contentType(), template.variables(),
            template.locale(), template.isActive(), template.isSystem()
        );
    }
}