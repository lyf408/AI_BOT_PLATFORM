package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.exception.ApiException;
import org.example.model.dto.userDTO.UpdateProfileRequest;
import org.example.model.dto.userDTO.UserProfileResponse;
import org.example.model.entity.CreditHistory;
import org.example.model.entity.User;
import org.example.repository.CreditHistoryRepository;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final VerificationServiceImpl verificationService;
    private final EmailServiceImpl emailService;
    private final UserRepository userRepository;
    private final CreditHistoryRepository creditHistoryRepository;

    @Value("${path.avatars}")
    private String uploadDir;

    public UserServiceImpl(@Lazy PasswordEncoder passwordEncoder,
                           VerificationServiceImpl verificationService,
                           EmailServiceImpl emailService,
                           UserRepository userRepository,
                           CreditHistoryRepository creditHistoryRepository) {
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.creditHistoryRepository = creditHistoryRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username, username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username or email: " + username);
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }

    @Override
    public void sendVerificationCode(String email) {
        if (verificationService.canSendCode(email)) {
            String code = verificationService.generateVerificationCode(email);
            try {
                emailService.sendEmail(email, "Verification Code",
                        "Your verification code is: " + code);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new ApiException("Please wait before requesting a new code",
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void register(User user, String verificationCode) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new ApiException("Username already exists", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new ApiException("Email already exists", HttpStatus.BAD_REQUEST);
        }
        if (!verificationService.verifyCode(user.getEmail(), verificationCode)) {
            throw new ApiException("Invalid verification code", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Timestamp currentTimestamp = Timestamp.from(Instant.now());
        user.setCreatedAt(currentTimestamp);
        user.setUpdatedAt(currentTimestamp);
        if (user.getRole() == null) {
            user.setRole(User.Role.USER);
        }
        User savedUser = userRepository.save(user);
        verificationService.clearCode(user.getEmail());
    }

    @Override
    public void resetPassword(String email, String verificationCode, String newPassword) {
        if (verificationService.verifyCode(email, verificationCode)) {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new ApiException("User not found", HttpStatus.NOT_FOUND);
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(Timestamp.from(Instant.now()));
            userRepository.save(user);
            verificationService.clearCode(email);
        } else {
            throw new ApiException("Invalid verification code", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public UserProfileResponse getUserProfile(Long userID) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return new UserProfileResponse(user);
    }

    @Override
    public void updateUserProfile(User user, UpdateProfileRequest updateProfileRequest) {
        if (updateProfileRequest.getBio() != null) {
            user.setBio(updateProfileRequest.getBio());
        }
        if (updateProfileRequest.getAvatarUrl() != null) {
            user.setAvatarUrl(updateProfileRequest.getAvatarUrl());
        }
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);
    }

    @Override
    public BigDecimal rechargeCredit(User user, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Amount must be greater than 0", HttpStatus.BAD_REQUEST);
        }
        user.setCredits(user.getCredits().add(amount));
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);
        CreditHistory creditHistory = new CreditHistory();
        creditHistory.setUser(user);
        creditHistory.setDescription("Recharge");
        creditHistory.setAmount(amount);
        creditHistory.setCreatedAt(Timestamp.from(Instant.now()));
        creditHistory.setUpdatedAt(Timestamp.from(Instant.now()));
        creditHistoryRepository.save(creditHistory);
        return user.getCredits();
    }

    @Override
    public String uploadAvatar(MultipartFile newAvatar) {
        if (newAvatar.isEmpty()) {
            throw new ApiException("Avatar is empty", HttpStatus.BAD_REQUEST);
        }
        String contentType = newAvatar.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException("File must be an image", HttpStatus.BAD_REQUEST);
        }
        String newFileName = "avatar_" + UUID.randomUUID() +
                getFileExtension(Objects.requireNonNull(newAvatar.getOriginalFilename()));
        try {
            Path path = Paths.get(uploadDir, newFileName);
            Files.createDirectories(path.getParent());
            newAvatar.transferTo(path);
            return newFileName;
        } catch (Exception e) {
            throw new ApiException("Error saving file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public BigDecimal getCredit(User user, Long userId) {
        if (user.getRole().equals(User.Role.USER) && !user.getUserId().equals(userId)) {
            throw new ApiException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return targetUser.getCredits();
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.'));
    }
}