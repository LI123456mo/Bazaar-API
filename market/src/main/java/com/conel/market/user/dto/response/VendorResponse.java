package com.conel.market.user.dto.response;

import com.conel.market.entity.vendor.VendorStatus;

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