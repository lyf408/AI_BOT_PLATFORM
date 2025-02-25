package org.example.model.dto.modelDTO;

import lombok.Getter;

@Getter
public class UpdateModelRequest {
    private String modelName;
    private String description;
    private String apiUrl;
    private String apiKey;
    private Integer costRate;
}
