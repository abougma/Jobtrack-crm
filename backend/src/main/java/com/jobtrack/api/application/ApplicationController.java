package com.jobtrack.api.application;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.jobtrack.application.port.ApplicationFilter;
import com.jobtrack.application.service.ApiExceptions;
import com.jobtrack.application.service.ApplicationCsvService;
import com.jobtrack.application.service.ApplicationService;
import com.jobtrack.domain.application.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {
    private static final Set<String> ALLOWED_SORT = Set.of(
            "updatedAt", "createdAt", "appliedAt", "company", "position", "status");

    private final ApplicationService service;
    private final ApplicationCsvService csv;

    public ApplicationController(ApplicationService service, ApplicationCsvService csv) {
        this.service = service;
        this.csv = csv;
    }

    @GetMapping
    public Page<ApplicationSummaryResponse> list(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ContractType contractType,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort) {
        ApplicationFilter filter = new ApplicationFilter(status, source, search, contractType, workMode, from, to);
        return service.list(UUID.fromString(userId), filter, pageable(page, size, sort))
                .map(ApplicationSummaryResponse::from);
    }

    @GetMapping("/{id}")
    public ApplicationDetailResponse get(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        return ApplicationDetailResponse.from(service.get(UUID.fromString(userId), id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationDetailResponse create(@AuthenticationPrincipal String userId,
                                            @Valid @RequestBody ApplicationRequest request) {
        return ApplicationDetailResponse.from(service.create(UUID.fromString(userId), request.toCommand()));
    }

    @PutMapping("/{id}")
    public ApplicationDetailResponse update(@AuthenticationPrincipal String userId, @PathVariable UUID id,
                                            @Valid @RequestBody ApplicationRequest request) {
        return ApplicationDetailResponse.from(service.update(UUID.fromString(userId), id, request.toCommand()));
    }

    @PatchMapping("/{id}/status")
    public ApplicationDetailResponse changeStatus(@AuthenticationPrincipal String userId, @PathVariable UUID id,
                                                  @Valid @RequestBody StatusRequest request) {
        return ApplicationDetailResponse.from(
                service.changeStatus(UUID.fromString(userId), id, request.status()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        service.delete(UUID.fromString(userId), id);
    }

    @GetMapping("/{id}/history")
    public List<ApplicationService.StatusChangeView> history(@AuthenticationPrincipal String userId,
                                                             @PathVariable UUID id) {
        return service.history(UUID.fromString(userId), id);
    }

    @GetMapping("/export.csv")
    public ResponseEntity<byte[]> export(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ContractType contractType,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        ApplicationFilter filter = new ApplicationFilter(status, source, search, contractType, workMode, from, to);
        byte[] content = csv.export(UUID.fromString(userId), filter);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("candidatures.csv").build().toString())
                .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
                .body(content);
    }

    @PostMapping("/import")
    public ApplicationCsvService.ImportResult importCsv(@AuthenticationPrincipal String userId,
                                                        @RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiExceptions.BadRequestException("Le fichier CSV est vide");
        }
        try {
            return csv.importCsv(UUID.fromString(userId), file.getInputStream());
        } catch (IOException exception) {
            throw new ApiExceptions.BadRequestException("Impossible de lire le fichier CSV");
        }
    }

    private Pageable pageable(int page, int size, String sort) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        String property = "updatedAt";
        Sort.Direction direction = Sort.Direction.DESC;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            if (parts.length > 0 && ALLOWED_SORT.contains(parts[0].trim())) {
                property = parts[0].trim();
            }
            if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())) {
                direction = Sort.Direction.ASC;
            }
        }
        return PageRequest.of(safePage, safeSize, Sort.by(direction, property));
    }

    public record ApplicationRequest(
            UUID contactId,
            @NotBlank @Size(max = 255) String company,
            @NotBlank @Size(max = 255) String position,
            @Size(max = 255) String location,
            WorkMode workMode,
            ContractType contractType,
            @Min(0) @Max(10_000_000) Integer salaryMin,
            @Min(0) @Max(10_000_000) Integer salaryMax,
            @Size(max = 100) String source,
            @Size(max = 1000) String offerUrl,
            LocalDate appliedAt,
            @Size(max = 20_000) String notes,
            ApplicationStatus status) {

        ApplicationService.ApplicationCommand toCommand() {
            if (salaryMin != null && salaryMax != null && salaryMin > salaryMax) {
                throw new ApiExceptions.BadRequestException(
                        "Le salaire minimum doit être inférieur ou égal au salaire maximum");
            }
            return new ApplicationService.ApplicationCommand(contactId, company, position, location,
                    workMode, contractType, salaryMin, salaryMax, source, offerUrl, appliedAt, notes, status);
        }
    }

    public record StatusRequest(@NotNull ApplicationStatus status) {}

    public record ApplicationSummaryResponse(
            UUID id, String company, String position, String location, WorkMode workMode,
            ContractType contractType, Integer salaryMin, Integer salaryMax, String source,
            String offerUrl, ApplicationStatus status, LocalDate appliedAt, UUID contactId,
            String contactName, Instant nextReminderAt, long interactionCount,
            Instant createdAt, Instant updatedAt) {

        static ApplicationSummaryResponse from(ApplicationService.ApplicationSummary summary) {
            return new ApplicationSummaryResponse(summary.id(), summary.company(), summary.position(),
                    summary.location(), summary.workMode(), summary.contractType(), summary.salaryMin(),
                    summary.salaryMax(), summary.source(), summary.offerUrl(), summary.status(),
                    summary.appliedAt(), summary.contactId(), summary.contactName(), summary.nextReminderAt(),
                    summary.interactionCount(), summary.createdAt(), summary.updatedAt());
        }
    }

    public record ApplicationDetailResponse(
            UUID id, String company, String position, String location, WorkMode workMode,
            ContractType contractType, Integer salaryMin, Integer salaryMax, String source,
            String offerUrl, ApplicationStatus status, LocalDate appliedAt, UUID contactId,
            String contactName, Instant nextReminderAt, long interactionCount,
            Instant createdAt, Instant updatedAt, String notes,
            List<ApplicationService.StatusChangeView> history,
            List<ApplicationService.InteractionView> interactions,
            List<ApplicationService.ReminderView> reminders, UUID generatedCvId) {

        static ApplicationDetailResponse from(ApplicationService.ApplicationDetail detail) {
            ApplicationService.ApplicationSummary summary = detail.summary();
            return new ApplicationDetailResponse(summary.id(), summary.company(), summary.position(),
                    summary.location(), summary.workMode(), summary.contractType(), summary.salaryMin(),
                    summary.salaryMax(), summary.source(), summary.offerUrl(), summary.status(),
                    summary.appliedAt(), summary.contactId(), summary.contactName(), summary.nextReminderAt(),
                    summary.interactionCount(), summary.createdAt(), summary.updatedAt(), detail.notes(),
                    detail.history(), detail.interactions(), detail.reminders(), detail.generatedCvId());
        }
    }
}
