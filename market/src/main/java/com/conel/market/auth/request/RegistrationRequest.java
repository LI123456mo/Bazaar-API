package com.conel.market.auth.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationRequest {

    String firstName;

    String lastName;

    String email;

    String phoneNumber;

    String password;

    String confirmPassword;
}
