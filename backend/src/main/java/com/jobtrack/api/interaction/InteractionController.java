package com.jobtrack.api.interaction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.jobtrack.application.service.InteractionService;
import com.jobtrack.domain.crm.Interaction;
import com.jobtrack.domain.crm.InteractionType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/interactions")
public class InteractionController {
    private final InteractionService service;

    public InteractionController(InteractionService service) {
        this.service = service;
    }

    @GetMapping
    public List<InteractionResponse> list(@AuthenticationPrincipal String userId,
                                          @PathVariable UUID applicationId) {
        return service.list(UUID.fromString(userId), applicationId).stream()
                .map(InteractionResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InteractionResponse create(@AuthenticationPrincipal String userId, @PathVariable UUID applicationId,
                                      @Valid @RequestBody InteractionRequest request) {
        return InteractionResponse.from(service.create(UUID.fromString(userId), applicationId,
                request.type(), request.message(), request.occurredAt()));
    }

    @DeleteMapping("/{interactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal String userId, @PathVariable UUID applicationId,
                       @PathVariable UUID interactionId) {
        service.delete(UUID.fromString(userId), applicationId, interactionId);
    }

    public record InteractionRequest(@NotNull InteractionType type, @Size(max = 5000) String message,
                                     Instant occurredAt) {}

    public record InteractionResponse(UUID id, UUID applicationId, InteractionType type, String message,
                                      Instant occurredAt) {
        static InteractionResponse from(Interaction interaction) {
            return new InteractionResponse(interaction.id(), interaction.applicationId(), interaction.type(),
                    interaction.message(), interaction.occurredAt());
        }
    }
}
