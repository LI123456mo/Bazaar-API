package com.conel.market.models.user.request;

import java.time.LocalDate;

public record UserProfileUpdateRequest (
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate dateOfBirth
){}
