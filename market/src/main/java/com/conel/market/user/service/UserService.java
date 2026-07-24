package com.conel.market.user.service;

import com.conel.market.entity.vendor.VendorStatus;
import com.conel.market.user.dto.response.UserResponse;
import com.conel.market.user.dto.request.ChangePasswordRequest;
import com.conel.market.user.dto.request.UserProfileUpdateRequest;
import com.conel.market.user.dto.response.VendorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    void changePassword(ChangePasswordRequest request, String userId);

    void deactivateAccount(String userId);

    void reactivateAccount(String userId);

    void deleteAccount(String userId);

    UserResponse getUserById(String id);

    Page<UserResponse> findAll(Pageable pageable);

    UserResponse updateProfileInfo(UserProfileUpdateRequest request, String userId);
}
