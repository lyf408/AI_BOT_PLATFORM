package org.example.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "bots")
public class Bot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long botId;

    @Column(nullable = false, unique = true, length = 50)
    private String botName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BotType botType = BotType.PUBLIC;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @Column(nullable = false)
    private String avatarUrl = "default_bot_avatar.png";

    @Column(nullable = false)
    private Double temperature = 0.8;

    @Column(nullable = false)
    private Integer maxTokens = 512;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt = "";

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Timestamp createdAt;

    @Column(nullable = false)
    private Timestamp updatedAt;

    public enum BotType {
        OFFICIAL, PUBLIC, PRIVATE
    }
}
