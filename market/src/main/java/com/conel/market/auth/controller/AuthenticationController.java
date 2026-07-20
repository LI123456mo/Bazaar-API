package com.conel.market.auth.controller;


import com.conel.market.auth.services.AuthenticationService;
import com.conel.market.auth.dto.request.AuthenticationRequest;
import com.conel.market.auth.dto.request.RefreshRequest;
import com.conel.market.auth.dto.request.RegistrationRequest;
import com.conel.market.auth.dto.response.AuthenticationResponse;
import com.conel.market.emailVerification.UserVerificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication", description = "Authentication API")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private  final UserVerificationService userVerificationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid
            @RequestBody
            final AuthenticationRequest request
    ){
        return ResponseEntity.ok(this.authenticationService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<Void>  register(
            @Valid
            @RequestBody
            final RegistrationRequest request){
        this.authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @Valid
            @RequestBody
            final RefreshRequest request
            ){
        return ResponseEntity.ok(this.authenticationService.refreshToken(request));
    }

    @PostMapping("/register-vendor")
    public ResponseEntity<Void> registerVendor(@Valid @RequestBody RegistrationRequest request) {
        authenticationService.registerVendor(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam @NotBlank String token) {
        userVerificationService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }
}
