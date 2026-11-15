package com.erp.brazil.hr.infrastructure.aop;

import com.erp.brazil.hr.application.usecase.AuditEventUseCase;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
public class AuditAspect {

    private final AuditEventUseCase auditEventUseCase;

    public AuditAspect(AuditEventUseCase auditEventUseCase) {
        this.auditEventUseCase = auditEventUseCase;
    }

    @Pointcut("within(@org.springframework.stereotype.Repository *) || within(@org.springframework.stereotype.Service *)")
    public void repositoryOrService() {}

    @Pointcut("execution(* com.erp.brazil.hr..*.save*(..)) || " +
              "execution(* com.erp.brazil.hr..*.delete*(..)) || " +
              "execution(* com.erp.brazil.hr..*.update*(..))")
    public void writeOperations() {}

    @AfterReturning(pointcut = "repositoryOrService() && writeOperations()", returning = "result")
    public void logWriteOperation(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = method.getName();

            String action = determineAction(methodName);
            if (action == null) return;

            Object entity = joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null;
            Object oldValue = null; // Would need @Before for old value

            String entityType = entity != null ? entity.getClass().getSimpleName() : className;
            String entityId = extractEntityId(entity);

            Long companyId = extractCompanyId(entity);
            String userId = extractUserId();
            String ipAddress = extractIpAddress();
            String userAgent = extractUserAgent();
            String correlationId = extractCorrelationId();

            Map<String, Object> newValues = buildNewValues(joinPoint, joinPoint.getArgs(), result);

            auditEventUseCase.logEvent(action, entityType, entityId, companyId, userId, oldValue, newValues,
                ipAddress, userAgent, correlationId);
        } catch (Exception e) {
            // Don't fail the main operation due to audit logging
            System.err.println("Audit logging failed: " + e.getMessage());
        }
    }

    private String determineAction(String methodName) {
        if (methodName.startsWith("save") || methodName.startsWith("create") || methodName.startsWith("insert")) {
            return "CREATE";
        }
        if (methodName.startsWith("update") || methodName.startsWith("modify") || methodName.startsWith("patch")) {
            return "UPDATE";
        }
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "DELETE";
        }
        return null;
    }

    private String extractEntityId(Object entity) {
        if (entity == null) return "unknown";
        try {
            Method idMethod = entity.getClass().getMethod("getId");
            Object id = idMethod.invoke(entity);
            return id != null ? id.toString() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private Long extractCompanyId(Object entity) {
        if (entity == null) return null;
        try {
            Method method = entity.getClass().getMethod("getCompanyId");
            Object value = method.invoke(entity);
            return value != null ? (Long) value : null;
        } catch (Exception e) {
            // Try common field names
            String[] fields = {"getCompanyId", "getCompany_id", "getTenantId", "getTenant_id"};
            for (String field : fields) {
                try {
                    Method method = entity.getClass().getMethod(field);
                    Object value = method.invoke(entity);
                    if (value instanceof Long) return (Long) value;
                } catch (Exception ignored) {}
            }
            return null;
        }
    }

    private String extractUserId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader != null) {
                return userIdHeader;
            }
        }
        return null;
    }

    private String extractIpAddress() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null) return forwarded.split(",")[0].trim();
            return request.getRemoteAddr();
        }
        return null;
    }

    private String extractUserAgent() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return attrs.getRequest().getHeader("User-Agent");
        }
        return "unknown";
    }

    private String extractCorrelationId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String correlationId = attrs.getRequest().getHeader("X-Correlation-Id");
            if (correlationId != null) return correlationId;
        }
        return UUID.randomUUID().toString();
    }

    private Map<String, Object> buildNewValues(JoinPoint joinPoint, Object[] args, Object result) {
        Map<String, Object> values = new LinkedHashMap<>();

        // Add method parameters
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        for (int i = 0; i < args.length && i < paramNames.length; i++) {
            if (args[i] != null && !isPrimitiveOrWrapper(args[i].getClass())) {
                values.put(paramNames[i], args[i]);
            }
        }

        // Add result
        if (result != null) {
            values.put("result", result);
        }

        return values;
    }

    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
            clazz == String.class ||
            clazz == Long.class || clazz == Integer.class || clazz == Short.class || clazz == Byte.class ||
            clazz == Double.class || clazz == Float.class || clazz == Boolean.class || clazz == Character.class ||
            clazz == BigDecimal.class || clazz == BigInteger.class ||
            clazz == java.time.LocalDate.class || clazz == java.time.LocalDateTime.class || clazz == java.time.Instant.class;
    }
}