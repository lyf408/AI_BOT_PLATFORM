package org.example.model.dto.botDTO;

import lombok.Getter;
import org.example.model.entity.ChatHistory;

import java.util.List;

@Getter
public class ChatHistoryResponse {
    private final Long id;
    private final Long sessionId;
    private final String content;
    private final String messageType;
    private final String senderRole;
    private final String updatedAt;

    public ChatHistoryResponse(ChatHistory chatHistory) {
        this.id = chatHistory.getId();
        this.sessionId = chatHistory.getSession().getSessionId();
        this.content = chatHistory.getContent();
        this.messageType = chatHistory.getMessageType().name();
        this.senderRole = chatHistory.getSenderRole().name();
        this.updatedAt = chatHistory.getUpdatedAt().toString();
    }

    public static List<ChatHistoryResponse> fromChatHistories(List<ChatHistory> chatHistories) {
        return chatHistories.stream().map(ChatHistoryResponse::new).toList();
    }
}
