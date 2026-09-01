package com.conel.market.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PaymentInitiationValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPaymentInitiation {
    String message() default "Phone number is required for M-Pesa payments and must be in international format (254...)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
