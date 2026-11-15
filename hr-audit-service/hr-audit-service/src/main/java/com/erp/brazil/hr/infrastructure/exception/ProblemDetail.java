package com.erp.brazil.hr.infrastructure.exception;

import lombok.*;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDetail {

    private URI type;
    private String title;
    private int status;
    private String detail;
    private URI instance;
    private Instant timestamp;
    private Map<String, Object> properties;

    public static ProblemDetailBuilder builder() {
        return new ProblemDetailBuilder();
    }

    public static class ProblemDetailBuilder {
        private Map<String, Object> properties = new java.util.HashMap<>();

        public ProblemDetailBuilder property(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }

        public ProblemDetail build() {
            return new ProblemDetail(type, title, status, detail, instance, timestamp, properties);
        }
    }
}