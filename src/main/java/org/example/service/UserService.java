package org.example.service;

import org.example.model.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    void sendVerificationCode(String email);
    void register(User user, String verificationCode);
    void resetPassword(String email, String verificationCode, String newPassword);
}
