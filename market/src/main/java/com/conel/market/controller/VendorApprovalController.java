package com.conel.market.controller;

import com.conel.market.user.service.vendor.VendorApprovalService;
import com.conel.market.user.dto.response.VendorResponse;
import com.conel.market.entity.vendor.VendorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/vendors")
@RequiredArgsConstructor
public class VendorApprovalController {

    private final VendorApprovalService vendorApprovalService;

    @PostMapping("/{vendorId}/approve")
    @PreAuthorize("hasAuthority('vendor:approve')")
    public ResponseEntity<Void> approveVendor(
            @PathVariable String vendorId
    ){
        vendorApprovalService.approveVendor(vendorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{vendorId}/reject")
    @PreAuthorize("hasAuthority('vendor:approve')")
    public ResponseEntity<Void> rejectVendor(
            @PathVariable String vendorId,
            @RequestParam String reason
    ){
        vendorApprovalService.rejectVendor(vendorId,reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{vendorId}/suspend")
    @PreAuthorize("hasAuthority('vendor:suspend')")
    public ResponseEntity<Void> suspendVendor(
            @PathVariable String vendorId,
            @RequestParam String reason
    ){
        vendorApprovalService.suspendVendor(vendorId,reason);
        return ResponseEntity.noContent().build();
    }


    @GetMapping
    @PreAuthorize("hasAuthority('vendor:read')")
    public ResponseEntity<Page<VendorResponse>> getVendors(
            @RequestParam(required = false) VendorStatus status,
            @PageableDefault(size = 20, sort = "vendorStatus") Pageable pageable
    ) {
        return ResponseEntity.ok(vendorApprovalService.getVendors(status, pageable));
    }
}
