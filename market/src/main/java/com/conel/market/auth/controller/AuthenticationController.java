package com.conel.market.auth.controller;


import com.conel.market.auth.services.AuthenticationService;
import com.conel.market.auth.dto.request.AuthenticationRequest;
import com.conel.market.auth.dto.request.RefreshRequest;
import com.conel.market.auth.dto.request.RegistrationRequest;
import com.conel.market.auth.dto.response.AuthenticationResponse;
import com.conel.market.emailVerification.UserVerificationService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(
            summary = "Login",
            description = "Authenticate user and return JWT tokens"
    )
    public ResponseEntity<AuthenticationResponse> login(
            @Valid
            @RequestBody
            final AuthenticationRequest request
    ){
        return ResponseEntity.ok(this.authenticationService.login(request));
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register Customer",
            description = "Register a new customer account"
    )
    public ResponseEntity<Void>  register(
            @Valid
            @RequestBody
            final RegistrationRequest request){
        this.authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/refresh")
    @Operation (
            summary = "Refresh Token",
            description = "Refresh JWT access token using a valid refresh token"
    )
    public ResponseEntity<AuthenticationResponse> refresh(
            @Valid
            @RequestBody
            final RefreshRequest request
            ){
        return ResponseEntity.ok(this.authenticationService.refreshToken(request));
    }

    @PostMapping("/register-vendor")
    @Operation(
            summary = "Register Vendor",
            description = "Register a new vendor account — requires admin approval before selling"
    )
    public ResponseEntity<Void> registerVendor(@Valid @RequestBody RegistrationRequest request) {
        authenticationService.registerVendor(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping("/verify-email")
    @Operation(
            summary = "Verify Email",
            description = "Verify email address using token sent to user's email"
    )
    public ResponseEntity<Void> verifyEmail(@RequestParam @NotBlank String token) {
        userVerificationService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    /*@PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email")
    public ResponseEntity<Void> resendVerification(
            @RequestParam @NotBlank String email) {
        userVerificationService.resendVerificationEmail(email);
        return ResponseEntity.ok().build();
    }

    // User forgot password → sends reset link to email
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<Void> forgotPassword(
            @RequestParam @NotBlank String email) {
        authenticationService.forgotPassword(email);
        return ResponseEntity.ok().build();
    }

    // User clicks link in email → sets new password
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password")
    public ResponseEntity<Void> resetPassword(
            @RequestParam @NotBlank String token,
            @RequestParam @NotBlank String newPassword) {
        authenticationService.resetPassword(token, newPassword);
        return ResponseEntity.ok().build();
    }*/
}
