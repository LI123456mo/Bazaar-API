package com.conel.market.auth;

import com.conel.market.auth.request.AuthenticationRequest;
import com.conel.market.auth.request.RefreshRequest;
import com.conel.market.auth.request.RegistrationRequest;
import com.conel.market.auth.response.AuthenticationResponse;

public interface AuthenticationService {

    AuthenticationResponse login(AuthenticationRequest request);

    void register(RegistrationRequest request);

    void registerVendor(RegistrationRequest request);

    AuthenticationResponse refreshToken(RefreshRequest request);
}
