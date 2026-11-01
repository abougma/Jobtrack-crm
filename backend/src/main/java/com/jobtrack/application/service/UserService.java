package com.jobtrack.application.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.UserPlanRepository;
import com.jobtrack.application.port.UserProfileRepository;
import com.jobtrack.application.port.UserRepository;
import com.jobtrack.domain.plan.Plan;
import com.jobtrack.domain.plan.UserPlan;
import com.jobtrack.domain.user.User;
import com.jobtrack.domain.user.UserProfile;
import com.jobtrack.domain.user.UserRole;

@Service
public class UserService {
    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final UserPlanRepository plans;

    public UserService(UserRepository users, UserProfileRepository profiles, UserPlanRepository plans) {
        this.users = users;
        this.profiles = profiles;
        this.plans = plans;
    }

    @Transactional(readOnly = true)
    public UserView get(UUID userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Utilisateur introuvable"));
        return view(user);
    }

    @Transactional
    public UserView updateProfile(UUID userId, ProfileUpdate command) {
        User user = users.findById(userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Utilisateur introuvable"));
        UserProfile profile = profiles.findById(userId).orElseGet(() -> UserProfile.empty(userId));
        profiles.save(profile.update(command.firstName(), command.lastName(),
                command.headline(), command.location()));
        return view(user);
    }

    @Transactional(readOnly = true)
    public UserView view(User user) {
        UserProfile profile = profiles.findById(user.id()).orElseGet(() -> UserProfile.empty(user.id()));
        Plan plan = plans.findById(user.id()).map(UserPlan::plan).orElse(Plan.FREE);
        return new UserView(user.id(), user.email(), user.role().name(),
                profile.firstName(), profile.lastName(), profile.headline(), profile.location(),
                plan.name(), user.createdAt());
    }

    public record ProfileUpdate(String firstName, String lastName, String headline, String location) {}

    public record UserView(UUID id, String email, String role, String firstName, String lastName,
                           String headline, String location, String plan, Instant createdAt) {}
}
