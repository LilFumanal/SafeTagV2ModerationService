package com.lil.safetagmoderationservice.service;

import com.lil.safetagmoderationservice.dto.ReviewStatus;
import com.lil.safetagmoderationservice.entity.ModerationLog;
import com.lil.safetagmoderationservice.repository.ModerationLogRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ModerationService {
    private final RestTemplate restTemplate;
    @Value("${REVIEW_SERVICE_URL}")
    private String reviewServiceUrl;
    private final ModerationLogRepository moderationLogRepository;
    private final Set<String> badWords = new HashSet<>();

    public ModerationService(RestTemplate restTemplate, ModerationLogRepository moderationLogRepository) {
        this.restTemplate = restTemplate;
        this.moderationLogRepository = moderationLogRepository;
    }

    @PostConstruct
    public void init() {
        // Charge la liste au démarrage du microservice pour des performances optimales
        try (var inputStream = getClass().getResourceAsStream("/bad_words.txt");
             var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    badWords.add(line.trim().toLowerCase());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement de la liste de modération", e);
        }
    }

    public ReviewStatus moderateComment(String text) {
        if (text == null || text.isBlank()) {
            return ReviewStatus.REJECTED;
        }

        String lowerCaseText = text.toLowerCase();
        boolean containsBadWords = badWords.stream().anyMatch(lowerCaseText::contains);

        // Si on trouve un gros mot, on l'envoie en file d'attente.
        // (Tu peux mettre "REJECTED" si tu préfères un blocage strict).
        return containsBadWords ? ReviewStatus.REJECTED : ReviewStatus.APPROVED;
    }

    public void processRejection(UUID reviewId, String reason) {
        moderationLogRepository.save(new ModerationLog(reviewId, "REJECTED", reason));
        String targetUrl = reviewServiceUrl + "api/v1/internal/reviews/" + reviewId + "/reject?reason=" + reason;
        restTemplate.postForLocation(targetUrl, null);
    }

    public void processRevision(UUID reviewId) {
        moderationLogRepository.save(new ModerationLog(reviewId, "PENDING", "User modified the review"));
        String targetUrl = reviewServiceUrl + "api/v1/internal/reviews/" + reviewId + "/pending";
        restTemplate.postForLocation(targetUrl, null);
    }

    // 2. Quand un modérateur valide l'avis
    public void processApproval(UUID reviewId) {
        moderationLogRepository.save(new ModerationLog(reviewId, "APPROVED", "Approved by moderator"));
        String targetUrl = reviewServiceUrl + "api/v1/internal/reviews/" + reviewId + "/approve";
        restTemplate.postForLocation(targetUrl, null);
    }
}
