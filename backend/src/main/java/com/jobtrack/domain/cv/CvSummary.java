package com.jobtrack.domain.cv;

import java.time.Instant;
import java.util.UUID;

public record CvSummary(UUID id, String title, UUID applicationId, int score, Instant createdAt) {}
