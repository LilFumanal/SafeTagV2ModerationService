package com.lil.safetagmoderationservice.controller;

import com.lil.safetagmoderationservice.dto.RejectRequest;
import com.lil.safetagmoderationservice.service.ModerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/moderation")
public class ModerationController {

    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    // Utilisation des Records (Java 14+) pour simplifier les objets de transfert
    public record ModerationRequest(String text) {}
    public record ModerationResponse(String status) {}

    @PostMapping("/check")
    public ResponseEntity<ModerationResponse> checkText(@RequestBody ModerationRequest request) {
        // On appelle la nouvelle méthode
        String status = moderationService.moderateComment(request.text());
        return ResponseEntity.ok(new ModerationResponse(status));
    }

    @PostMapping("/reviews/{id}/reject")
    public ResponseEntity<Void> rejectReview(
            @PathVariable Long id,
            @RequestBody RejectRequest request) {

        // On délègue la logique au service de modération
        moderationService.processRejection(id, request.rejectionReason());

        return ResponseEntity.ok().build();
    }

}