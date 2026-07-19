package com.conel.market.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record UserProfileUpdateRequest (
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        String phoneNumber,
        LocalDate dateOfBirth
){}
