package com.conel.market.models.user;

import com.conel.market.models.user.request.ChangePasswordRequest;
import com.conel.market.models.user.request.UserProfileUpdateRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    void changePassword(ChangePasswordRequest request, String userId);

    void deactivateAccount(String userId);

    void reactivateAccount(String userId);

    void deleteAccount(String userId);

    UserResponse getUserById(String id);

    UserResponse updateProfileInfo(UserProfileUpdateRequest request, String userId);
}
