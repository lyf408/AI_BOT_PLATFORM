package org.example.service;

import org.example.model.dto.modelDTO.CreateModelRequest;
import org.example.model.dto.modelDTO.GetModelResponse;
import org.example.model.dto.modelDTO.UpdateModelRequest;
import org.example.model.entity.Model;

import java.util.List;

public interface ModelService {
    Model createModel(CreateModelRequest createModelRequest);
    void updateModel(UpdateModelRequest updateModelRequest);
    void deleteModel(Long modelId);
    List<GetModelResponse> getAllModels();
}
