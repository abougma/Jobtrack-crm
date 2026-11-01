package com.jobtrack.api.user;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.jobtrack.application.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public UserResponse get(@AuthenticationPrincipal String userId) {
        return UserResponse.from(service.get(UUID.fromString(userId)));
    }

    @PutMapping("/profile")
    public UserResponse update(@AuthenticationPrincipal String userId,
                               @Valid @RequestBody ProfileRequest request) {
        return UserResponse.from(service.updateProfile(UUID.fromString(userId),
                new UserService.ProfileUpdate(request.firstName(), request.lastName(),
                        request.headline(), request.location())));
    }

    public record ProfileRequest(
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName,
            @Size(max = 255) String headline,
            @Size(max = 255) String location) {}
}
