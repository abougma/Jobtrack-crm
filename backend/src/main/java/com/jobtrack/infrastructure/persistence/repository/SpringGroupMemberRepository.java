package com.jobtrack.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobtrack.infrastructure.persistence.entity.OrganizationGroupMemberJpaEntity;

public interface SpringGroupMemberRepository
        extends JpaRepository<OrganizationGroupMemberJpaEntity, OrganizationGroupMemberJpaEntity.Key> {
    List<OrganizationGroupMemberJpaEntity> findAllByGroupId(UUID groupId);
    List<OrganizationGroupMemberJpaEntity> findAllByUserIdIn(Collection<UUID> userIds);
    long countByGroupId(UUID groupId);
    void deleteByGroupIdAndUserId(UUID groupId, UUID userId);
}
