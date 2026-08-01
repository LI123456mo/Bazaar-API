package com.conel.market.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries both password values so the reset endpoint can validate the new credential without exposing any account state.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetRequest {

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}
