package com.jobtrack.api.admin;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.jobtrack.application.service.PlatformAdminService;
import com.jobtrack.domain.plan.Plan;
import com.jobtrack.domain.user.UserRole;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Endpoints réservés au rôle PLATFORM_ADMIN (contrôlé dans SecurityConfig).
 * Aucune entité JPA n'est exposée : seules les vues applicatives le sont.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class PlatformAdminController {
    private final PlatformAdminService service;

    public PlatformAdminController(PlatformAdminService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public PlatformAdminService.OverviewView overview() {
        return service.overview();
    }

    @GetMapping("/revenue")
    public com.jobtrack.application.service.PaymentService.RevenueView revenue() {
        return service.revenue();
    }

    @GetMapping("/users")
    public Page<PlatformAdminService.AdminUserView> users(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Plan plan,
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listUsers(query, plan, role, pageable(page, size));
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public PlatformAdminService.AdminUserView createUser(@Valid @RequestBody CreateUserRequest request) {
        return service.createUser(request.email(), request.password(), request.role());
    }

    @GetMapping("/users/{id}")
    public PlatformAdminService.AdminUserDetailView user(@PathVariable UUID id) {
        return service.getUser(id);
    }

    @PatchMapping("/users/{id}/plan")
    public PlatformAdminService.AdminUserView updatePlan(@PathVariable UUID id,
                                                        @Valid @RequestBody PlanRequest request) {
        return service.updatePlan(id, request.plan());
    }

    @PatchMapping("/users/{id}/role")
    public PlatformAdminService.AdminUserView updateRole(@AuthenticationPrincipal String userId,
                                                        @PathVariable UUID id,
                                                        @Valid @RequestBody RoleRequest request) {
        return service.updateRole(UUID.fromString(userId), id, request.role());
    }

    @GetMapping("/organizations")
    public Page<PlatformAdminService.AdminOrganizationView> organizations(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listOrganizations(query, pageable(page, size));
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public record CreateUserRequest(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(min = 12, max = 128) String password,
            @NotNull UserRole role) {}

    public record PlanRequest(@NotNull Plan plan) {}

    public record RoleRequest(@NotNull UserRole role) {}
}
