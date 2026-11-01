package com.jobtrack.application.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jobtrack.application.port.CvAssistant;
import com.jobtrack.domain.cv.*;
import com.jobtrack.domain.profile.*;

@Service
public class MockCvAssistant implements CvAssistant {
    private static final List<SkillKeyword> KEYWORDS = List.of(
            new SkillKeyword("Java", "java"),
            new SkillKeyword("Spring", "spring"),
            new SkillKeyword("Spring Boot", "spring\\s+boot"),
            new SkillKeyword("TypeScript", "typescript"),
            new SkillKeyword("JavaScript", "javascript"),
            new SkillKeyword("React", "react"),
            new SkillKeyword("Angular", "angular"),
            new SkillKeyword("Vue.js", "vue(?:\\.js)?"),
            new SkillKeyword("SQL", "sql"),
            new SkillKeyword("PostgreSQL", "postgres(?:ql)?"),
            new SkillKeyword("MySQL", "mysql"),
            new SkillKeyword("Docker", "docker"),
            new SkillKeyword("Kubernetes", "kubernetes|k8s"),
            new SkillKeyword("Python", "python"),
            new SkillKeyword("Git", "git"),
            new SkillKeyword("AWS", "aws|amazon\\s+web\\s+services"),
            new SkillKeyword("Azure", "azure"),
            new SkillKeyword("GCP", "gcp|google\\s+cloud"),
            new SkillKeyword("Node.js", "node(?:\\.js)?"),
            new SkillKeyword("Kafka", "kafka"),
            new SkillKeyword("Redis", "redis"),
            new SkillKeyword("Maven", "maven"),
            new SkillKeyword("Gradle", "gradle"),
            new SkillKeyword("JUnit", "junit"),
            new SkillKeyword("REST", "rest(?:ful)?"),
            new SkillKeyword("GraphQL", "graphql"),
            new SkillKeyword("CI/CD", "ci\\s*/\\s*cd|int[ée]gration\\s+continue"),
            new SkillKeyword("Linux", "linux"),
            new SkillKeyword("Agile", "agile"),
            new SkillKeyword("Scrum", "scrum"),
            new SkillKeyword("HTML", "html"),
            new SkillKeyword("CSS", "css"),
            new SkillKeyword("Tailwind", "tailwind"),
            new SkillKeyword("Hibernate", "hibernate"),
            new SkillKeyword("JPA", "jpa"),
            new SkillKeyword("Microservices", "microservices?"),
            new SkillKeyword("TDD", "tdd"),
            new SkillKeyword("DevOps", "devops")
    );

    @Override
    public CvAnalysis analyze(ProfileSnapshot profile, String offerText) {
        Set<String> known = knownSkills(profile);
        List<String> requested = requestedSkills(offerText);
        List<String> covered = requested.stream()
                .filter(skill -> known.contains(normalize(skill))).toList();
        List<String> missing = requested.stream()
                .filter(skill -> !known.contains(normalize(skill))).toList();

        List<String> missingInformation = new ArrayList<>();
        if (profile.experiences().isEmpty()) missingInformation.add("Expérience professionnelle");
        if (profile.educations().isEmpty()) missingInformation.add("Formation");
        if (profile.projects().isEmpty()) missingInformation.add("Projet concret");
        if (profile.languages().isEmpty()) missingInformation.add("Langues");
        if (profile.skills().isEmpty()) missingInformation.add("Compétences");

        List<String> alerts = new ArrayList<>();
        if (requested.isEmpty()) {
            alerts.add("Aucune compétence technique reconnue dans l'offre : vérifiez que le texte est complet.");
        }
        if (!missing.isEmpty()) {
            alerts.add("Compétences demandées absentes du profil : " + String.join(", ", missing)
                    + ". Ne les ajoutez que si vous les avez réellement pratiquées.");
        }
        if (profile.experiences().isEmpty()) {
            alerts.add("Aucune expérience renseignée : le CV manquera de preuves professionnelles.");
        }
        if (profile.educations().isEmpty()) {
            alerts.add("Aucune formation renseignée : complétez votre parcours avant de générer le CV.");
        }
        if (offerText != null && offerText.trim().length() < 100) {
            alerts.add("Le texte de l'offre semble court : l'analyse peut être incomplète.");
        }

        int score = requested.isEmpty() ? 0
                : (int) Math.round(100.0 * covered.size() / requested.size());
        if (score > 0 && score < 40) {
            alerts.add("Correspondance faible : ciblez des offres plus proches ou complétez votre profil.");
        }

        String experienceGap = experienceGap(profile, offerText, missing);
        List<String> suggestions = suggestions(profile, missing, score);
        return new CvAnalysis(score, covered, missing, missingInformation, alerts, experienceGap, suggestions);
    }

    @Override
    public GeneratedCv generate(UUID userId, UUID applicationId, String title, String headline,
                                ProfileSnapshot profile, String offerText, CvAnalysis analysis) {
        List<Skill> sortedSkills = profile.skills().stream()
                .sorted(Comparator.comparing(Skill::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        LinkedHashSet<String> skills = new LinkedHashSet<>(analysis.coveredSkills());
        sortedSkills.forEach(skill -> skills.add(skill.name()));

        List<CvExperience> experiences = profile.experiences().stream()
                .sorted(Comparator.comparing(Experience::startedOn,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .map(experience -> new CvExperience(experience.jobTitle(), experience.company(),
                        experience.startedOn(), experience.endedOn(), experience.description()))
                .toList();

        List<CvProject> projects = profile.projects().stream()
                .sorted(Comparator.comparing(Project::name, String.CASE_INSENSITIVE_ORDER))
                .limit(6)
                .map(project -> new CvProject(project.name(), project.description(), project.technologies()))
                .toList();

        List<CvEducation> educations = profile.educations().stream()
                .sorted(Comparator.comparing(Education::startedOn,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(4)
                .map(education -> new CvEducation(education.degree(), education.institution(),
                        education.startedOn(), education.endedOn()))
                .toList();

        String resolvedHeadline = headline != null && !headline.isBlank()
                ? headline.trim()
                : profile.experiences().stream().findFirst().map(Experience::jobTitle).orElse("Profil professionnel");
        String summary = summary(profile, resolvedHeadline, analysis);

        LinkedHashSet<String> missingInformation = new LinkedHashSet<>(analysis.missingInformation());
        missingInformation.addAll(analysis.missingSkills());

        return new GeneratedCv(UUID.randomUUID(), userId, applicationId, title, resolvedHeadline, summary,
                List.copyOf(skills), experiences, projects, educations, List.copyOf(missingInformation),
                analysis.score(), java.time.Instant.now());
    }

    private Set<String> knownSkills(ProfileSnapshot profile) {
        Set<String> known = new LinkedHashSet<>();
        profile.skills().forEach(skill -> known.add(normalize(skill.name())));
        profile.projects().forEach(project -> {
            if (project.technologies() != null) {
                Arrays.stream(project.technologies().split("[,;/]"))
                        .map(String::trim).filter(value -> !value.isBlank())
                        .forEach(value -> known.add(normalize(value)));
            }
        });
        profile.experiences().forEach(experience -> {
            if (experience.description() != null) {
                KEYWORDS.stream().filter(keyword -> keyword.matches(experience.description()))
                        .forEach(keyword -> known.add(normalize(keyword.canonical())));
            }
        });
        return known;
    }

    private List<String> requestedSkills(String offerText) {
        if (offerText == null || offerText.isBlank()) {
            return List.of();
        }
        return KEYWORDS.stream().filter(keyword -> keyword.matches(offerText))
                .map(SkillKeyword::canonical).distinct().toList();
    }

    private String experienceGap(ProfileSnapshot profile, String offerText, List<String> missing) {
        double totalYears = totalYears(profile.experiences());
        Matcher matcher = Pattern.compile("(\\d+)\\s*(?:ans|ann[ée]es|an)\\b", Pattern.CASE_INSENSITIVE)
                .matcher(offerText == null ? "" : offerText);
        if (matcher.find()) {
            int requestedYears = Integer.parseInt(matcher.group(1));
            if (totalYears < requestedYears) {
                return "L'offre demande " + requestedYears + " an(s) d'expérience ; votre profil en cumule environ "
                        + formatYears(totalYears) + ".";
            }
            return "L'offre demande " + requestedYears + " an(s) d'expérience ; votre profil en cumule environ "
                    + formatYears(totalYears) + ".";
        }
        if (profile.experiences().isEmpty()) {
            return "Aucune expérience professionnelle renseignée : impossible d'évaluer l'écart d'expérience.";
        }
        if (!missing.isEmpty()) {
            return "Écart possible sur les compétences suivantes : " + String.join(", ", missing) + ".";
        }
        return "Aucun écart majeur détecté entre les expériences renseignées et l'offre.";
    }

    private List<String> suggestions(ProfileSnapshot profile, List<String> missing, int score) {
        List<String> suggestions = new ArrayList<>();
        if (!missing.isEmpty()) {
            suggestions.add("Ajoutez une expérience ou un projet réel utilisant " + String.join(", ", missing)
                    + " si c'est le cas.");
        }
        if (profile.projects().isEmpty()) {
            suggestions.add("Ajoutez un projet personnel ou académique concret avec les technologies utilisées.");
        }
        if (profile.experiences().isEmpty()) {
            suggestions.add("Ajoutez vos stages, alternances ou expériences professionnelles.");
        }
        if (profile.educations().isEmpty()) {
            suggestions.add("Complétez votre formation (diplôme, établissement, dates).");
        }
        if (score >= 70) {
            suggestions.add("Mettez en avant les compétences couvertes dans le résumé et les expériences.");
        }
        return suggestions;
    }

    private String summary(ProfileSnapshot profile, String headline, CvAnalysis analysis) {
        double years = totalYears(profile.experiences());
        String experience = years > 0
                ? "Profil avec environ " + formatYears(years) + " d'expérience."
                : "Profil en construction, sans expérience professionnelle renseignée.";
        String skills = analysis.coveredSkills().isEmpty()
                ? "Compétences issues du profil uniquement."
                : "Compétences couvertes : " + String.join(", ", analysis.coveredSkills()) + ".";
        return experience + " " + headline + ". " + skills
                + " CV généré uniquement à partir d'éléments réels du profil.";
    }

    private double totalYears(List<Experience> experiences) {
        long months = experiences.stream().mapToLong(experience -> {
            LocalDate start = experience.startedOn();
            LocalDate end = experience.endedOn() == null ? LocalDate.now() : experience.endedOn();
            if (start == null || end.isBefore(start)) {
                return 0;
            }
            return ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1));
        }).sum();
        return Math.round(months / 12.0 * 10.0) / 10.0;
    }

    private String formatYears(double years) {
        return String.format(Locale.FRANCE, "%.1f an(s)", years);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record SkillKeyword(String canonical, String regex) {
        boolean matches(String text) {
            return Pattern.compile("(?<![\\p{L}\\d])(" + regex + ")(?![\\p{L}\\d])", Pattern.CASE_INSENSITIVE)
                    .matcher(text).find();
        }
    }
}
