package com.conel.market.validation;

import com.conel.market.dto.payment.PaymentInitiationRequest;
import com.conel.market.entity.payment.PaymentMethod;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PaymentInitiationValidator
        implements ConstraintValidator<ValidPaymentInitiation, PaymentInitiationRequest> {

    @Override
    public boolean isValid(PaymentInitiationRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;

        if (request.paymentMethod() == PaymentMethod.M_PESA) {
            if (request.phoneNumber() == null || request.phoneNumber().isBlank()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Phone number is required for M-Pesa payments"
                ).addPropertyNode("phoneNumber").addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}