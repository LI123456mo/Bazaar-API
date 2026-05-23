package com.conel.market.models.user.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileUpdateRequest {

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;
}
