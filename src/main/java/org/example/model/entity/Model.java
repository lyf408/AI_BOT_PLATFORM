package org.example.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "models")
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ModelId;

    @Column(nullable = false, unique = true, length = 50)
    private String modelName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description = "";

    @Column(nullable = false)
    private String apiUrl = "";

    @Column(nullable = false)
    private String apiKey = "";

    @Column(nullable = false)
    private Integer costRate = 1;

    @Column(nullable = false)
    private Timestamp createdAt;

    @Column(nullable = false)
    private Timestamp updatedAt;
}
