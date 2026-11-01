package com.jobtrack.api.user;

import java.time.Instant;
import java.util.UUID;

import com.jobtrack.application.service.UserService;

public record UserResponse(UUID id, String email, String role, String firstName, String lastName,
                           String headline, String location, String plan, Instant createdAt) {

    public static UserResponse from(UserService.UserView view) {
        return new UserResponse(view.id(), view.email(), view.role(), view.firstName(), view.lastName(),
                view.headline(), view.location(), view.plan(), view.createdAt());
    }
}
