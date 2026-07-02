package com.conel.market.auth;


import com.conel.market.auth.request.AuthenticationRequest;
import com.conel.market.auth.request.RefreshRequest;
import com.conel.market.auth.request.RegistrationRequest;
import com.conel.market.auth.response.AuthenticationResponse;
import com.conel.market.models.user.UserVerificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private  final UserVerificationService userVerificationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
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

    @PostMapping("refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
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
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        userVerificationService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }
}
