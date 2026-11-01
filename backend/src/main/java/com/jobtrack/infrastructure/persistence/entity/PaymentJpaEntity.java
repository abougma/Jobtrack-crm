package com.jobtrack.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.jobtrack.domain.plan.Plan;
import jakarta.persistence.*;

@Entity
@Table(name = "payments")
public class PaymentJpaEntity {
    @Id
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Plan plan;
    @Column(name = "amount_cents", nullable = false)
    private long amountCents;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "stripe_session_id", nullable = false, unique = true)
    private String stripeSessionId;
    @Column(nullable = false, length = 32)
    private String status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "paid_at")
    private Instant paidAt;

    protected PaymentJpaEntity() {}

    public PaymentJpaEntity(UUID id, UUID userId, Plan plan, long amountCents, String currency,
                            String stripeSessionId, String status, Instant createdAt, Instant paidAt) {
        this.id = id;
        this.userId = userId;
        this.plan = plan;
        this.amountCents = amountCents;
        this.currency = currency;
        this.stripeSessionId = stripeSessionId;
        this.status = status;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Plan getPlan() { return plan; }
    public long getAmountCents() { return amountCents; }
    public String getCurrency() { return currency; }
    public String getStripeSessionId() { return stripeSessionId; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPaidAt() { return paidAt; }
}
