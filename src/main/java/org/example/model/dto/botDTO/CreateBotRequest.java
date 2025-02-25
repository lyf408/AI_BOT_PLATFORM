package org.example.model.dto.botDTO;

import lombok.Getter;
import org.example.model.entity.Bot;

@Getter
public class CreateBotRequest {
    private String botName;
    private String description;
    private Boolean isPublic;
    private String avatarUrl;
    private Long modelId;
    private Double temperature;
    private Integer maxTokens;
    private String prompt;

    public Bot toBot() {
        Bot bot = new Bot();
        bot.setBotName(botName);
        bot.setDescription(description);
        bot.setBotType(isPublic ? Bot.BotType.PUBLIC : Bot.BotType.PRIVATE);
        bot.setAvatarUrl(avatarUrl);
        bot.setTemperature(temperature);
        bot.setMaxTokens(maxTokens);
        bot.setPrompt(prompt);
        return bot;
    }
}
