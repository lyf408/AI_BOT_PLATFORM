package org.example.model.dto.botDTO;

import lombok.Getter;

@Getter
public class ChatRequest {
    private Long sessionId;
    private String message;
}
