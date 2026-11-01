package com.jobtrack.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.ApplicationRepository;
import com.jobtrack.application.port.InteractionRepository;
import com.jobtrack.domain.crm.Interaction;
import com.jobtrack.domain.crm.InteractionType;

@Service
public class InteractionService {
    private final ApplicationRepository applications;
    private final InteractionRepository interactions;

    public InteractionService(ApplicationRepository applications, InteractionRepository interactions) {
        this.applications = applications;
        this.interactions = interactions;
    }

    @Transactional(readOnly = true)
    public List<Interaction> list(UUID userId, UUID applicationId) {
        requireOwnedApplication(userId, applicationId);
        return interactions.findAllByApplicationId(applicationId);
    }

    @Transactional
    public Interaction create(UUID userId, UUID applicationId, InteractionType type, String message, Instant occurredAt) {
        requireOwnedApplication(userId, applicationId);
        return interactions.save(Interaction.create(applicationId, type, message,
                occurredAt == null ? Instant.now() : occurredAt));
    }

    @Transactional
    public void delete(UUID userId, UUID applicationId, UUID interactionId) {
        requireOwnedApplication(userId, applicationId);
        Interaction interaction = interactions.findByIdAndApplicationId(interactionId, applicationId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Interaction introuvable"));
        interactions.delete(interaction);
    }

    private void requireOwnedApplication(UUID userId, UUID applicationId) {
        if (applications.findByIdAndUserId(applicationId, userId).isEmpty()) {
            throw new ApiExceptions.NotFoundException("Candidature introuvable");
        }
    }
}
