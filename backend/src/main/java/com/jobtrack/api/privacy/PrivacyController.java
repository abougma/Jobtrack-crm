package com.jobtrack.api.privacy;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.jobtrack.application.service.ApiExceptions;
import com.jobtrack.application.service.PrivacyService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1/privacy")
public class PrivacyController {
    private final PrivacyService privacy;

    public PrivacyController(PrivacyService privacy) {
        this.privacy = privacy;
    }

    @GetMapping("/export")
    public Map<String, Object> export(@AuthenticationPrincipal String userId) {
        return privacy.export(UUID.fromString(userId));
    }

    @DeleteMapping("/account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal String userId, @Valid @RequestBody DeleteRequest request) {
        if (!Boolean.TRUE.equals(request.confirm())) {
            throw new ApiExceptions.BadRequestException("La suppression doit être confirmée");
        }
        privacy.deleteAccount(UUID.fromString(userId));
    }

    public record DeleteRequest(@NotNull Boolean confirm) {}
}
