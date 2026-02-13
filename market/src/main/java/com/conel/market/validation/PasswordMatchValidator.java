package com.conel.market.validation;

import com.conel.market.dto.UserDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch,Object> {
    @Override
    public void initialize(PasswordMatch constraintAnnotation){

    }
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context){
        UserDto userDto=(UserDto) value;
        return userDto.password() != null && userDto.password().equals(userDto.confirmPassword());
    }
}
