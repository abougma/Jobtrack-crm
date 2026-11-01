package com.jobtrack.application.port;

import java.util.List;

import com.jobtrack.domain.profile.ProfileSnapshot;

public interface InterviewAssistant {
    InterviewPreparation prepare(ProfileSnapshot profile, String offerText);

    record InterviewPreparation(
            List<Question> technicalQuestions,
            List<Question> behavioralQuestions,
            List<String> starPrompts,
            List<String> recruiterQuestions,
            List<String> missingEvidence) {}

    record Question(String question, String reason, List<String> profileEvidence) {}
}
