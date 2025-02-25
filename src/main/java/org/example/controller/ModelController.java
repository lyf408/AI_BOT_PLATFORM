package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.model.dto.modelDTO.CreateModelRequest;
import org.example.model.dto.modelDTO.UpdateModelRequest;
import org.example.model.entity.Model;
import org.example.model.entity.User;
import org.example.repository.UserRepository;
import org.example.service.ModelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/model")
@Tag(name = "Model", description = "APIs for model")
public class ModelController {

    private final UserRepository userRepository;
    private final ModelService modelService;

    @PostMapping()
    @Operation(summary = "Create model", description = "Create new model")
    public ResponseEntity<?> createModel(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody CreateModelRequest createModelRequest) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (!user.getRole().equals(User.Role.ADMIN)) {
            return new ResponseEntity<>("Unauthorized, only admin can create model.", HttpStatus.UNAUTHORIZED);
        }
        Model model = modelService.createModel(createModelRequest);
        return ResponseEntity.ok(model.getModelId());
    }

    @PutMapping()
    @Operation(summary = "Update model", description = "Update model")
    public ResponseEntity<?> updateModel(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody UpdateModelRequest updateModelRequest) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (!user.getRole().equals(User.Role.ADMIN)) {
            return new ResponseEntity<>("Unauthorized, only admin can update model.", HttpStatus.UNAUTHORIZED);
        }
        modelService.updateModel(updateModelRequest);
        return ResponseEntity.ok("Model updated successfully");
    }

    @DeleteMapping("/{modelId}")
    @Operation(summary = "Delete model", description = "Delete model")
    public ResponseEntity<?> deleteModel(@AuthenticationPrincipal UserDetails userDetails,
                                         @PathVariable Long modelId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (!user.getRole().equals(User.Role.ADMIN)) {
            return new ResponseEntity<>("Unauthorized, only admin can delete model.", HttpStatus.UNAUTHORIZED);
        }
        modelService.deleteModel(modelId);
        return ResponseEntity.ok("Model deleted successfully");
    }

    @GetMapping("/all")
    @Operation(summary = "Get all models", description = "Get all models")
    public ResponseEntity<?> getAllModels() {
        return ResponseEntity.ok(modelService.getAllModels());
    }
}
