package com.jobtrack.application.port;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jobtrack.domain.plan.Plan;
import com.jobtrack.domain.user.User;
import com.jobtrack.domain.user.UserRole;

public interface UserRepository {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsById(UUID id);
    Page<User> search(String query, UserRole role, Plan plan, Pageable pageable);
    long count();
    long countByRole(UserRole role);
    long countByCreatedAtAfter(Instant instant);
    User save(User user);
    void deleteById(UUID id);
}
