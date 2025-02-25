package org.example.model.dto.botDTO;

import lombok.Getter;
import org.example.model.entity.Bot;
import org.example.model.entity.Model;

@Getter
public class GetBotResponse {
    private final Long botId;
    private final String botName;
    private final String description;
    private final Bot.BotType botType;
    private final String avatarUrl;
    private final String creatorName;
    private final String modelName;
    private final Double temperature;
    private final Integer maxTokens;
    private final String prompt;

    public GetBotResponse(Bot bot) {
        this.botId = bot.getBotId();
        this.botName = bot.getBotName();
        this.description = bot.getDescription();
        this.botType = bot.getBotType();
        this.avatarUrl = bot.getAvatarUrl();
        this.creatorName = bot.getCreator().getUsername();
        this.modelName = bot.getModel().getModelName();
        this.temperature = bot.getTemperature();
        this.maxTokens = bot.getMaxTokens();
        this.prompt = bot.getPrompt();
    }
}
