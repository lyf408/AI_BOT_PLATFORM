package org.example.model.dto.userDTO;

import lombok.Getter;
import org.example.model.entity.User;

@Getter
public class UpdateProfileRequest {
    private String avatarUrl;
    private String bio;
}
