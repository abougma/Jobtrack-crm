package com.jobtrack.api.plan;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.jobtrack.application.port.UserPlanRepository;
import com.jobtrack.domain.plan.Plan;
import com.jobtrack.domain.plan.UserPlan;
import com.jobtrack.application.service.PaymentService;

import com.jobtrack.application.service.QuotaService;

@RestController
@RequestMapping("/api/v1/plan")
public class PlanController {
    private final QuotaService quotas;
    private final UserPlanRepository plans;
    private final PaymentService payments;

    public PlanController(QuotaService quotas, UserPlanRepository plans, PaymentService payments) {
        this.quotas = quotas;
        this.plans = plans;
        this.payments = payments;
    }

    @GetMapping
    public QuotaService.QuotaSnapshot get(@AuthenticationPrincipal String userId) {
        return quotas.get(UUID.fromString(userId));
    }

    @PostMapping("/checkout")
    public PaymentService.CheckoutView checkout(@AuthenticationPrincipal String userId,
                                                @Valid @RequestBody PlanRequest request) {
        return payments.createCheckout(UUID.fromString(userId), request.plan());
    }

    public record PlanRequest(@NotNull Plan plan) {}
}
