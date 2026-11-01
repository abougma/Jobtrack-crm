package com.jobtrack.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.jobtrack.application.port.ApplicationRepository;
import com.jobtrack.application.port.MembershipRepository;
import com.jobtrack.application.port.OrganizationRepository;
import com.jobtrack.application.port.PasswordHasher;
import com.jobtrack.application.port.UserPlanRepository;
import com.jobtrack.application.port.UserProfileRepository;
import com.jobtrack.application.port.UserRepository;
import com.jobtrack.domain.plan.Plan;
import com.jobtrack.domain.plan.UserPlan;
import com.jobtrack.domain.user.User;
import com.jobtrack.domain.user.UserProfile;
import com.jobtrack.domain.user.UserRole;

class PlatformAdminServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final UserProfileRepository profiles = mock(UserProfileRepository.class);
    private final UserPlanRepository plans = mock(UserPlanRepository.class);
    private final OrganizationRepository organizations = mock(OrganizationRepository.class);
    private final MembershipRepository memberships = mock(MembershipRepository.class);
    private final ApplicationRepository applications = mock(ApplicationRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final PlatformAdminService service = new PlatformAdminService(
            users, profiles, plans, organizations, memberships, applications, passwordHasher);

    @Test
    void aggregatesPlatformIndicators() {
        when(users.count()).thenReturn(42L);
        when(users.countByRole(UserRole.PLATFORM_ADMIN)).thenReturn(2L);
        when(users.countByCreatedAtAfter(any())).thenReturn(7L);
        when(organizations.count()).thenReturn(3L);
        when(memberships.count()).thenReturn(15L);
        when(applications.count()).thenReturn(120L);
        when(plans.countByPlan(Plan.FREE)).thenReturn(30L);
        when(plans.countByPlan(Plan.PRO)).thenReturn(10L);
        when(plans.countByPlan(Plan.ORGANIZATION)).thenReturn(2L);

        PlatformAdminService.OverviewView overview = service.overview();

        assertThat(overview.totalUsers()).isEqualTo(42);
        assertThat(overview.platformAdmins()).isEqualTo(2);
        assertThat(overview.newUsersLast30Days()).isEqualTo(7);
        assertThat(overview.totalOrganizations()).isEqualTo(3);
        assertThat(overview.totalMemberships()).isEqualTo(15);
        assertThat(overview.totalApplications()).isEqualTo(120);
        assertThat(overview.usersByPlan())
                .extracting(PlatformAdminService.PlanMetric::users)
                .containsExactly(30L, 10L, 2L);
    }

    @Test
    void listsUsersWithTheirPlanAndProfile() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "ada@example.com", "hash", UserRole.JOB_SEEKER,
                Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T10:00:00Z"));
        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1);
        when(users.search(eq("ada"), eq(UserRole.JOB_SEEKER), eq(Plan.PRO), any())).thenReturn(page);
        when(profiles.findAllByIdIn(List.of(userId)))
                .thenReturn(List.of(new UserProfile(userId, "Ada", "Lovelace", "Dev", "Paris", Instant.now())));
        when(plans.findAllByIdIn(List.of(userId)))
                .thenReturn(List.of(UserPlan.create(userId).changePlan(Plan.PRO)));

        Page<PlatformAdminService.AdminUserView> result = service.listUsers(
                "ada", Plan.PRO, UserRole.JOB_SEEKER, PageRequest.of(0, 20));

        assertThat(result.getContent()).singleElement().satisfies(view -> {
            assertThat(view.email()).isEqualTo("ada@example.com");
            assertThat(view.plan()).isEqualTo(Plan.PRO);
            assertThat(view.role()).isEqualTo(UserRole.JOB_SEEKER);
            assertThat(view.firstName()).isEqualTo("Ada");
            assertThat(view.lastName()).isEqualTo("Lovelace");
        });
    }

    @Test
    void changesThePlanOfAnotherUser() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "ada@example.com", "hash", UserRole.JOB_SEEKER, Instant.now(), Instant.now());
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(plans.findById(userId)).thenReturn(Optional.of(UserPlan.create(userId)));
        when(profiles.findById(userId)).thenReturn(Optional.of(UserProfile.empty(userId)));

        PlatformAdminService.AdminUserView view = service.updatePlan(userId, Plan.PRO);

        assertThat(view.plan()).isEqualTo(Plan.PRO);
        verify(plans).save(any(UserPlan.class));
    }

    @Test
    void refusesToRemoveOwnAdminRole() {
        UUID adminId = UUID.randomUUID();
        User admin = new User(adminId, "admin@example.com", "hash", UserRole.PLATFORM_ADMIN,
                Instant.now(), Instant.now());
        when(users.findById(adminId)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.updateRole(adminId, adminId, UserRole.JOB_SEEKER))
                .isInstanceOf(ApiExceptions.BadRequestException.class)
                .hasMessageContaining("propre rôle administrateur");
        verify(users, never()).save(any(User.class));
    }

    @Test
    void refusesToRemoveTheLastPlatformAdmin() {
        UUID actorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        User target = new User(targetId, "last@example.com", "hash", UserRole.PLATFORM_ADMIN,
                Instant.now(), Instant.now());
        when(users.findById(targetId)).thenReturn(Optional.of(target));
        when(users.countByRole(UserRole.PLATFORM_ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.updateRole(actorId, targetId, UserRole.JOB_SEEKER))
                .isInstanceOf(ApiExceptions.ConflictException.class)
                .hasMessageContaining("administrateur plateforme");
        verify(users, never()).save(any(User.class));
    }

    @Test
    void createsAnAdministratorAccount() {
        when(users.existsByEmail("new-admin@example.com")).thenReturn(false);
        when(passwordHasher.hash("motdepasse-solide-123")).thenReturn("hashed");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlatformAdminService.AdminUserView view = service.createUser(
                "New-Admin@Example.com ", "motdepasse-solide-123", UserRole.PLATFORM_ADMIN);

        assertThat(view.email()).isEqualTo("new-admin@example.com");
        assertThat(view.role()).isEqualTo(UserRole.PLATFORM_ADMIN);
        assertThat(view.plan()).isEqualTo(Plan.FREE);
        verify(profiles).save(any(UserProfile.class));
        verify(plans).save(any(UserPlan.class));
    }

    @Test
    void refusesToCreateADuplicateAccount() {
        when(users.existsByEmail("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser("ada@example.com", "motdepasse-solide-123",
                UserRole.JOB_SEEKER))
                .isInstanceOf(ApiExceptions.ConflictException.class)
                .hasMessageContaining("déjà utilisée");
        verify(users, never()).save(any(User.class));
    }

    @Test
    void promotesAUserToPlatformAdmin() {
        UUID actorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        User target = new User(targetId, "new@example.com", "hash", UserRole.JOB_SEEKER,
                Instant.now(), Instant.now());
        when(users.findById(targetId)).thenReturn(Optional.of(target));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profiles.findById(targetId)).thenReturn(Optional.of(UserProfile.empty(targetId)));
        when(plans.findById(targetId)).thenReturn(Optional.of(UserPlan.create(targetId)));

        PlatformAdminService.AdminUserView view = service.updateRole(actorId, targetId, UserRole.PLATFORM_ADMIN);

        assertThat(view.role()).isEqualTo(UserRole.PLATFORM_ADMIN);
    }
}
