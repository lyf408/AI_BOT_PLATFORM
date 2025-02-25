package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.exception.ApiException;
import org.example.model.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final VerificationServiceImpl verificationService;
    private final EmailServiceImpl emailService;
    private final UserRepository userRepository;

    public UserServiceImpl(@Lazy PasswordEncoder passwordEncoder, VerificationServiceImpl verificationService, EmailServiceImpl emailService, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.emailService = emailService;
        this.userRepository = userRepository;
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
}