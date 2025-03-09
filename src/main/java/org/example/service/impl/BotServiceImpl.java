package org.example.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import okio.BufferedSource;
import org.example.exception.ApiException;
import org.example.model.dto.botDTO.ChatHistoryResponse;
import org.example.model.dto.botDTO.CreateBotRequest;
import org.example.model.dto.botDTO.UpdateBotRequest;
import org.example.model.entity.*;
import org.example.repository.*;
import org.example.service.BotService;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {

    private final BotRepository botRepository;
    private final ModelRepository modelRepository;
    private final SessionRepository sessionRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final CreditHistoryRepository creditHistoryRepository;

    @Value("${okhttp.connect-timeout-seconds}")
    private int connectTimeout;

    @Value("${okhttp.read-timeout-seconds}")
    private int readTimeout;

    @Value("${okhttp.write-timeout-seconds}")
    private int writeTimeout;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(connectTimeout, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(readTimeout, java.util.concurrent.TimeUnit.SECONDS)    // 读取超时
            .writeTimeout(writeTimeout, java.util.concurrent.TimeUnit.SECONDS)   // 写入超时
            .build();
    private final MediaType mediaType = MediaType.parse("application/json");


    @Override
    public Bot createBot(User user, CreateBotRequest createBotRequest) {
        Bot bot = createBotRequest.toBot();
        if (botRepository.findByBotName(bot.getBotName()) != null) {
            throw new ApiException("Bot name already exists", HttpStatus.BAD_REQUEST);
        }
        Model model = modelRepository.findById(createBotRequest.getModelId()).orElse(null);
        bot.setModel(model);
        bot.setCreator(user);
        bot.setCreatedAt(Timestamp.from(java.time.Instant.now()));
        bot.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        return botRepository.save(bot);
    }

    @Override
    public Bot createOfficialBot(User user, CreateBotRequest createBotRequest) {
        if (user.getRole().equals(User.Role.USER)) {
            throw new ApiException("You are not allowed to create an official bot", HttpStatus.FORBIDDEN);
        }
        Bot bot = createBotRequest.toBot();
        if (botRepository.findByBotName(bot.getBotName()) != null) {
            throw new ApiException("Bot name already exists", HttpStatus.BAD_REQUEST);
        }
        Model model = modelRepository.findById(createBotRequest.getModelId()).orElse(null);
        bot.setModel(model);
        bot.setCreator(user);
        bot.setBotType(Bot.BotType.OFFICIAL);
        bot.setCreatedAt(Timestamp.from(java.time.Instant.now()));
        bot.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        return botRepository.save(bot);
    }

    @Override
    public void updateBot(User user, UpdateBotRequest updateBotRequest) {
        Bot existingBot = botRepository.findByBotName(updateBotRequest.getBotName());
        if (existingBot == null) {
            throw new ApiException("Bot not found", HttpStatus.NOT_FOUND);
        }
        if (!existingBot.getActive()) {
            throw new ApiException("Bot already deleted", HttpStatus.BAD_REQUEST);
        }
        if (user.getRole().equals(User.Role.USER) && !existingBot.getCreator().equals(user)) {
            throw new ApiException("You are not allowed to update this bot", HttpStatus.FORBIDDEN);
        }
        if (updateBotRequest.getDescription() != null) {
            existingBot.setDescription(updateBotRequest.getDescription());
        }
        if (updateBotRequest.getAvatarUrl() != null) {
            existingBot.setAvatarUrl(updateBotRequest.getAvatarUrl());
        }
        if (updateBotRequest.getTemperature() != null) {
            existingBot.setTemperature(updateBotRequest.getTemperature());
        }
        if (updateBotRequest.getMaxTokens() != null) {
            existingBot.setMaxTokens(updateBotRequest.getMaxTokens());
        }
        if (updateBotRequest.getPrompt() != null) {
            existingBot.setPrompt(updateBotRequest.getPrompt());
        }
        existingBot.setUpdatedAt(java.sql.Timestamp.from(java.time.Instant.now()));
        botRepository.save(existingBot);
    }

    @Override
    public void deleteBot(User user, Long botId) {
        Bot bot = botRepository.findById(botId).orElse(null);
        if (bot == null) {
            throw new ApiException("Bot not found", HttpStatus.NOT_FOUND);
        }
        if (!bot.getActive()) {
            throw new ApiException("Bot already deleted", HttpStatus.BAD_REQUEST);
        }
        if (user.getRole().equals(User.Role.USER) && !bot.getCreator().equals(user)) {
            throw new ApiException("You are not allowed to delete this bot", HttpStatus.FORBIDDEN);
        }
        bot.setActive(false);
        bot.setDescription("Original Bot Name: " + bot.getBotName() +
                "\nOriginal Description: " + bot.getDescription());
        String deletedName = null;
         do {
            UUID uuid = UUID.randomUUID();
            deletedName = "deleted-" + uuid;
        } while (botRepository.findByBotName(deletedName) != null);

        bot.setBotName(deletedName);
        botRepository.save(bot);
    }

    @Override
    public Bot getBot(User user, Long botId) {
        Bot bot = botRepository.findById(botId).orElse(null);
        if (bot == null) {
            throw new ApiException("Bot not found", HttpStatus.NOT_FOUND);
        }
        if (user.getRole().equals(User.Role.USER)) {
            if (!bot.getCreator().equals(user) && bot.getBotType().equals(Bot.BotType.PRIVATE)) {
                throw new ApiException("You are not allowed to view this bot", HttpStatus.FORBIDDEN);
            }
            if (!bot.getActive()) {
                throw new ApiException("Bot already deleted", HttpStatus.BAD_REQUEST);
            }
        }
        return bot;
    }

    @Override
    public Session createSession(User user, Long botId) {
        Bot bot = botRepository.findByBotId(botId);
        if (user == null) {
            throw new ApiException("User not found", HttpStatus.NOT_FOUND);
        }
        if (bot == null) {
            throw new ApiException("Bot not found", HttpStatus.NOT_FOUND);
        }
        if (!bot.getActive()) {
            throw new ApiException("Bot already deleted", HttpStatus.BAD_REQUEST);
        }
        if (user.getRole().equals(User.Role.USER) &&
                !bot.getCreator().equals(user) &&
                bot.getBotType().equals(Bot.BotType.PRIVATE)) {
            throw new ApiException("You are not allowed to create a session for this bot", HttpStatus.FORBIDDEN);
        }
        Session session = new Session();
        session.setBot(bot);
        session.setUser(user);
        session.setMaxTokens(bot.getMaxTokens());
        session.setCreatedAt(Timestamp.from(java.time.Instant.now()));
        session.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        return sessionRepository.save(session);
    }

    @Override
    public List<Session> getSessions(User user, Integer limit, Integer offset) {
        if (user == null) {
            throw new ApiException("User not found", HttpStatus.NOT_FOUND);
        }
        List<Session> sessions = sessionRepository.findAllByUser(user.getUserId(), limit, offset);
        if (sessions == null) {
            throw new ApiException("No sessions found", HttpStatus.NOT_FOUND);
        }
        return sessions;
    }

    @Override
    public void updateSessionMaxTokens(User user, Long sessionId, Integer maxTokens) {
        Session session = sessionRepository.findBySessionId(sessionId);
        if (session == null) {
            throw new ApiException("Session not found", HttpStatus.NOT_FOUND);
        }
        if (user == null) {
            throw new ApiException("User not found", HttpStatus.NOT_FOUND);
        }
        if (!session.getUser().equals(user)) {
            throw new ApiException("You are not allowed to update this session", HttpStatus.FORBIDDEN);
        }
        if (!session.getBot().getActive()) {
            throw new ApiException("Bot already deleted", HttpStatus.BAD_REQUEST);
        }
        session.setMaxTokens(maxTokens);
        session.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        sessionRepository.save(session);
    }

    @Override
    public List<ChatHistoryResponse> getSessionChatHistory(User user, Long sessionId) {
        Session session = getSession(user, sessionId);
        List<ChatHistory> chatHistories = chatHistoryRepository.findAllBySession(session);
        List<ChatHistoryResponse> chatHistoryResponses = ChatHistoryResponse.fromChatHistories(chatHistories);
        return chatHistoryResponses;
    }

    @Override
    public ChatHistory chat(User user, Long sessionId, String message) {
        Session session = getSession(user, sessionId);
        if (message == null || message.isEmpty()) {
            throw new ApiException("Message cannot be empty", HttpStatus.BAD_REQUEST);
        }
        if (!session.getBot().getActive()) {
            throw new ApiException("Bot already deleted", HttpStatus.BAD_REQUEST);
        }
        BigDecimal cost = BigDecimal.valueOf(session.getBot().getModel().getCostRate())
                .multiply(BigDecimal.valueOf(session.getMaxTokens()))
                .divide(BigDecimal.valueOf(100), RoundingMode.CEILING);
        if (user.getCredits().compareTo(cost) < 0) {
            throw new ApiException("Insufficient credits", HttpStatus.BAD_REQUEST);
        }
        user.setCredits(user.getCredits().subtract(cost));
        user.setUpdatedAt(Timestamp.from(java.time.Instant.now()));

        CreditHistory creditHistory = new CreditHistory();
        creditHistory.setUser(user);
        creditHistory.setAmount(cost.negate());
        creditHistory.setDescription("Chat with bot " + session.getBot().getBotName());
        creditHistory.setCreatedAt(Timestamp.from(java.time.Instant.now()));
        creditHistory.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        creditHistoryRepository.save(creditHistory);

        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setSession(session);
        chatHistory.setSenderRole(ChatHistory.SenderRole.USER);
        chatHistory.setContent(message);
        chatHistory.setCreatedAt(Timestamp.from(java.time.Instant.now()));
        chatHistory.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        return chatHistoryRepository.save(chatHistory);
    }

    @Override
    public SseEmitter getChatResponseStream(User user, Long sessionId, Long lastMessageId) {
        Session session = getSession(user, sessionId);
        session.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        sessionRepository.save(session);
        List<ChatHistory> chatHistoryList = chatHistoryRepository.findAllBySessionBeforeMessage(sessionId, lastMessageId);
        Collections.reverse(chatHistoryList);
        JsonObject requestBody = new JsonObject();

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", session.getBot().getPrompt());
        messages.add(systemMessage);

        for (ChatHistory chatHistory : chatHistoryList) {
            JsonObject chatMessage = new JsonObject();
            chatMessage.addProperty("role", chatHistory.getSenderRole().toString().toLowerCase());
            chatMessage.addProperty("content", chatHistory.getContent());
            messages.add(chatMessage);
        }

        requestBody.add("messages", messages);
        requestBody.addProperty("stream", true);
        requestBody.addProperty("model", session.getBot().getModel().getModelName());
        requestBody.addProperty("temperature", session.getBot().getTemperature());
        requestBody.addProperty("max_tokens", session.getMaxTokens());

        RequestBody body = RequestBody.create(requestBody.toString(), mediaType);

        Request request = new Request.Builder()
                .url(session.getBot().getModel().getApiUrl())
                .post(body)
                .addHeader("Authorization", "Bearer " + session.getBot().getModel().getApiKey())
                .addHeader("Content-Type", "application/json")
                .build();

        StringBuilder responseBuilder = new StringBuilder();

        SseEmitter emitter = new SseEmitter(60_000L); // 设置超时时间（60秒）

        // 异步推送数据
        executor.execute(() -> {
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    BufferedSource source = response.body().source();
                    while (!source.exhausted()) {
                        String line = source.readUtf8Line();
                        if (line != null && !line.isEmpty()) {
                            if (line.startsWith("data: ")) {
                                line = line.substring(6);
                            }
                            if (line.equals("[DONE]")) {
                                emitter.send(SseEmitter.event().data("[DONE]"));
                                emitter.complete();
                                break;
                            }

                            JSONObject jsonResponse = new JSONObject(line);
                            if (jsonResponse.has("choices")) {
                                JSONObject choice = jsonResponse.getJSONArray("choices").getJSONObject(0);
                                // 如果 content 不为空，则拼接到完整回复
                                if (choice.has("delta") && choice.getJSONObject("delta").has("content")) {
                                    String text = choice.getJSONObject("delta").getString("content");
                                    String formattedText = text.replace(" ", "&nbsp;").replace("\n", "<br>");
                                    emitter.send(SseEmitter.event().data(formattedText));
                                    responseBuilder.append(text);
                                }
                            }

                        }
                    }
                    ChatHistory chatHistory = new ChatHistory();
                    chatHistory.setSession(session);
                    chatHistory.setSenderRole(ChatHistory.SenderRole.ASSISTANT);
                    chatHistory.setContent(responseBuilder.toString());
                    chatHistory.setCreatedAt(Timestamp.from(java.time.Instant.now()));
                    chatHistory.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
                    chatHistoryRepository.save(chatHistory);
                } else {
                    emitter.completeWithError(new IOException("Request failed: " + response.code()));
                }
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @Override
    public List<String> predictNextMessage(Long messageId) {
        ChatHistory chatHistory = chatHistoryRepository.findById(messageId).orElse(null);
        if (chatHistory == null) {
            throw new ApiException("Chat history not found", HttpStatus.NOT_FOUND);
        }
        JsonObject requestBody = new JsonObject();
        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", """
                You are an intelligent chat assistant that can predict the user's next message based on the messages they send.
                ###Task:
                1. Analyze user messages.
                2. Provide three predictions for the user's next message, starting with "-pred=- ".
                3. Make message predictions based on the user's language. If the user uses Chinese, use Chinese to make predictions.
                ###Input:
                - User: Hello, I would like to know more about your products.
                ###Output format:
                -pred=- How are your products priced?
                -pred=- Do you have any other products?
                -pred=- What are the advantages of your product compared to competitors?
                """);
        messages.add(systemMessage);
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", chatHistory.getContent());
        messages.add(userMessage);
        requestBody.add("messages", messages);
        requestBody.addProperty("stream", false);
        requestBody.addProperty("model", "gpt-3.5-turbo");
        requestBody.addProperty("temperature", 0.8);
        requestBody.addProperty("max_tokens", 256);
        RequestBody body = RequestBody.create(requestBody.toString(), mediaType);
        Request request = new Request.Builder()
                .url("https://xiaoai.plus/v1/chat/completions")
                .post(body)
                .addHeader("Authorization", "Bearer sk-WXMk631iKpu1J2Ql6GnCVLRt2YwOeLXALEqx6X1BmPuIMsRg")
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response response = okHttpClient.newCall(request).execute()){
            if (response.isSuccessful() && response.body() != null) {
                BufferedSource source = response.body().source();
                String line = source.readUtf8Line();
                if (line != null && !line.isEmpty()) {
                    JSONObject jsonResponse = new JSONObject(line);
                    if (jsonResponse.has("choices")) {
                        JSONObject choice = jsonResponse.getJSONArray("choices").getJSONObject(0);
                        if (choice.has("message") && choice.getJSONObject("message").has("content")) {
                            String prediction = choice.getJSONObject("message").getString("content");
                            String[] predictions = prediction.replace("\n", "").split("-pred=- ");
                            List<String> predictionList = new ArrayList<>();
                            for (String s : predictions) {
                                s = s.trim();
                                if (!s.isEmpty()) {
                                    predictionList.add(s);
                                }
                            }
                            return predictionList;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ApiException("Request failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return List.of();
    }

    @NotNull
    private Session getSession(User user, Long sessionId) {
        Session session = sessionRepository.findBySessionId(sessionId);
        if (session == null) {
            throw new ApiException("Session not found", HttpStatus.NOT_FOUND);
        }
        if (user == null) {
            throw new ApiException("User not found", HttpStatus.NOT_FOUND);
        }
        if (!session.getUser().equals(user)) {
            throw new ApiException("You are not allowed to view this session", HttpStatus.FORBIDDEN);
        }
        if (!session.getBot().getActive()) {
            throw new ApiException("Bot already deleted", HttpStatus.BAD_REQUEST);
        }
        return session;
    }
}
