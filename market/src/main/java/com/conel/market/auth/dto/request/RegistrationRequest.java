package com.conel.market.auth.dto.request;

import com.conel.market.validation.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@PasswordMatch
public class RegistrationRequest {

    @NotBlank
    String firstName;

    @NotBlank
    String lastName;

    @NotBlank
    @Email
    String email;

    String phoneNumber;

    @NotBlank
    String password;

    @NotBlank
    String confirmPassword;
}
