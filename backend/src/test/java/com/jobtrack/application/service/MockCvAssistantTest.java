package com.jobtrack.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.jobtrack.domain.cv.CvAnalysis;
import com.jobtrack.domain.cv.GeneratedCv;
import com.jobtrack.domain.profile.*;

class MockCvAssistantTest {

    private final MockCvAssistant assistant = new MockCvAssistant();
    private final UUID userId = UUID.randomUUID();

    private ProfileSnapshot profile() {
        return new ProfileSnapshot(
                List.of(Experience.create(userId, "Acme", "Développeur Java",
                        LocalDate.of(2023, 1, 1), null, "Développement d'API Java Spring.")),
                List.of(Education.create(userId, "Université", "Master informatique",
                        LocalDate.of(2021, 9, 1), LocalDate.of(2023, 6, 30), null)),
                List.of(Project.create(userId, "JobTrack", "CRM de recherche d'emploi", "Java, React")),
                List.of(Skill.create(userId, "Java", SkillLevel.ADVANCED, SkillContext.PROFESSIONAL)),
                List.of(Language.create(userId, "Français", LanguageLevel.NATIVE)));
    }

    @Test
    void analyzesCoveredAndMissingSkillsWithoutInventingAny() {
        CvAnalysis analysis = assistant.analyze(profile(),
                "Nous recherchons un développeur Java avec Docker et Kubernetes.");

        assertThat(analysis.coveredSkills()).contains("Java");
        assertThat(analysis.missingSkills()).contains("Docker", "Kubernetes");
        assertThat(analysis.coveredSkills()).doesNotContain("Docker", "Kubernetes");
        assertThat(analysis.score()).isBetween(1, 100);
        assertThat(analysis.missingInformation()).isEmpty();
    }

    @Test
    void reportsMissingProfileSections() {
        ProfileSnapshot empty = new ProfileSnapshot(List.of(), List.of(), List.of(), List.of(), List.of());
        CvAnalysis analysis = assistant.analyze(empty, "Développeur Java Spring Boot");

        assertThat(analysis.coveredSkills()).isEmpty();
        assertThat(analysis.missingInformation())
                .contains("Expérience professionnelle", "Formation", "Projet concret", "Langues", "Compétences");
        assertThat(analysis.alerts()).isNotEmpty();
    }

    @Test
    void generatesCvOnlyFromRealProfileData() {
        ProfileSnapshot profile = profile();
        String offer = "Développeur Java, Spring Boot et Docker.";
        CvAnalysis analysis = assistant.analyze(profile, offer);

        GeneratedCv generated = assistant.generate(UUID.randomUUID(), null, "CV — Acme", "Développeur Java",
                profile, offer, analysis);

        assertThat(generated.title()).isEqualTo("CV — Acme");
        assertThat(generated.headline()).isEqualTo("Développeur Java");
        assertThat(generated.skills()).contains("Java");
        assertThat(generated.skills()).doesNotContain("Docker");
        assertThat(generated.experiences()).hasSize(1);
        assertThat(generated.experiences().getFirst().company()).isEqualTo("Acme");
        assertThat(generated.missingInformation()).contains("Docker");
    }

    @Test
    void returnsZeroScoreWhenOfferHasNoRecognizedSkill() {
        CvAnalysis analysis = assistant.analyze(profile(), "Nous cherchons une personne motivée.");
        assertThat(analysis.score()).isZero();
        assertThat(analysis.alerts()).isNotEmpty();
    }
}
