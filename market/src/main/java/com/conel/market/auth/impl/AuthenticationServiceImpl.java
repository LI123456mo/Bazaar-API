package com.conel.market.auth.impl;

import com.conel.market.auth.AuthenticationService;
import com.conel.market.auth.request.AuthenticationRequest;
import com.conel.market.auth.request.RefreshRequest;
import com.conel.market.auth.request.RegistrationRequest;
import com.conel.market.auth.response.AuthenticationResponse;
import com.conel.market.security.JwtService;
import com.conel.market.mapper.UserMapper;
import com.conel.market.models.Role;
import com.conel.market.models.User;
import com.conel.market.repositories.RoleRepository;
import com.conel.market.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthenticationResponse login(AuthenticationRequest request) {
        final Authentication auth=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        final User user=(User) auth.getPrincipal();
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
        checkPassword(request.getPassword(),request.getConfirmPassword());
        checkUserPhoneNumber(request.getPhoneNumber());

        final Role userRole=this.roleRepository.findByName("ROLE_USER")
                .orElseThrow(()->new EntityNotFoundException("Role User does not exist"));

        final List<Role> roles=new ArrayList<>();
        roles.add(userRole);

        final User user=this.userMapper.toUser(request);
        user.setRoles(roles);
        log.debug("saving user {}",user);
        this.userRepository.save(user);

        final List<User> users=new ArrayList<>();
        users.add(user);
        userRole.setUsers(users);
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
        final  boolean emailExist= this.userRepository.existsByEmailIgnoreCase(email);
        if (emailExist){

        }
    }
    private void checkUserPhoneNumber(String phoneNumber) {
        final boolean phoneNumberExists=this.userRepository.existsByPhoneNumber(phoneNumber);

        if (phoneNumberExists){

        }
    }

    private void checkPassword(String password, String confirmPassword) {
        if (password==null || !password.equals(confirmPassword)){

        }
    }
}
