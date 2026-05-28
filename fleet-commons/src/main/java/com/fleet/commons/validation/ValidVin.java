package com.fleet.commons.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "VIN must be exactly 17 alphanumeric characters (excluding I, O, Q)")
public @interface ValidVin {
    String message() default "Invalid VIN format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
