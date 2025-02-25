package org.example.model.dto.botDTO;

import lombok.Getter;
import org.example.model.entity.Bot;

@Getter
public class UpdateBotRequest {
    private String botName;
    private String description;
    private Boolean isPublic;
    private String avatarUrl;
    private Long modelId;
    private Double temperature;
    private Integer maxTokens;
    private String prompt;
}
