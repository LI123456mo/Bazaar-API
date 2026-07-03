package com.conel.market.user.dto.request;

import java.time.LocalDate;

public record UserProfileUpdateRequest (
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate dateOfBirth
){}
