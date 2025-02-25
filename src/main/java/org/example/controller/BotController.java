package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "Bot", description = "APIs for bot")
public class BotController {

    private final BotService botService;
    private final UserRepository userRepository;

    @PostMapping()
    public ResponseEntity<?> createBot(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestBody CreateBotRequest createBotRequest) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Bot bot = botService.createBot(user, createBotRequest);
        return ResponseEntity.ok(bot.getBotId());
    }

    @PutMapping()
    public ResponseEntity<?> updateBot(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestBody UpdateBotRequest updateBotRequest) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        botService.updateBot(user, updateBotRequest);
        return ResponseEntity.ok("Bot updated successfully");
    }

    @DeleteMapping("/{botId}")
    public ResponseEntity<?> deleteBot(@AuthenticationPrincipal UserDetails userDetails,
                          @PathVariable Long botId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        botService.deleteBot(user, botId);
        return ResponseEntity.ok("Bot deleted successfully");
    }

    @GetMapping("/{botId}")
    public ResponseEntity<?> getBot(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long botId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Bot bot = botService.getBot(user, botId);
        return ResponseEntity.ok(bot);
    }

    @PostMapping("/session")
    public ResponseEntity<?> createSession(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam Long botId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        Session session = botService.createSession(user, botId);
        return ResponseEntity.ok(session.getSessionId());
    }

    @GetMapping("/session")
    public ResponseEntity<?> getSessions(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam(defaultValue = "10") Integer limit,
                                 @RequestParam(defaultValue = "0") Integer offset) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        List<Session> sessions = botService.getSessions(user, limit, offset);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestBody ChatRequest chatRequest) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        ChatHistory chatHistory = botService.chat(user, chatRequest.getSessionId(), chatRequest.getMessage());
        return ResponseEntity.ok(chatHistory.getId());
    }

    @GetMapping(value = "/response", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getChatResponseStream(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestParam Long sessionId,
                                            @RequestParam Long lastMessageId) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        return botService.getChatResponseStream(user, sessionId, lastMessageId);
    }
}
