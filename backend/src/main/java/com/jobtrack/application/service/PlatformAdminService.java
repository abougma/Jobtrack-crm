package com.jobtrack.application.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.ApplicationRepository;
import com.jobtrack.application.port.MembershipRepository;
import com.jobtrack.application.port.OrganizationRepository;
import com.jobtrack.application.port.PasswordHasher;
import com.jobtrack.application.port.UserPlanRepository;
import com.jobtrack.application.port.UserProfileRepository;
import com.jobtrack.application.port.UserRepository;
import com.jobtrack.domain.organization.Organization;
import com.jobtrack.domain.plan.Plan;
import com.jobtrack.domain.plan.UserPlan;
import com.jobtrack.domain.user.User;
import com.jobtrack.domain.user.UserProfile;
import com.jobtrack.domain.user.UserRole;

/**
 * Console d'administration plateforme : indicateurs globaux, gestion des
 * comptes (plan, rôle) et supervision des organisations.
 * Réservé au rôle PLATFORM_ADMIN, contrôlé au niveau de la couche API.
 */
@Service
public class PlatformAdminService {
    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final UserPlanRepository plans;
    private final OrganizationRepository organizations;
    private final MembershipRepository memberships;
    private final ApplicationRepository applications;
    private final PasswordHasher passwordHasher;

    public PlatformAdminService(UserRepository users, UserProfileRepository profiles,
                                UserPlanRepository plans, OrganizationRepository organizations,
                                MembershipRepository memberships, ApplicationRepository applications,
                                PasswordHasher passwordHasher) {
        this.users = users;
        this.profiles = profiles;
        this.plans = plans;
        this.organizations = organizations;
        this.memberships = memberships;
        this.applications = applications;
        this.passwordHasher = passwordHasher;
    }

    @Transactional(readOnly = true)
    public OverviewView overview() {
        return new OverviewView(
                users.count(),
                users.countByRole(UserRole.PLATFORM_ADMIN),
                users.countByCreatedAtAfter(Instant.now().minus(30, ChronoUnit.DAYS)),
                organizations.count(),
                memberships.count(),
                applications.count(),
                Arrays.stream(Plan.values())
                        .map(plan -> new PlanMetric(plan, plans.countByPlan(plan)))
                        .toList());
    }

    @Transactional(readOnly = true)
    public Page<AdminUserView> listUsers(String query, Plan plan, UserRole role, Pageable pageable) {
        Page<User> page = users.search(query, role, plan, pageable);
        List<UUID> ids = page.getContent().stream().map(User::id).toList();
        Map<UUID, UserProfile> profilesById = profiles.findAllByIdIn(ids).stream()
                .collect(Collectors.toMap(UserProfile::userId, Function.identity()));
        Map<UUID, Plan> plansById = plans.findAllByIdIn(ids).stream()
                .collect(Collectors.toMap(UserPlan::userId, UserPlan::plan));
        return page.map(user -> view(user, profilesById.get(user.id()), plansById.get(user.id())));
    }

    @Transactional(readOnly = true)
    public AdminUserDetailView getUser(UUID userId) {
        User user = requireUser(userId);
        UserProfile profile = profiles.findById(userId).orElseGet(() -> UserProfile.empty(userId));
        UserPlan plan = plans.findById(userId).orElseGet(() -> UserPlan.create(userId));
        List<String> organizationNames = memberships.findAllByUserId(userId).stream()
                .map(membership -> organizations.findById(membership.organizationId())
                        .map(Organization::name).orElse(null))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return new AdminUserDetailView(user.id(), user.email(), user.role(), plan.plan(),
                profile.firstName(), profile.lastName(), profile.headline(), profile.location(),
                plan.cvAnalysisUsed(), plan.plan().cvAnalysisLimit(), plan.cvAnalysisResetAt(),
                applications.findAllByUserId(userId).size(), organizationNames, user.createdAt());
    }

    /**
     * Crée un compte directement depuis la console (candidat ou administrateur).
     * Le mot de passe est haché, le profil et le plan FREE sont initialisés comme
     * lors d'une inscription classique.
     */
    @Transactional
    public AdminUserView createUser(String email, String password, UserRole role) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.isBlank()) {
            throw new ApiExceptions.BadRequestException("L'adresse e-mail est obligatoire.");
        }
        if (users.existsByEmail(normalizedEmail)) {
            throw new ApiExceptions.ConflictException("Cette adresse e-mail est déjà utilisée.");
        }
        User user = User.create(normalizedEmail, passwordHasher.hash(password));
        if (role != null && role != UserRole.JOB_SEEKER) {
            user = user.withRole(role);
        }
        user = users.save(user);
        profiles.save(UserProfile.empty(user.id()));
        plans.save(UserPlan.create(user.id()));
        return view(user, null, Plan.FREE);
    }

    @Transactional
    public AdminUserView updatePlan(UUID userId, Plan plan) {
        User user = requireUser(userId);
        UserPlan current = plans.findById(userId).orElseGet(() -> UserPlan.create(userId));
        plans.save(current.changePlan(plan));
        UserProfile profile = profiles.findById(userId).orElseGet(() -> UserProfile.empty(userId));
        return view(user, profile, plan);
    }

    @Transactional
    public AdminUserView updateRole(UUID actorId, UUID userId, UserRole role) {
        User user = requireUser(userId);
        if (actorId.equals(userId) && role != UserRole.PLATFORM_ADMIN) {
            throw new ApiExceptions.BadRequestException(
                    "Vous ne pouvez pas retirer votre propre rôle administrateur.");
        }
        if (user.role() == UserRole.PLATFORM_ADMIN && role != UserRole.PLATFORM_ADMIN
                && users.countByRole(UserRole.PLATFORM_ADMIN) <= 1) {
            throw new ApiExceptions.ConflictException(
                    "Au moins un administrateur plateforme doit rester actif.");
        }
        User updated = users.save(user.withRole(role));
        UserProfile profile = profiles.findById(userId).orElseGet(() -> UserProfile.empty(userId));
        Plan plan = plans.findById(userId).map(UserPlan::plan).orElse(Plan.FREE);
        return view(updated, profile, plan);
    }

    @Transactional(readOnly = true)
    public Page<AdminOrganizationView> listOrganizations(String query, Pageable pageable) {
        return organizations.search(query, pageable)
                .map(organization -> new AdminOrganizationView(organization.id(), organization.name(),
                        memberships.countByOrganizationId(organization.id()), organization.createdAt()));
    }

    private User requireUser(UUID userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Utilisateur introuvable"));
    }

    private AdminUserView view(User user, UserProfile profile, Plan plan) {
        UserProfile effective = profile == null ? UserProfile.empty(user.id()) : profile;
        return new AdminUserView(user.id(), user.email(), user.role(), plan == null ? Plan.FREE : plan,
                effective.firstName(), effective.lastName(), user.createdAt());
    }

    public record OverviewView(long totalUsers, long platformAdmins, long newUsersLast30Days,
                               long totalOrganizations, long totalMemberships, long totalApplications,
                               List<PlanMetric> usersByPlan) {}

    public record PlanMetric(Plan plan, long users) {}

    public record AdminUserView(UUID id, String email, UserRole role, Plan plan,
                                String firstName, String lastName, Instant createdAt) {}

    public record AdminUserDetailView(UUID id, String email, UserRole role, Plan plan,
                                      String firstName, String lastName, String headline, String location,
                                      int cvAnalysisUsed, int cvAnalysisLimit, LocalDate cvAnalysisResetAt,
                                      long applicationCount, List<String> organizations, Instant createdAt) {}

    public record AdminOrganizationView(UUID id, String name, long members, Instant createdAt) {}
}
