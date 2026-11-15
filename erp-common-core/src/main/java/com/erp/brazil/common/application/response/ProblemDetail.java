package com.erp.brazil.common.application.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RFC 7807 Problem Details for HTTP APIs.
 * Standardized error response format for REST APIs.
 */
@Getter
@Setter
@Builder
public class ProblemDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private URI type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private OffsetDateTime timestamp;
    private Map<String, Object> extensions;

    public ProblemDetail() {
        this.timestamp = OffsetDateTime.now();
        this.extensions = new LinkedHashMap<>();
    }

    public ProblemDetail(URI type, String title, int status, String detail, String instance) {
        this();
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
    }

    public ProblemDetail(URI type, String title, int status, String detail, String instance,
                         OffsetDateTime timestamp, Map<String, Object> extensions) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.timestamp = timestamp != null ? timestamp : OffsetDateTime.now();
        this.extensions = extensions != null ? extensions : new LinkedHashMap<>();
    }

    /**
     * Adds an extension property.
     *
     * @param key the property name
     * @param value the property value
     * @return this for chaining
     */
    public ProblemDetail withExtension(String key, Object value) {
        this.extensions.put(key, value);
        return this;
    }

    /**
     * Adds multiple extension properties.
     *
     * @param extensions the properties to add
     * @return this for chaining
     */
    public ProblemDetail withExtensions(Map<String, Object> extensions) {
        this.extensions.putAll(extensions);
        return this;
    }

    /**
     * Creates a ProblemDetail for a validation error.
     *
     * @param detail the error detail
     * @param instance the request path
     * @param violations the validation violations
     * @return problem detail
     */
    public static ProblemDetail validationError(String detail, String instance,
                                                 Map<String, String> violations) {
        return ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/validation-error"))
                .title("Validation Error")
                .status(400)
                .detail(detail)
                .instance(instance)
                .extensions(Map.of("violations", (Object) violations))
                .build();
    }

    /**
     * Creates a ProblemDetail for a not found error.
     *
     * @param resource the resource type
     * @param identifier the resource identifier
     * @param instance the request path
     * @return problem detail
     */
    public static ProblemDetail notFound(String resource, String identifier, String instance) {
        return ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/resource-not-found"))
                .title("Resource Not Found")
                .status(404)
                .detail(resource + " with identifier '" + identifier + "' not found")
                .instance(instance)
                .extensions(Map.of("resource", (Object) resource, "identifier", (Object) identifier))
                .build();
    }

    /**
     * Creates a ProblemDetail for a conflict error.
     *
     * @param detail the error detail
     * @param instance the request path
     * @return problem detail
     */
    public static ProblemDetail conflict(String detail, String instance) {
        return ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/conflict"))
                .title("Conflict")
                .status(409)
                .detail(detail)
                .instance(instance)
                .build();
    }

    /**
     * Creates a ProblemDetail for an unauthorized error.
     *
     * @param detail the error detail
     * @param instance the request path
     * @return problem detail
     */
    public static ProblemDetail unauthorized(String detail, String instance) {
        return ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/unauthorized"))
                .title("Unauthorized")
                .status(401)
                .detail(detail)
                .instance(instance)
                .build();
    }

    /**
     * Creates a ProblemDetail for a forbidden error.
     *
     * @param detail the error detail
     * @param instance the request path
     * @return problem detail
     */
    public static ProblemDetail forbidden(String detail, String instance) {
        return ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/forbidden"))
                .title("Forbidden")
                .status(403)
                .detail(detail)
                .instance(instance)
                .build();
    }

    /**
     * Creates a ProblemDetail for an internal server error.
     *
     * @param detail the error detail
     * @param instance the request path
     * @return problem detail
     */
    public static ProblemDetail internalError(String detail, String instance) {
        return ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/internal-error"))
                .title("Internal Server Error")
                .status(500)
                .detail(detail)
                .instance(instance)
                .build();
    }

    /**
     * Creates a ProblemDetail for a service unavailable error.
     *
     * @param detail the error detail
     * @param instance the request path
     * @return problem detail
     */
    public static ProblemDetail serviceUnavailable(String detail, String instance) {
        return ProblemDetail.builder()
                .type(URI.create("https://erp.brazil.com/problems/service-unavailable"))
                .title("Service Unavailable")
                .status(503)
                .detail(detail)
                .instance(instance)
                .build();
    }
}