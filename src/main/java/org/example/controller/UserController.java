package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.example.model.dto.userDTO.UpdateProfileRequest;
import org.example.model.dto.userDTO.UserProfileResponse;
import org.example.model.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User", description = "APIs for user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get user's own profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        UserProfileResponse userProfile = userService.getUserProfile(user.getUserId());
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/profile/{userId}")
    @Operation(summary = "Get user profile by user ID", description = "Get user profile by user ID")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse userProfile = userService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    @PostMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update user profile")
    public ResponseEntity<?> updateUserProfile(@AuthenticationPrincipal UserDetails userDetails,
                                               @RequestBody UpdateProfileRequest updateProfileRequest) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        userService.updateUserProfile(user, updateProfileRequest);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @PutMapping(value = "/avatar", consumes = {"multipart/form-data"})
    @Operation(summary = "Upload avatar", description = "Upload user avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile newAvatar) {
        String fileName = userService.uploadAvatar(newAvatar);
        return ResponseEntity.ok(fileName);
    }

    @PostMapping("/recharge")
    @Operation(summary = "Recharge credit", description = "Recharge user credit")
    public ResponseEntity<?> rechargeCredit(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam BigDecimal amount) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        BigDecimal credit = userService.rechargeCredit(user, amount);
        return ResponseEntity.ok(credit);
    }

    @GetMapping("/credit")
    @Operation(summary = "Get credit", description = "Get user's own credit")
    public ResponseEntity<?> getCredit(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        BigDecimal credit = user.getCredits();
        return ResponseEntity.ok(credit);
    }

    @GetMapping("/credit/{userId}")
    @Operation(summary = "Get credit by user ID", description = "Get user credit by user ID, only admin can access this API")
    public ResponseEntity<?> getCredit(@AuthenticationPrincipal UserDetails userDetails,
                                       @PathVariable Long userId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        BigDecimal credit = userService.getCredit(user, userId);
        return ResponseEntity.ok(credit);
    }
}
