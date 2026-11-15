package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "notification_template",
    uniqueConstraints = @UniqueConstraint(name = "uk_notification_template_company_code_locale", columnNames = {"company_id", "code", "locale"}),
    indexes = {
        @Index(name = "idx_notification_template_channel", columnList = "channel"),
        @Index(name = "idx_notification_template_active", columnList = "is_active")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplate extends BaseEntity {

    @Column(name = "company_id")
    private Long companyId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Size(max = 255)
    @Column(name = "subject", length = 255)
    private String subject;

    @NotBlank
    @Column(name = "content_template", nullable = false, columnDefinition = "TEXT")
    private String contentTemplate;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 20)
    private ContentType contentType = ContentType.HTML;

    @Type(JsonBinaryType.class)
    @Column(name = "variables", columnDefinition = "jsonb")
    private java.util.List<String> variables;

    @Size(max = 5)
    @Column(name = "locale", length = 5)
    private String locale = "pt_BR";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_system")
    private Boolean isSystem = false;

    public enum ContentType {
        TEXT, HTML, MARKDOWN
    }

    public String render(Map<String, Object> variables) {
        String result = contentTemplate;
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, value);
            }
        }
        return result;
    }

    public String renderSubject(Map<String, Object> variables) {
        if (subject == null) return null;
        String result = subject;
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, value);
            }
        }
        return result;
    }
}