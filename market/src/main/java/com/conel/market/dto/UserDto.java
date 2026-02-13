package com.conel.market.dto;

import com.conel.market.validation.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatch
public record UserDto(
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name is required")
        String lastName,
        @Email(message = "Please provide a valid email address")
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,
        String confirmPassword,
        String role
        ) {
}
