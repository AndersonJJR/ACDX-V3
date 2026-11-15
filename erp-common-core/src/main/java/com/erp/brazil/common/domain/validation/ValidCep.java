package com.erp.brazil.common.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a string is a valid Brazilian CEP.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BrazilianValidators.CepValidator.class)
public @interface ValidCep {
    String message() default "{brasilian.validators.cep.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}