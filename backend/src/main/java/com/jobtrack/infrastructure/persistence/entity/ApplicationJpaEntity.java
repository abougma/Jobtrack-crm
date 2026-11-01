package com.jobtrack.infrastructure.persistence.entity;

import java.time.*;
import java.util.UUID;

import com.jobtrack.domain.application.*;
import jakarta.persistence.*;

@Entity
@Table(name = "applications")
public class ApplicationJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "contact_id") private UUID contactId;
    @Column(nullable = false) private String company;
    @Column(nullable = false) private String position;
    private String location;
    @Enumerated(EnumType.STRING) @Column(name = "work_mode", length = 32) private WorkMode workMode;
    @Enumerated(EnumType.STRING) @Column(name = "contract_type", length = 32) private ContractType contractType;
    @Column(name = "salary_min") private Integer salaryMin;
    @Column(name = "salary_max") private Integer salaryMax;
    private String source;
    @Column(name = "offer_url") private String offerUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private ApplicationStatus status;
    @Column(name = "applied_at") private LocalDate appliedAt;
    @Column(columnDefinition = "TEXT") private String notes;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected ApplicationJpaEntity() {}

    public ApplicationJpaEntity(UUID id, UUID userId, UUID contactId, String company, String position,
                                String location, WorkMode workMode, ContractType contractType,
                                Integer salaryMin, Integer salaryMax, String source, String offerUrl,
                                ApplicationStatus status, LocalDate appliedAt, String notes,
                                Instant createdAt, Instant updatedAt) {
        this.id = id; this.userId = userId; this.contactId = contactId; this.company = company;
        this.position = position; this.location = location; this.workMode = workMode;
        this.contractType = contractType; this.salaryMin = salaryMin; this.salaryMax = salaryMax;
        this.source = source; this.offerUrl = offerUrl; this.status = status; this.appliedAt = appliedAt;
        this.notes = notes; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getContactId() { return contactId; }
    public String getCompany() { return company; }
    public String getPosition() { return position; }
    public String getLocation() { return location; }
    public WorkMode getWorkMode() { return workMode; }
    public ContractType getContractType() { return contractType; }
    public Integer getSalaryMin() { return salaryMin; }
    public Integer getSalaryMax() { return salaryMax; }
    public String getSource() { return source; }
    public String getOfferUrl() { return offerUrl; }
    public ApplicationStatus getStatus() { return status; }
    public LocalDate getAppliedAt() { return appliedAt; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
