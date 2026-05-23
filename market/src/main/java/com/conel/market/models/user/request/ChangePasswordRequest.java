package com.conel.market.models.user.request;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordRequest {

    private String currentPassword;

    private String newPassword;

    private String confirmNewPassword;
}
