package com.jobtrack.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.ApplicationRepository;
import com.jobtrack.application.port.InterviewAssistant;
import com.jobtrack.application.port.ProfileRepository;

@Service
public class InterviewService {
    private final ApplicationRepository applications;
    private final ProfileRepository profiles;
    private final InterviewAssistant assistant;

    public InterviewService(ApplicationRepository applications, ProfileRepository profiles,
                            InterviewAssistant assistant) {
        this.applications = applications;
        this.profiles = profiles;
        this.assistant = assistant;
    }

    @Transactional(readOnly = true)
    public InterviewAssistant.InterviewPreparation prepare(UUID userId, String offerText, UUID applicationId) {
        if (offerText == null || offerText.isBlank()) {
            throw new ApiExceptions.BadRequestException("Le texte de l'offre est obligatoire");
        }
        if (applicationId != null) {
            applications.findByIdAndUserId(applicationId, userId)
                    .orElseThrow(() -> new ApiExceptions.NotFoundException("Candidature introuvable"));
        }
        return assistant.prepare(profiles.load(userId), offerText);
    }
}
