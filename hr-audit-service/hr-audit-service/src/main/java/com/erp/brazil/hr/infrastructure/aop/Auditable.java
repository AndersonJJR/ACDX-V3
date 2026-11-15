package com.erp.brazil.hr.infrastructure.aop;

import com.erp.brazil.hr.domain.model.AuditLog;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String value() default "";
    String entityType() default "";
    AuditLog.RiskLevel riskLevel() default AuditLog.RiskLevel.LOW;
    String[] complianceTags() default {};
}