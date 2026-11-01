package com.jobtrack.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtrack.application.port.GeneratedCvRepository;
import com.jobtrack.domain.cv.CvSummary;
import com.jobtrack.domain.cv.GeneratedCv;
import com.jobtrack.infrastructure.persistence.entity.GeneratedCvJpaEntity;
import com.jobtrack.infrastructure.persistence.repository.SpringGeneratedCvRepository;

@Component
public class GeneratedCvRepositoryAdapter implements GeneratedCvRepository {
    private final SpringGeneratedCvRepository repository;
    private final ObjectMapper objectMapper;

    public GeneratedCvRepositoryAdapter(SpringGeneratedCvRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CvSummary> findAllByUserId(UUID userId) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(entity -> new CvSummary(entity.getId(), entity.getTitle(), entity.getApplicationId(),
                        entity.getScore(), entity.getCreatedAt()))
                .toList();
    }

    @Override
    public Optional<GeneratedCv> findByIdAndUserId(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId).map(this::read);
    }

    @Override
    public Optional<GeneratedCv> findLatestByApplicationIdAndUserId(UUID applicationId, UUID userId) {
        return repository.findFirstByApplicationIdAndUserIdOrderByCreatedAtDesc(applicationId, userId)
                .map(this::read);
    }

    @Override
    public GeneratedCv save(GeneratedCv cv) {
        GeneratedCvJpaEntity entity = new GeneratedCvJpaEntity(cv.id(), cv.userId(), cv.applicationId(),
                cv.title(), write(cv), cv.score(), cv.createdAt());
        return read(repository.save(entity));
    }

    @Override
    public void delete(GeneratedCv cv) {
        repository.deleteById(cv.id());
    }

    private String write(GeneratedCv cv) {
        try {
            return objectMapper.writeValueAsString(cv);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Impossible de sérialiser le CV", exception);
        }
    }

    private GeneratedCv read(GeneratedCvJpaEntity entity) {
        try {
            return objectMapper.readValue(entity.getContent(), GeneratedCv.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Impossible de relire le CV " + entity.getId(), exception);
        }
    }
}
