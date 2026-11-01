package com.jobtrack.api.cv;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.jobtrack.application.port.CvExporter;
import com.jobtrack.application.service.CvService;
import com.jobtrack.domain.cv.CvAnalysis;
import com.jobtrack.domain.cv.CvSummary;
import com.jobtrack.domain.cv.GeneratedCv;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/cv")
public class CvController {
    private final CvService service;

    public CvController(CvService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public CvAnalysis analyze(@AuthenticationPrincipal String userId,
                              @Valid @RequestBody AnalyzeRequest request) {
        return service.analyze(UUID.fromString(userId), request.offerText(), request.applicationId());
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public GeneratedCv generate(@AuthenticationPrincipal String userId,
                                @Valid @RequestBody GenerateRequest request) {
        return service.generate(UUID.fromString(userId), request.offerText(), request.applicationId(),
                request.title());
    }

    @PostMapping("/generate-from-profile")
    @ResponseStatus(HttpStatus.CREATED)
    public GeneratedCv generateFromProfile(@AuthenticationPrincipal String userId,
                                           @Valid @RequestBody ProfileGenerateRequest request) {
        return service.generateFromProfile(UUID.fromString(userId), request.title(), request.fullName(),
                request.location(), request.summary());
    }

    @GetMapping
    public List<CvSummary> list(@AuthenticationPrincipal String userId) {
        return service.list(UUID.fromString(userId));
    }

    @GetMapping("/{id}")
    public GeneratedCv get(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        return service.get(UUID.fromString(userId), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        service.delete(UUID.fromString(userId), id);
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(@AuthenticationPrincipal String userId, @PathVariable UUID id,
                                         @RequestParam(defaultValue = "docx") String format) {
        CvExporter.Format parsed;
        try {
            parsed = CvExporter.Format.from(format);
        } catch (IllegalArgumentException exception) {
            throw new com.jobtrack.application.service.ApiExceptions.BadRequestException(
                    "Format d'export inconnu : " + format);
        }
        CvService.ExportedFile file = service.export(UUID.fromString(userId), id, parsed);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.filename()).build().toString())
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.content());
    }

    public record AnalyzeRequest(@NotBlank @Size(max = 50_000) String offerText, UUID applicationId) {}

    public record GenerateRequest(@NotBlank @Size(max = 50_000) String offerText, UUID applicationId,
                                  @Size(max = 255) String title) {}

    public record ProfileGenerateRequest(
            @NotBlank @Size(max = 1_000) String title,
            @NotBlank @Size(max = 255) String fullName,
            @Size(max = 255) String location,
            @NotBlank @Size(max = 2_000) String summary) {}
}
