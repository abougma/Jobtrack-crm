package com.jobtrack.api.plan;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobtrack.application.service.QuotaService;

@RestController
@RequestMapping("/api/v1/plan")
public class PlanController {
    private final QuotaService quotas;

    public PlanController(QuotaService quotas) {
        this.quotas = quotas;
    }

    @GetMapping
    public QuotaService.QuotaSnapshot get(@AuthenticationPrincipal String userId) {
        return quotas.get(UUID.fromString(userId));
    }
}
