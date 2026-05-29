package com.fleet.commons.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
// Doubled the backslash before 'd' here:
@Pattern(regexp = "^[A-Z]{2}-\\d{3}-[A-Z]{2}$", message = "Plate number must follow French format: AA-123-AA")
public @interface ValidPlateNumber {
    String message() default "Invalid plate number format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}