package com.jobtrack.application.service;

import java.util.*;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.jobtrack.application.port.InterviewAssistant;
import com.jobtrack.domain.profile.ProfileSnapshot;

@Service
public class MockInterviewAssistant implements InterviewAssistant {
    private static final List<SkillKeyword> KEYWORDS = List.of(
            new SkillKeyword("Java", "java"),
            new SkillKeyword("Spring", "spring"),
            new SkillKeyword("TypeScript", "typescript"),
            new SkillKeyword("JavaScript", "javascript"),
            new SkillKeyword("React", "react"),
            new SkillKeyword("Angular", "angular"),
            new SkillKeyword("SQL", "sql"),
            new SkillKeyword("PostgreSQL", "postgres(?:ql)?"),
            new SkillKeyword("Docker", "docker"),
            new SkillKeyword("Kubernetes", "kubernetes|k8s"),
            new SkillKeyword("Python", "python"),
            new SkillKeyword("Git", "git"),
            new SkillKeyword("AWS", "aws|amazon\\s+web\\s+services"),
            new SkillKeyword("Azure", "azure"),
            new SkillKeyword("Node.js", "node(?:\\.js)?"),
            new SkillKeyword("Kafka", "kafka"),
            new SkillKeyword("Redis", "redis"),
            new SkillKeyword("Maven", "maven"),
            new SkillKeyword("JUnit", "junit"),
            new SkillKeyword("REST", "rest(?:ful)?"),
            new SkillKeyword("GraphQL", "graphql"),
            new SkillKeyword("Linux", "linux"),
            new SkillKeyword("Agile", "agile"),
            new SkillKeyword("Scrum", "scrum"),
            new SkillKeyword("Microservices", "microservices?"),
            new SkillKeyword("DevOps", "devops")
    );

    @Override
    public InterviewPreparation prepare(ProfileSnapshot profile, String offerText) {
        Set<String> known = knownSkills(profile);
        List<String> requested = requestedSkills(offerText);

        List<String> projectNames = profile.projects().stream().map(project -> project.name()).limit(3).toList();
        List<String> experienceTitles = profile.experiences().stream()
                .map(experience -> experience.jobTitle() + " — " + experience.company()).limit(3).toList();

        List<Question> technical = requested.stream().map(skill -> {
            boolean covered = known.contains(normalize(skill));
            return new Question(
                    "Comment avez-vous utilisé " + skill + " dans un projet réel ?",
                    covered
                            ? "Préparez un exemple concret issu de votre profil et décrivez vos décisions techniques."
                            : "Cette compétence apparaît dans l'offre mais pas dans votre profil : préparez une réponse honnête.",
                    covered ? projectNames : List.of());
        }).toList();

        List<Question> behavioral = List.of(
                new Question("Parlez-nous d'une difficulté technique que vous avez surmontée.",
                        "Structurez votre réponse avec la méthode STAR et restez factuel.",
                        experienceTitles.isEmpty() ? projectNames : experienceTitles),
                new Question("Pourquoi ce poste et cette entreprise ?",
                        "Reliez votre motivation aux éléments réels de l'offre, sans exagérer votre expérience.",
                        List.of()),
                new Question("Comment organisez-vous votre travail lorsque plusieurs priorités se cumulent ?",
                        "Donnez une méthode concrète et un exemple vécu.",
                        experienceTitles));

        List<String> missing = new ArrayList<>();
        if (profile.experiences().isEmpty()) missing.add("Un exemple professionnel à raconter");
        if (profile.projects().isEmpty()) missing.add("Un projet concret à présenter");
        if (requested.isEmpty()) missing.add("Des compétences techniques identifiées dans l'offre");

        return new InterviewPreparation(
                technical,
                behavioral,
                List.of("Situation : quel était le contexte ?",
                        "Tâche : quelle responsabilité aviez-vous ?",
                        "Action : qu'avez-vous fait concrètement ?",
                        "Résultat : qu'avez-vous obtenu ou appris ?"),
                List.of("Comment mesurez-vous la réussite sur ce poste ?",
                        "Quelle sera la priorité des trois premiers mois ?",
                        "Comment l'équipe accompagne-t-elle la progression ?",
                        "Quelles sont les prochaines étapes du processus ?"),
                missing);
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
        return known;
    }

    private List<String> requestedSkills(String offerText) {
        if (offerText == null || offerText.isBlank()) {
            return List.of();
        }
        return KEYWORDS.stream().filter(keyword -> keyword.matches(offerText))
                .map(SkillKeyword::canonical).distinct().toList();
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
