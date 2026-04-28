package com.lil.safetagmoderationservice.service;

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

@Service
public class ModerationService {
    private final RestTemplate restTemplate;
    @Value("${app.services.review.url}")
    private String reviewServiceUrl;
    private final Set<String> badWords = new HashSet<>();

    public ModerationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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

    public String moderateComment(String text) {
        if (text == null || text.isBlank()) {
            return "REJECTED";
        }

        String lowerCaseText = text.toLowerCase();
        boolean containsBadWords = badWords.stream().anyMatch(lowerCaseText::contains);

        // Si on trouve un gros mot, on l'envoie en file d'attente.
        // (Tu peux mettre "REJECTED" si tu préfères un blocage strict).
        return containsBadWords ? "REJECTED" : "APPROVED";
    }

    public void processRejection(Long reviewId, String reason) {
        Map<String, String> payload = Map.of("rejectionReason", reason);
        String targetUrl = reviewServiceUrl + "/" + reviewId + "/reject";
        restTemplate.postForLocation(targetUrl, payload);
    }
}
