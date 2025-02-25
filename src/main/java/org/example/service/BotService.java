package org.example.service;

import org.example.model.entity.ChatHistory;
import org.example.model.entity.Session;
import org.example.model.dto.botDTO.CreateBotRequest;
import org.example.model.dto.botDTO.UpdateBotRequest;
import org.example.model.entity.Bot;
import org.example.model.entity.User;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface BotService {
    Bot createBot(User user, CreateBotRequest createBotRequest);
    void updateBot(User user, UpdateBotRequest updateBotRequest);
    void deleteBot(User user, Long botId);
    Bot getBot(User user, Long botId);
    Session createSession(User user, Long botId);
    List<Session> getSessions(User user, Integer limit, Integer offset);
    ChatHistory chat(User user, Long sessionId, String message);
    SseEmitter getChatResponseStream(User user, Long sessionId, Long lastMessageId);
}
