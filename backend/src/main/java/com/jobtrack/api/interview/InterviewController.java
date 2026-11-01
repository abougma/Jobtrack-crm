package com.jobtrack.api.interview;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.jobtrack.application.port.InterviewAssistant;
import com.jobtrack.application.service.InterviewService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/interview")
public class InterviewController {
    private final InterviewService service;

    public InterviewController(InterviewService service) {
        this.service = service;
    }

    @PostMapping("/prepare")
    public InterviewAssistant.InterviewPreparation prepare(@AuthenticationPrincipal String userId,
                                                           @Valid @RequestBody PrepareRequest request) {
        return service.prepare(UUID.fromString(userId), request.offerText(), request.applicationId());
    }

    public record PrepareRequest(@NotBlank @Size(max = 50_000) String offerText, UUID applicationId) {}
}
