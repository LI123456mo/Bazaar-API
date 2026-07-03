package com.conel.market.user.dto.response;

import java.time.LocalDate;

public record UserResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        LocalDate dateOfBirth,
        boolean emailVerified,
        boolean phoneVerified,
        boolean enabled,
        String vendorStatus
) {}
