package org.example.model.dto.authDTO;

import lombok.Getter;

@Getter
public class PasswordResetRequest {
    private String email;
    private String verificationCode;
    private String newPassword;
}
