package com.jobtrack.application.port;

import java.util.UUID;

import com.jobtrack.domain.cv.CvAnalysis;
import com.jobtrack.domain.cv.GeneratedCv;
import com.jobtrack.domain.profile.ProfileSnapshot;

public interface CvAssistant {
    CvAnalysis analyze(ProfileSnapshot profile, String offerText);

    GeneratedCv generate(UUID userId, UUID applicationId, String title, String headline,
                         ProfileSnapshot profile, String offerText, CvAnalysis analysis);
}
