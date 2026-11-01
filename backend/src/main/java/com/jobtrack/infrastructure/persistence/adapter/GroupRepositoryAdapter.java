package com.jobtrack.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jobtrack.application.port.GroupRepository;
import com.jobtrack.domain.organization.OrganizationGroup;
import com.jobtrack.infrastructure.persistence.entity.OrganizationGroupMemberJpaEntity;
import com.jobtrack.infrastructure.persistence.repository.SpringGroupMemberRepository;
import com.jobtrack.infrastructure.persistence.repository.SpringGroupRepository;

@Component
public class GroupRepositoryAdapter implements GroupRepository {
    private final SpringGroupRepository groups;
    private final SpringGroupMemberRepository members;

    public GroupRepositoryAdapter(SpringGroupRepository groups, SpringGroupMemberRepository members) {
        this.groups = groups;
        this.members = members;
    }

    @Override
    public OrganizationGroup save(OrganizationGroup group) {
        return DomainMapper.toDomain(groups.save(DomainMapper.toEntity(group)));
    }

    @Override
    public List<OrganizationGroup> findAllByOrganizationId(UUID organizationId) {
        return groups.findAllByOrganizationIdOrderByNameAsc(organizationId).stream()
                .map(DomainMapper::toDomain).toList();
    }

    @Override
    public Optional<OrganizationGroup> findByIdAndOrganizationId(UUID id, UUID organizationId) {
        return groups.findByIdAndOrganizationId(id, organizationId).map(DomainMapper::toDomain);
    }

    @Override
    public List<UUID> findMemberIds(UUID groupId) {
        return members.findAllByGroupId(groupId).stream()
                .map(OrganizationGroupMemberJpaEntity::getUserId).toList();
    }

    @Override
    public void addMember(UUID groupId, UUID userId) {
        members.save(new OrganizationGroupMemberJpaEntity(groupId, userId));
    }

    @Override
    public void removeMember(UUID groupId, UUID userId) {
        members.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public long countMembers(UUID groupId) {
        return members.countByGroupId(groupId);
    }
}
