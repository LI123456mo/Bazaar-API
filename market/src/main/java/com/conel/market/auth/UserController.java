package com.conel.market.auth;

import com.conel.market.config.auth.AuthenticationRequest;
import com.conel.market.config.auth.AuthenticationResponse;
import com.conel.market.config.auth.AuthenticationService;
import com.conel.market.config.auth.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag()
public class UserController {
}
