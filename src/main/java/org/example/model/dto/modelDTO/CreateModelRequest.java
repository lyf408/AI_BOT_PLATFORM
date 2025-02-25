package org.example.model.dto.modelDTO;

import lombok.Getter;
import org.example.model.entity.Model;

@Getter
public class CreateModelRequest {
    private String modelName;
    private String description;
    private String apiUrl;
    private String apiKey;
    private Integer costRate;

    public Model toModel() {
        Model model = new Model();
        model.setModelName(modelName);
        model.setDescription(description);
        model.setApiUrl(apiUrl);
        model.setApiKey(apiKey);
        model.setCostRate(costRate);
        return model;
    }
}
