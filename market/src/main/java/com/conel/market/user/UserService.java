package com.conel.market.user;

import com.conel.market.user.request.ChangePasswordRequest;
import com.conel.market.user.request.ProfileUpdateRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    void updateProfileInfo(ProfileUpdateRequest request, String userId);

    void changePassword(ChangePasswordRequest request, String userId);

    void deactivateAccount(String userId);

    void reactivateAccount(String userId);

    void deleteAccount(String userId);
}
