package com.conel.market.models.user;


import com.conel.market.auth.request.RegistrationRequest;
import com.conel.market.models.user.request.UserProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public User toUser(final RegistrationRequest request) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(this.passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .locked(false)
                .credentialsExpired(false)
                .emailVerified(false)
                .phoneVerified(false)
                .build();
    }

    public void mergeUserInfo(final User user, final UserProfileUpdateRequest request) {
        if (StringUtils.isNotBlank(request.firstName()) && !user.getFirstName()
                                                                   .equals(request.firstName())) {
            user.setFirstName(request.firstName());
        }
        if (StringUtils.isNotBlank(request.lastName()) && !user.getLastName()
                                                                  .equals(request.lastName())) {
            user.setLastName(request.lastName());
        }
        if (request.dateOfBirth() != null && !request.dateOfBirth()
                                                        .equals(user.getDateOfBirth())) {
            user.setDateOfBirth(request.dateOfBirth());
        }
    }

    public UserResponse toUserResponse(User currentUser) {
        return new UserResponse(
                currentUser.getId(),
                currentUser.getFirstName(),
                currentUser.getLastName(),
                currentUser.getEmail(),
                currentUser.getPhoneNumber(),
                currentUser.getDateOfBirth(),
                currentUser.isEmailVerified(),
                currentUser.isPhoneVerified(),
                currentUser.isEnabled(),
                currentUser.getVendorStatus() != null
                        ? currentUser.getVendorStatus().name()
                        : null  // ← vendorStatus is String in DTO but VendorStatus enum on User
        );
    }
}
