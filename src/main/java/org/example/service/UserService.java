package org.example.service;

import org.example.model.dto.userDTO.UpdateProfileRequest;
import org.example.model.dto.userDTO.UserProfileResponse;
import org.example.model.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public interface UserService extends UserDetailsService {
    void sendVerificationCode(String email);
    void register(User user, String verificationCode);
    void resetPassword(String email, String verificationCode, String newPassword);
    UserProfileResponse getUserProfile(Long userID);
    void updateUserProfile(User user, UpdateProfileRequest updateProfileRequest);
    BigDecimal rechargeCredit(User user, BigDecimal amount);
    String uploadAvatar(MultipartFile newAvatar);
    BigDecimal getCredit(User user, Long userId);
}
