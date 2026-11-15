package com.erp.brazil.common.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a string is a valid Brazilian State Registration (Inscrição Estadual).
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BrazilianValidators.StateRegistrationValidator.class)
public @interface ValidStateRegistration {
    String message() default "{brasilian.validators.stateRegistration.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    /**
     * The state abbreviation (UF) for validation.
     * Example: "SP", "RJ", "MG", etc.
     */
    String uf() default "";
}