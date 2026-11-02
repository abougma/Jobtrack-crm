package com.jobtrack.api.profile;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.jobtrack.application.service.ProfileService;
import com.jobtrack.domain.profile.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public ProfileSnapshot get(@AuthenticationPrincipal String userId) {
        return service.get(UUID.fromString(userId));
    }

    @PostMapping("/experiences")
    @ResponseStatus(HttpStatus.CREATED)
    public Experience addExperience(@AuthenticationPrincipal String userId,
                                    @Valid @RequestBody ExperienceRequest request) {
        return service.addExperience(UUID.fromString(userId), request.toDraft());
    }

    @PutMapping("/experiences/{id}")
    public Experience updateExperience(@AuthenticationPrincipal String userId, @PathVariable UUID id,
                                       @Valid @RequestBody ExperienceRequest request) {
        return service.updateExperience(UUID.fromString(userId), id, request.toDraft());
    }

    @DeleteMapping("/experiences/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExperience(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        service.deleteExperience(UUID.fromString(userId), id);
    }

    @PostMapping("/educations")
    @ResponseStatus(HttpStatus.CREATED)
    public Education addEducation(@AuthenticationPrincipal String userId,
                                  @Valid @RequestBody EducationRequest request) {
        return service.addEducation(UUID.fromString(userId), request.toDraft());
    }

    @PutMapping("/educations/{id}")
    public Education updateEducation(@AuthenticationPrincipal String userId, @PathVariable UUID id,
                                     @Valid @RequestBody EducationRequest request) {
        return service.updateEducation(UUID.fromString(userId), id, request.toDraft());
    }

    @DeleteMapping("/educations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEducation(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        service.deleteEducation(UUID.fromString(userId), id);
    }

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public Project addProject(@AuthenticationPrincipal String userId,
                              @Valid @RequestBody ProjectRequest request) {
        return service.addProject(UUID.fromString(userId), request.toDraft());
    }

    @PutMapping("/projects/{id}")
    public Project updateProject(@AuthenticationPrincipal String userId, @PathVariable UUID id,
                                 @Valid @RequestBody ProjectRequest request) {
        return service.updateProject(UUID.fromString(userId), id, request.toDraft());
    }

    @DeleteMapping("/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        service.deleteProject(UUID.fromString(userId), id);
    }

    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public Skill addSkill(@AuthenticationPrincipal String userId,
                          @Valid @RequestBody SkillRequest request) {
        return service.addSkill(UUID.fromString(userId), request.toDraft());
    }

    @PutMapping("/skills/{id}")
    public Skill updateSkill(@AuthenticationPrincipal String userId, @PathVariable UUID id,
                             @Valid @RequestBody SkillRequest request) {
        return service.updateSkill(UUID.fromString(userId), id, request.toDraft());
    }

    @DeleteMapping("/skills/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkill(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        service.deleteSkill(UUID.fromString(userId), id);
    }

    @PostMapping("/languages")
    @ResponseStatus(HttpStatus.CREATED)
    public Language addLanguage(@AuthenticationPrincipal String userId,
                                @Valid @RequestBody LanguageRequest request) {
        return service.addLanguage(UUID.fromString(userId), request.toDraft());
    }

    @PutMapping("/languages/{id}")
    public Language updateLanguage(@AuthenticationPrincipal String userId, @PathVariable UUID id,
                                   @Valid @RequestBody LanguageRequest request) {
        return service.updateLanguage(UUID.fromString(userId), id, request.toDraft());
    }

    @DeleteMapping("/languages/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLanguage(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        service.deleteLanguage(UUID.fromString(userId), id);
    }

    public record ExperienceRequest(
            @NotBlank @Size(max = 255) String company,
            @NotBlank @Size(max = 255) String jobTitle,
            @NotNull LocalDate startedOn,
            LocalDate endedOn,
            @Size(max = 10_000) String description) {
        ProfileService.ExperienceDraft toDraft() {
            return new ProfileService.ExperienceDraft(company, jobTitle, startedOn, endedOn, description);
        }
    }

    public record EducationRequest(
            @NotBlank @Size(max = 255) String institution,
            @NotBlank @Size(max = 255) String degree,
            LocalDate startedOn,
            LocalDate endedOn,
            @Size(max = 10_000) String description) {
        ProfileService.EducationDraft toDraft() {
            return new ProfileService.EducationDraft(institution, degree, startedOn, endedOn, description);
        }
    }

    public record ProjectRequest(
            @NotBlank @Size(max = 255) String name,
            @NotBlank @Size(max = 10_000) String description,
            @Size(max = 1000) String technologies) {
        ProfileService.ProjectDraft toDraft() {
            return new ProfileService.ProjectDraft(name, description, technologies);
        }
    }

    public record SkillRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull SkillLevel level,
            @NotNull SkillContext context,
            @NotBlank @Size(max = 100) String category) {
        ProfileService.SkillDraft toDraft() {
            return new ProfileService.SkillDraft(name, level, context, category);
        }
    }

    public record LanguageRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull LanguageLevel level) {
        ProfileService.LanguageDraft toDraft() {
            return new ProfileService.LanguageDraft(name, level);
        }
    }
}
