package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.bouncycastle.math.raw.Mod;
import org.example.exception.ApiException;
import org.example.model.dto.modelDTO.CreateModelRequest;
import org.example.model.dto.modelDTO.GetModelResponse;
import org.example.model.dto.modelDTO.UpdateModelRequest;
import org.example.model.entity.Bot;
import org.example.model.entity.Model;
import org.example.model.entity.Session;
import org.example.repository.BotRepository;
import org.example.repository.ChatHistoryRepository;
import org.example.repository.ModelRepository;
import org.example.repository.SessionRepository;
import org.example.service.ModelService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelServiceImpl implements ModelService {

    private final ModelRepository modelRepository;
    private final BotRepository botRepository;
    private final SessionRepository sessionRepository;
    private final ChatHistoryRepository chatHistoryRepository;

    @Override
    public Model createModel(CreateModelRequest createModelRequest) {
        Model model = createModelRequest.toModel();
        if (modelRepository.findByModelName(model.getModelName()) != null) {
            throw new ApiException("Model name already exists", HttpStatus.BAD_REQUEST);
        }
        model.setCreatedAt(Timestamp.from(java.time.Instant.now()));
        model.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        return modelRepository.save(model);
    }

    @Override
    public void updateModel(UpdateModelRequest updateModelRequest) {
        Model existingModel = modelRepository.findByModelName(updateModelRequest.getModelName());
        if (existingModel == null) {
            throw new ApiException("Model not found", HttpStatus.NOT_FOUND);
        }
        if (updateModelRequest.getCostRate() != null) {
            existingModel.setCostRate(updateModelRequest.getCostRate());
        }
        if (updateModelRequest.getDescription() != null) {
            existingModel.setDescription(updateModelRequest.getDescription());
        }
        if (updateModelRequest.getApiUrl() != null) {
            existingModel.setApiUrl(updateModelRequest.getApiUrl());
        }
        if (updateModelRequest.getApiKey() != null) {
            existingModel.setApiKey(updateModelRequest.getApiKey());
        }
        existingModel.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        modelRepository.save(existingModel);
    }

    @Override
    public void deleteModel(Long modelId) {
        Model existingModel = modelRepository.findById(modelId).orElse(null);
        if (existingModel == null) {
            throw new ApiException("Model not found", HttpStatus.NOT_FOUND);
        }
        List<Bot> botList = botRepository.findAllByModel(existingModel);
        List<Session> sessionList = new ArrayList<>();
        for (Bot bot : botList) {
            sessionList.addAll(sessionRepository.findAllByBot(bot));
        }
        for (Session session : sessionList) {
            chatHistoryRepository.deleteAllBySession(session);
        }
        sessionRepository.deleteAll(sessionList);
        botRepository.deleteAll(botList);
        modelRepository.delete(existingModel);
    }

    @Override
    public List<GetModelResponse> getAllModels() {
        List<Model> models = modelRepository.findAll();
        List<GetModelResponse> getModelResponses = new ArrayList<>();
        for (Model model : models) {
            getModelResponses.add(new GetModelResponse(model));
        }
        return getModelResponses;
    }
}
