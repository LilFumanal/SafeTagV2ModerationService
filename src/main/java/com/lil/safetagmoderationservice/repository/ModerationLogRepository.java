package com.lil.safetagmoderationservice.repository;

import com.lil.safetagmoderationservice.entity.ModerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ModerationLogRepository extends JpaRepository<ModerationLog, UUID> {

    List<ModerationLog> findByReviewIdOrderByCreatedAtDesc(UUID reviewId);
}
