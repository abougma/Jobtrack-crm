package com.jobtrack.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "organization_group_members")
@IdClass(OrganizationGroupMemberJpaEntity.Key.class)
public class OrganizationGroupMemberJpaEntity {
    @Id @Column(name = "group_id") private UUID groupId;
    @Id @Column(name = "user_id") private UUID userId;

    protected OrganizationGroupMemberJpaEntity() {}

    public OrganizationGroupMemberJpaEntity(UUID groupId, UUID userId) {
        this.groupId = groupId; this.userId = userId;
    }

    public UUID getGroupId() { return groupId; }
    public UUID getUserId() { return userId; }

    public static class Key implements Serializable {
        private UUID groupId;
        private UUID userId;
        public Key() {}
        public Key(UUID groupId, UUID userId) { this.groupId = groupId; this.userId = userId; }
        @Override public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Key key)) return false;
            return Objects.equals(groupId, key.groupId) && Objects.equals(userId, key.userId);
        }
        @Override public int hashCode() { return Objects.hash(groupId, userId); }
    }
}
