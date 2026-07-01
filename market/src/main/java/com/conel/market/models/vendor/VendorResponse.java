package com.conel.market.models.vendor;

import java.time.LocalDate;

public record VendorResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        LocalDate dateOfBirth,
        VendorStatus vendorStatus,
        boolean vendorApproved,
        int totalProducts
) {}