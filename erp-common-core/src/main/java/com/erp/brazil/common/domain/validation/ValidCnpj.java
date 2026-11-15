package com.erp.brazil.common.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a string is a valid Brazilian CNPJ.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BrazilianValidators.CnpjValidator.class)
public @interface ValidCnpj {
    String message() default "{brasilian.validators.cnpj.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}