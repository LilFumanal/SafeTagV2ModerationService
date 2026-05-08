package com.lil.safetagmoderationservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class ModerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID reviewId;

    @Column(nullable = false)
    private String status;

    @Column(length = 1000)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructeur vide requis par JPA
    public ModerationLog() {}

    public ModerationLog(UUID reviewId, String status, String reason) {
        this.reviewId = reviewId;
        this.status = status;
        this.reason = reason;
    }

    // Ajoute les Getters et Setters classiques ici
}
