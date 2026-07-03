package com.conel.market.user.service;

import com.conel.market.user.dto.response.UserResponse;
import com.conel.market.user.dto.request.ChangePasswordRequest;
import com.conel.market.user.dto.request.UserProfileUpdateRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    void changePassword(ChangePasswordRequest request, String userId);

    void deactivateAccount(String userId);

    void reactivateAccount(String userId);

    void deleteAccount(String userId);

    UserResponse getUserById(String id);

    UserResponse updateProfileInfo(UserProfileUpdateRequest request, String userId);
}
