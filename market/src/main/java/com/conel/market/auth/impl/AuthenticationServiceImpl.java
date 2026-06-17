package com.conel.market.auth.impl;

import com.conel.market.auth.AuthenticationService;
import com.conel.market.auth.request.AuthenticationRequest;
import com.conel.market.auth.request.RefreshRequest;
import com.conel.market.auth.request.RegistrationRequest;
import com.conel.market.auth.response.AuthenticationResponse;
import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
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

        final Role userRole = this.roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new EntityNotFoundException("Role User does not exist"));

        final List<Role> roles = new ArrayList<>();
        roles.add(userRole);

        final User user = this.userMapper.toUser(request);
        user.setRoles(roles);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        log.debug("Saving user {}", user);
        this.userRepository.save(user);
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
            return; // Skip if phone number is optional  registration setup
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
