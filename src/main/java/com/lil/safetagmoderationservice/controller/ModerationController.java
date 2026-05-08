package com.lil.safetagmoderationservice.controller;

import com.lil.safetagmoderationservice.dto.RejectRequest;
import com.lil.safetagmoderationservice.dto.ReviewStatus;
import com.lil.safetagmoderationservice.service.ModerationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/moderation")
public class ModerationController {

    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    // Utilisation des Records (Java 14+) pour simplifier les objets de transfert
    public record ModerationRequest(String text) {}
    public record ModerationResponse(ReviewStatus status) {}

    @PostMapping("/check")
    public ResponseEntity<ModerationResponse> checkText(@RequestBody ModerationRequest request) {
        // On appelle la nouvelle méthode
        ReviewStatus status = moderationService.moderateComment(request.text());
        return ResponseEntity.ok(new ModerationResponse(status));
    }

    @PostMapping("/{reviewId}/reject")
    public ResponseEntity<Void> rejectReview(
            @PathVariable UUID id,
            @RequestBody RejectRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (!"MODERATOR".equals(userRole) && !"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        moderationService.processRejection(id, request.rejectionReason());

        return ResponseEntity.ok().build();
    }
    @PostMapping("/{reviewId}/pending")
    public ResponseEntity<Void> markAsPending(@PathVariable UUID reviewId) {
        moderationService.processRevision(reviewId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/approve")
    public ResponseEntity<Void> approveReview(
            @PathVariable UUID reviewId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (!"MODERATOR".equals(userRole) && !"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        moderationService.processApproval(reviewId);
        return ResponseEntity.ok().build();
    }
}