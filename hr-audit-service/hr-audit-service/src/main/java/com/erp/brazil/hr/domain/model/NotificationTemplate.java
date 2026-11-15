package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Map;

@Entity
@Table(name = "notification_template",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_notification_template_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "uk_notification_template_company_code_locale", columnNames = {"company_id", "code", "locale"})
    },
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

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
    @Column(name = "channel", nullable = false, length = 10)
    private Channel channel;

    @Size(max = 255)
    @Column(name = "subject", length = 255)
    private String subject;

    @NotBlank
    @Column(name = "content_template", nullable = false, columnDefinition = "TEXT")
    private String contentTemplate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 10)
    private ContentType contentType = ContentType.HTML;

    @ElementCollection
    @CollectionTable(name = "notification_template_variables", joinColumns = @JoinColumn(name = "template_id"))
    @MapKeyColumn(name = "variable_key")
    @Column(name = "variable_value")
    @Builder.Default
    private Map<String, String> variables = new java.util.HashMap<>();

    @Size(max = 5)
    @Column(name = "locale", length = 5)
    private String locale = "pt_BR";

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "is_system")
    private boolean isSystem = false;

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

    public enum Channel {
        EMAIL, WHATSAPP, SMS, PUSH, IN_APP
    }

    public enum ContentType {
        TEXT, HTML, MARKDOWN
    }
}