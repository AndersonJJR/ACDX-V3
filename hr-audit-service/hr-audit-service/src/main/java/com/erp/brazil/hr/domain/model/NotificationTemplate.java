package com.erp.brazil.hr.domain.model;

import java.util.Map;
import java.util.UUID;

public record NotificationTemplate(
    Long id,
    UUID uuid,
    Long companyId,
    String code,
    String name,
    Notification.Channel channel,
    String subject,
    String contentTemplate,
    ContentType contentType,
    Map<String, String> variables,
    String locale,
    boolean isActive,
    boolean isSystem
) {
    public enum ContentType {
        TEXT, HTML, MARKDOWN
    }

    public String render(Map<String, Object> context) {
        String result = contentTemplate;
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = context.getOrDefault(entry.getKey(), entry.getValue()).toString();
                result = result.replace(placeholder, value);
            }
        }
        return result;
    }

    public String renderSubject(Map<String, Object> context) {
        if (subject == null) return "";
        String result = subject;
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = context.getOrDefault(entry.getKey(), entry.getValue()).toString();
                result = result.replace(placeholder, value);
            }
        }
        return result;
    }
}