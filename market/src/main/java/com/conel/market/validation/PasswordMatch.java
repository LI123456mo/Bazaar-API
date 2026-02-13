package com.conel.market.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})//Stamp on top of a class
@Retention(RetentionPolicy.RUNTIME)//to be considered while app run(retained)
@Constraint(validatedBy = PasswordMatchValidator.class)
public @interface PasswordMatch {
    String message() default "Password do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
