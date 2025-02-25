package org.example.service;

import org.example.model.dto.LLMDTO.LLMResponse;

public interface LLMService {
    LLMResponse chat(String message);
}
