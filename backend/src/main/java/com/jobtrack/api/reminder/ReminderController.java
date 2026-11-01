package com.jobtrack.api.reminder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.jobtrack.application.service.ReminderService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1")
public class ReminderController {
    private final ReminderService service;

    public ReminderController(ReminderService service) {
        this.service = service;
    }

    @GetMapping("/applications/{applicationId}/reminders")
    public List<ReminderService.ReminderView> list(@AuthenticationPrincipal String userId,
                                                   @PathVariable UUID applicationId) {
        return service.list(UUID.fromString(userId), applicationId);
    }

    @PostMapping("/applications/{applicationId}/reminders")
    @ResponseStatus(HttpStatus.CREATED)
    public ReminderService.ReminderView create(@AuthenticationPrincipal String userId,
                                               @PathVariable UUID applicationId,
                                               @Valid @RequestBody ReminderRequest request) {
        return service.create(UUID.fromString(userId), applicationId, request.title(), request.dueAt());
    }

    @PatchMapping("/applications/{applicationId}/reminders/{reminderId}/complete")
    public ReminderService.ReminderView complete(@AuthenticationPrincipal String userId,
                                                 @PathVariable UUID applicationId,
                                                 @PathVariable UUID reminderId) {
        return service.complete(UUID.fromString(userId), applicationId, reminderId);
    }

    @DeleteMapping("/applications/{applicationId}/reminders/{reminderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal String userId, @PathVariable UUID applicationId,
                       @PathVariable UUID reminderId) {
        service.delete(UUID.fromString(userId), applicationId, reminderId);
    }

    @GetMapping("/reminders")
    public List<ReminderService.ReminderView> global(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) Boolean completed) {
        return service.global(UUID.fromString(userId), from, to, completed);
    }

    public record ReminderRequest(@NotBlank @Size(max = 255) String title, @NotNull Instant dueAt) {}
}
