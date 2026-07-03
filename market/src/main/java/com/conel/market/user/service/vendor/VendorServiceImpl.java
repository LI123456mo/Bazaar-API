package com.conel.market.user.service.vendor;

import com.conel.market.entity.vendor.VendorStatus;
import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.user.entity.User;
import com.conel.market.user.dto.response.VendorResponse;
import com.conel.market.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorApprovalService {

   private final UserRepository userRepository;

    @Override
    @Transactional
    public void approveVendor(String id) {
        User vendor=userRepository.findById(id)
                .orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));

        vendor.setVendorStatus(VendorStatus.APPROVED);
        vendor.setVendorApproved(true);
        vendor.setEnabled(true);

        userRepository.save(vendor);
    }

    @Override
    public void rejectVendor(String vendorId, String reason) {
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));


        if (vendor.getVendorStatus()!=VendorStatus.PENDING_APPROVAL){
            throw new BusinessException(ErrorCode.INVALID_VENDOR_STATUS);
        }

        vendor.setVendorStatus(VendorStatus.REJECTED);
        vendor.setVendorApproved(false);

        vendor.setEnabled(false);
        vendor.setLocked(true);

        // TODO: send rejection email with reason to vendor
        // emailService.sendVendorRejectionEmail(vendor.getEmail(), reason);

        userRepository.save(vendor);
    }

    @Override
    public void suspendVendor(String vendorId, String reason) {
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Can only suspend an APPROVED vendor
        if (vendor.getVendorStatus() != VendorStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_VENDOR_STATUS);
        }

        vendor.setVendorStatus(VendorStatus.SUSPENDED);
        vendor.setVendorApproved(false);

        // Disable account — vendor can't log in while suspended
        vendor.setEnabled(false);

        // TODO: send suspension email with reason to vendor
        // emailService.sendVendorSuspensionEmail(vendor.getEmail(), reason);

        userRepository.save(vendor);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<VendorResponse> getVendors(VendorStatus status, Pageable pageable) {
        return userRepository.findAllVendors(status, pageable)
                .map(this::toVendorResponse);
    }


    private VendorResponse toVendorResponse(User user) {
        return new VendorResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getDateOfBirth(),
                user.getVendorStatus(),
                user.isVendorApproved(),
                user.getProducts().size()
        );
    }
}
