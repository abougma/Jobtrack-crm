package com.jobtrack.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtrack.domain.cv.CvSummary;
import com.jobtrack.domain.cv.GeneratedCv;

public interface GeneratedCvRepository {
    List<CvSummary> findAllByUserId(UUID userId);
    Optional<GeneratedCv> findByIdAndUserId(UUID id, UUID userId);
    Optional<GeneratedCv> findLatestByApplicationIdAndUserId(UUID applicationId, UUID userId);
    GeneratedCv save(GeneratedCv cv);
    void delete(GeneratedCv cv);
}
