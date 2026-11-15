package com.erp.brazil.common.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a string is a valid Brazilian CPF.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BrazilianValidators.CpfValidator.class)
public @interface ValidCpf {
    String message() default "{brasilian.validators.cpf.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}