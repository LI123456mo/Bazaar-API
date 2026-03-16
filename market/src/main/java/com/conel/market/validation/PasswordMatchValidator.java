package com.conel.market.validation;

import com.conel.market.config.auth.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch,Object> {
    @Override
    public void initialize(PasswordMatch constraintAnnotation){

    }
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context){
        if (!(value instanceof RegisterRequest request)){
            return false;
        }
        return request.getPassword() != null && request.getPassword().equals(request.getConfirmPassword());
    }
}
