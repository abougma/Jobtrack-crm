package com.jobtrack.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.jobtrack.application.port.InterviewAssistant;
import com.jobtrack.domain.profile.*;

class MockInterviewAssistantTest {

    private final MockInterviewAssistant assistant = new MockInterviewAssistant();
    private final UUID userId = UUID.randomUUID();

    private ProfileSnapshot profile() {
        return new ProfileSnapshot(
                List.of(Experience.create(userId, "Acme", "Développeur Java",
                        LocalDate.of(2023, 1, 1), null, "API Java Spring")),
                List.of(),
                List.of(Project.create(userId, "JobTrack", "CRM", "Java, React")),
                List.of(Skill.create(userId, "Java", SkillLevel.ADVANCED, SkillContext.PROFESSIONAL)),
                List.of());
    }

    @Test
    void preparesTechnicalQuestionsFromTheOffer() {
        InterviewAssistant.InterviewPreparation preparation =
                assistant.prepare(profile(), "Nous recherchons un développeur Java et Docker.");

        assertThat(preparation.technicalQuestions())
                .extracting(InterviewAssistant.Question::question)
                .anyMatch(question -> question.contains("Java"))
                .anyMatch(question -> question.contains("Docker"));
        assertThat(preparation.behavioralQuestions()).hasSize(3);
        assertThat(preparation.starPrompts()).hasSize(4);
        assertThat(preparation.recruiterQuestions()).isNotEmpty();
    }

    @Test
    void flagsMissingEvidenceWhenProfileIsEmpty() {
        ProfileSnapshot empty = new ProfileSnapshot(List.of(), List.of(), List.of(), List.of(), List.of());
        InterviewAssistant.InterviewPreparation preparation = assistant.prepare(empty, "Développeur Java");

        assertThat(preparation.missingEvidence())
                .contains("Un exemple professionnel à raconter", "Un projet concret à présenter");
    }
}
