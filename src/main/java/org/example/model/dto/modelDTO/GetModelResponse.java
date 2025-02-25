package org.example.model.dto.modelDTO;

import lombok.Getter;
import org.example.model.entity.Model;

@Getter
public class GetModelResponse {
    private final Long modelId;
    private final String modelName;
    private final String description;
    private final Integer costRate;

    public GetModelResponse(Model model) {
        this.modelId = model.getModelId();
        this.modelName = model.getModelName();
        this.description = model.getDescription();
        this.costRate = model.getCostRate();
    }
}
