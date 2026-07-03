package com.conel.market.user.service.vendor;


import com.conel.market.entity.vendor.VendorStatus;
import com.conel.market.user.dto.response.VendorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VendorApprovalService {

    void approveVendor(String Id);

    void rejectVendor(String Id,String reason);

    void suspendVendor(String Id,String reason);

    Page<VendorResponse> getVendors(VendorStatus status, Pageable pageable);
}
