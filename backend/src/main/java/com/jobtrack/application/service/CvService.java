package com.jobtrack.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.*;
import com.jobtrack.domain.application.Application;
import com.jobtrack.domain.cv.CvAnalysis;
import com.jobtrack.domain.cv.CvSummary;
import com.jobtrack.domain.cv.GeneratedCv;
import com.jobtrack.domain.profile.ProfileSnapshot;
import com.jobtrack.domain.user.UserProfile;

@Service
public class CvService {
    private final CvAssistant assistant;
    private final GeneratedCvRepository cvs;
    private final ProfileRepository profiles;
    private final UserProfileRepository userProfiles;
    private final ApplicationRepository applications;
    private final QuotaService quotas;
    private final CvExporter exporter;

    public CvService(CvAssistant assistant, GeneratedCvRepository cvs, ProfileRepository profiles,
                     UserProfileRepository userProfiles, ApplicationRepository applications,
                     QuotaService quotas, CvExporter exporter) {
        this.assistant = assistant;
        this.cvs = cvs;
        this.profiles = profiles;
        this.userProfiles = userProfiles;
        this.applications = applications;
        this.quotas = quotas;
        this.exporter = exporter;
    }

    @Transactional
    public CvAnalysis analyze(UUID userId, String offerText, UUID applicationId) {
        validateOffer(offerText);
        requireApplication(userId, applicationId);
        quotas.consumeCvAnalysis(userId);
        return assistant.analyze(profiles.load(userId), offerText);
    }

    @Transactional
    public GeneratedCv generate(UUID userId, String offerText, UUID applicationId, String requestedTitle) {
        validateOffer(offerText);
        Application application = requireApplication(userId, applicationId);
        quotas.consumeCvAnalysis(userId);
        ProfileSnapshot profile = profiles.load(userId);
        CvAnalysis analysis = assistant.analyze(profile, offerText);
        String headline = userProfiles.findById(userId).map(UserProfile::headline)
                .filter(value -> value != null && !value.isBlank()).orElse(null);
        String title = resolveTitle(requestedTitle, application);
        GeneratedCv generated = assistant.generate(userId, applicationId, title, headline, profile, offerText, analysis);
        return cvs.save(generated);
    }

    @Transactional(readOnly = true)
    public List<CvSummary> list(UUID userId) {
        return cvs.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public GeneratedCv get(UUID userId, UUID id) {
        return cvs.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("CV introuvable"));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        cvs.delete(get(userId, id));
    }

    @Transactional(readOnly = true)
    public ExportedFile export(UUID userId, UUID id, CvExporter.Format format) {
        GeneratedCv cv = get(userId, id);
        byte[] content = exporter.export(cv, format);
        String filename = "cv-" + cv.id() + "." + format.extension();
        return new ExportedFile(filename, format.contentType(), content);
    }

    private void validateOffer(String offerText) {
        if (offerText == null || offerText.isBlank()) {
            throw new ApiExceptions.BadRequestException("Le texte de l'offre est obligatoire");
        }
    }

    private Application requireApplication(UUID userId, UUID applicationId) {
        if (applicationId == null) {
            return null;
        }
        return applications.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Candidature introuvable"));
    }

    private String resolveTitle(String requestedTitle, Application application) {
        if (requestedTitle != null && !requestedTitle.isBlank()) {
            return requestedTitle.trim();
        }
        if (application != null) {
            return "CV — " + application.company() + " — " + application.position();
        }
        return "CV adapté";
    }

    public record ExportedFile(String filename, String contentType, byte[] content) {}
}
