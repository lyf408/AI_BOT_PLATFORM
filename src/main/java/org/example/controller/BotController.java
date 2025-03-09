package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jdk.jfr.Description;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.model.dto.botDTO.ChatHistoryResponse;
import org.example.model.dto.botDTO.ChatRequest;
import org.example.model.dto.botDTO.CreateBotRequest;
import org.example.model.dto.botDTO.UpdateBotRequest;
import org.example.model.dto.modelDTO.CreateModelRequest;
import org.example.model.entity.Bot;
import org.example.model.entity.ChatHistory;
import org.example.model.entity.Session;
import org.example.model.entity.User;
import org.example.repository.UserRepository;
import org.example.service.BotService;
import org.example.service.UserService;
import org.example.service.impl.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bot")
@Tag(name = "Bot", description = "APIs for bot and chat")
public class BotController {

    private final BotService botService;
    private final UserRepository userRepository;

    @PostMapping()
    @Operation(summary = "Create bot", description = "Create new bot")
    public ResponseEntity<?> createBot(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestBody CreateBotRequest createBotRequest) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Bot bot = botService.createBot(user, createBotRequest);
        return ResponseEntity.ok(bot.getBotId());
    }

    @PostMapping("/official")
    @Operation(summary = "Create official bot", description = "Create new official bot")
    public ResponseEntity<?> createOfficialBot(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestBody CreateBotRequest createBotRequest) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Bot bot = botService.createOfficialBot(user, createBotRequest);
        return ResponseEntity.ok(bot.getBotId());
    }

    @PutMapping()
    @Operation(summary = "Update bot", description = "Update bot")
    public ResponseEntity<?> updateBot(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestBody UpdateBotRequest updateBotRequest) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        botService.updateBot(user, updateBotRequest);
        return ResponseEntity.ok("Bot updated successfully");
    }

    @DeleteMapping("/{botId}")
    @Operation(summary = "Delete bot", description = "Delete bot. After deletion, the original record will also be retained")
    public ResponseEntity<?> deleteBot(@AuthenticationPrincipal UserDetails userDetails,
                          @PathVariable Long botId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        botService.deleteBot(user, botId);
        return ResponseEntity.ok("Bot deleted successfully");
    }

    @GetMapping("/{botId}")
    @Operation(summary = "Get bot", description = "Get bot by botId")
    public ResponseEntity<?> getBot(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long botId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Bot bot = botService.getBot(user, botId);
        return ResponseEntity.ok(bot);
    }

    @PostMapping("/session")
    @Operation(summary = "Create session", description = "Create new session")
    public ResponseEntity<?> createSession(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam Long botId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Session session = botService.createSession(user, botId);
        return ResponseEntity.ok(session.getSessionId());
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get sessions", description = "Get sessions created by current user, with pagination")
    public ResponseEntity<?> getSessions(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam(defaultValue = "10") Integer limit,
                                 @RequestParam(defaultValue = "0") Integer offset) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        List<Session> sessions = botService.getSessions(user, limit, offset);
        return ResponseEntity.ok(sessions);
    }

    @PutMapping("/sessions/maxTokens")
    @Operation(summary = "Update session max tokens", description = "Update session max tokens")
    public ResponseEntity<?> updateSessionMaxTokens(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam Long sessionId,
                                 @RequestParam Integer maxTokens) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        botService.updateSessionMaxTokens(user, sessionId, maxTokens);
        return ResponseEntity.ok("Session max tokens updated successfully");
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get session chat history", description = "Get session chat history by sessionId")
    public ResponseEntity<?> getSessionChatHistory(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long sessionId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        List<ChatHistoryResponse> chatHistories = botService.getSessionChatHistory(user, sessionId);
        return ResponseEntity.ok(chatHistories);
    }

    @PostMapping("/chat")
    @Operation(summary = "Chat", description = "Chat with bot")
    public ResponseEntity<?> chat(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestBody ChatRequest chatRequest) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        ChatHistory chatHistory = botService.chat(user, chatRequest.getSessionId(), chatRequest.getMessage());
        return ResponseEntity.ok(chatHistory.getId());
    }

    @GetMapping(value = "/response", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Get chat response stream", description = "Get chat response stream by sessionId and lastMessageId")
    public SseEmitter getChatResponseStream(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestParam Long sessionId,
                                            @RequestParam Long lastMessageId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        return botService.getChatResponseStream(user, sessionId, lastMessageId);
    }
}
