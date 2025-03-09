package org.example.model.dto.userDTO;

import jakarta.persistence.Column;
import lombok.Getter;
import org.example.model.entity.User;

import java.math.BigDecimal;

@Getter
public class UserProfileResponse {
    private final String username;
    private final String email;
    private final String role;
    private final String avatarUrl;
    private final String bio;

    public UserProfileResponse(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.avatarUrl = user.getAvatarUrl();
        this.bio = user.getBio();
    }
}
