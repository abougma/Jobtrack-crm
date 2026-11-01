package com.jobtrack.api.organization;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.jobtrack.application.service.ApplicationService;
import com.jobtrack.application.service.OrganizationDashboardService;
import com.jobtrack.application.service.OrganizationService;
import com.jobtrack.domain.organization.ConsentScope;
import com.jobtrack.domain.organization.OrganizationRole;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {
    private final OrganizationService organizations;
    private final OrganizationDashboardService dashboard;

    public OrganizationController(OrganizationService organizations, OrganizationDashboardService dashboard) {
        this.organizations = organizations;
        this.dashboard = dashboard;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationService.OrganizationView create(@AuthenticationPrincipal String userId,
                                                       @Valid @RequestBody NameRequest request) {
        return organizations.create(UUID.fromString(userId), request.name());
    }

    @GetMapping
    public List<OrganizationService.OrganizationView> list(@AuthenticationPrincipal String userId) {
        return organizations.list(UUID.fromString(userId));
    }

    @GetMapping("/{organizationId}")
    public OrganizationService.OrganizationView get(@AuthenticationPrincipal String userId,
                                                    @PathVariable UUID organizationId) {
        return organizations.get(UUID.fromString(userId), organizationId);
    }

    @GetMapping("/{organizationId}/groups")
    public List<OrganizationService.GroupView> groups(@AuthenticationPrincipal String userId,
                                                      @PathVariable UUID organizationId) {
        return organizations.listGroups(UUID.fromString(userId), organizationId);
    }

    @PostMapping("/{organizationId}/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationService.GroupView createGroup(@AuthenticationPrincipal String userId,
                                                     @PathVariable UUID organizationId,
                                                     @Valid @RequestBody NameRequest request) {
        return organizations.createGroup(UUID.fromString(userId), organizationId, request.name());
    }

    @PostMapping("/{organizationId}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationService.InvitationResult invite(@AuthenticationPrincipal String userId,
                                                       @PathVariable UUID organizationId,
                                                       @Valid @RequestBody InvitationRequest request) {
        return organizations.invite(UUID.fromString(userId), organizationId, request.email(), request.role());
    }

    @PostMapping("/join")
    public OrganizationService.OrganizationView join(@AuthenticationPrincipal String userId,
                                                     @Valid @RequestBody JoinRequest request) {
        return organizations.join(UUID.fromString(userId), request.token());
    }

    @GetMapping("/{organizationId}/members")
    public List<OrganizationService.MemberView> members(@AuthenticationPrincipal String userId,
                                                        @PathVariable UUID organizationId) {
        return organizations.listMembers(UUID.fromString(userId), organizationId);
    }

    @DeleteMapping("/{organizationId}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@AuthenticationPrincipal String userId, @PathVariable UUID organizationId) {
        organizations.leave(UUID.fromString(userId), organizationId);
    }

    @GetMapping("/{organizationId}/consent")
    public OrganizationService.ConsentView consent(@AuthenticationPrincipal String userId,
                                                   @PathVariable UUID organizationId) {
        return organizations.getConsent(UUID.fromString(userId), organizationId);
    }

    @PutMapping("/{organizationId}/consent")
    public OrganizationService.ConsentView consent(@AuthenticationPrincipal String userId,
                                                   @PathVariable UUID organizationId,
                                                   @Valid @RequestBody ConsentRequest request) {
        return organizations.updateConsent(UUID.fromString(userId), organizationId, request.scope());
    }

    @DeleteMapping("/{organizationId}/consent")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeConsent(@AuthenticationPrincipal String userId, @PathVariable UUID organizationId) {
        organizations.revokeConsent(UUID.fromString(userId), organizationId);
    }

    @GetMapping("/{organizationId}/dashboard")
    public OrganizationDashboardService.Snapshot dashboard(@AuthenticationPrincipal String userId,
                                                           @PathVariable UUID organizationId,
                                                           @RequestParam(required = false) UUID groupId) {
        return dashboard.get(UUID.fromString(userId), organizationId, groupId);
    }

    @GetMapping("/{organizationId}/members/{memberUserId}/applications")
    public List<ApplicationService.ApplicationSummary> memberApplications(
            @AuthenticationPrincipal String userId, @PathVariable UUID organizationId,
            @PathVariable UUID memberUserId) {
        return dashboard.memberApplications(UUID.fromString(userId), organizationId, memberUserId);
    }

    public record NameRequest(@NotBlank @Size(max = 255) String name) {}
    public record InvitationRequest(@NotBlank @Email @Size(max = 320) String email,
                                    @NotNull OrganizationRole role) {}
    public record JoinRequest(@NotBlank String token) {}
    public record ConsentRequest(@NotNull ConsentScope scope) {}
}
