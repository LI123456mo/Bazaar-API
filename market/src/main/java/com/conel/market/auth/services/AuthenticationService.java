package com.conel.market.auth.services;

import com.conel.market.auth.dto.request.AuthenticationRequest;
import com.conel.market.auth.dto.request.RefreshRequest;
import com.conel.market.auth.dto.request.RegistrationRequest;
import com.conel.market.auth.dto.response.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {

    AuthenticationResponse login(AuthenticationRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    void register(RegistrationRequest request);

    void registerVendor(RegistrationRequest request);

    AuthenticationResponse refreshToken(RefreshRequest request);
}
