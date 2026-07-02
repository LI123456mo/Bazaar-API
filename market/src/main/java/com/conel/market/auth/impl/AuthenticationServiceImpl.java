package com.conel.market.auth.impl;

import com.conel.market.auth.AuthenticationService;
import com.conel.market.auth.request.AuthenticationRequest;
import com.conel.market.auth.request.RefreshRequest;
import com.conel.market.auth.request.RegistrationRequest;
import com.conel.market.auth.response.AuthenticationResponse;
import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.models.user.UserVerificationService;
import com.conel.market.models.vendor.VendorStatus;
import com.conel.market.security.JwtService;
import com.conel.market.models.user.UserMapper;
import com.conel.market.models.role.Role;
import com.conel.market.models.user.User;
import com.conel.market.models.role.RoleRepository;
import com.conel.market.models.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserVerificationService userVerificationService;

    @Override
    public AuthenticationResponse login(AuthenticationRequest request) {
        final Authentication auth=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        final User user=(User) auth.getPrincipal();
        assert user != null;
        final String token=this.jwtService.generateAccessToken(user.getUsername());
        final String refreshToken=this.jwtService.generateRefreshToken(user.getUsername());
        final String tokenType="Bearer";

        return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .build();
    }

    @Override
    @Transactional
    public void register(RegistrationRequest request) {
        checkUserEmail(request.getEmail());
        checkUserPhoneNumber(request.getPhoneNumber());
        checkPassword(request.getPassword(), request.getConfirmPassword());

        final Role userRole = this.roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new EntityNotFoundException("Role CUSTOMER does not exist"));

        final User user = this.userMapper.toUser(request);
        user.setRoles(List.of(userRole));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVendorStatus(null);
        user.setEmailVerified(false);

        log.debug("Registering customer: {}", user.getEmail());
        User savedUser=this.userRepository.save(user);

        userVerificationService.sendVerificationEmail(savedUser);
        log.info("Verification email send to: {}",savedUser.getEmail());
    }

    @Override
    @Transactional
    public void registerVendor(RegistrationRequest request) {
        checkUserEmail(request.getEmail());
        checkUserPhoneNumber(request.getPhoneNumber());
        checkPassword(request.getPassword(), request.getConfirmPassword());

        final Role vendorRole = this.roleRepository.findByName("VENDOR")
                .orElseThrow(() -> new EntityNotFoundException("Role VENDOR does not exist"));

        final User user = this.userMapper.toUser(request);
        user.setRoles(List.of(vendorRole));
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setVendorStatus(VendorStatus.PENDING_APPROVAL);
        user.setVendorApproved(false);
        user.setEnabled(true);
        user.setEmailVerified(false);

        log.debug("Registering vendor (pending approval): {}", user.getEmail());
        User savedVendor=this.userRepository.save(user);

        userVerificationService.sendVerificationEmail(savedVendor);

        log.info("Verification email send to: {}",savedVendor.getEmail());
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) {
        final String newAccessToken=this.jwtService.refreshAccessToken(request.getRefreshToken());
        final String tokenType="Bearer";
        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType(tokenType)
                .build();
    }



    private void checkUserEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.EMAIL_CANNOT_BE_EMPTY);
        }
        final  boolean emailExist= this.userRepository.existsByEmailIgnoreCase(email);
        if (emailExist){
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
    private void checkUserPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return;
        }
        final boolean phoneNumberExists=this.userRepository.existsByPhoneNumber(phoneNumber);

        if (phoneNumberExists){
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
    }

    private void checkPassword(String password, String confirmPassword) {
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.PASSWORD_REQUIRED);
        }
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
    }
}
