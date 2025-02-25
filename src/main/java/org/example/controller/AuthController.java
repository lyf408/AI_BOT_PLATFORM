package org.example.controller;

import dev.langchain4j.agent.tool.P;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.model.dto.authDTO.LoginRequest;
import org.example.model.dto.authDTO.PasswordResetRequest;
import org.example.model.dto.authDTO.RegisterRequest;
import org.example.model.entity.User;
import org.example.service.UserService;
import org.example.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "APIs for login, register, password reset and email verification")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/verification")
    @Operation(summary = "Send verification code", description = "Send verification code to email", operationId = "1")
    public ResponseEntity<?> verification(@RequestParam String email) {
        userService.sendVerificationCode(email);
        return ResponseEntity.ok("success sent to " + email);
    }

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Register new user with username, email, password and verification code")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        userService.register(user, registerRequest.getVerificationCode());
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Login user with username or email and password")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Incorrect username or password", HttpStatus.BAD_REQUEST);
        }
        UserDetails userDetails = null;
        try {
            userDetails = userService.loadUserByUsername(loginRequest.getUsernameOrEmail());
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        }
        final String jwt = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password with email, verification code and new password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        userService.resetPassword(request.getEmail(), request.getVerificationCode(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successfully");
    }
}
