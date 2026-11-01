package com.jobtrack.infrastructure.persistence.repository;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jobtrack.infrastructure.persistence.entity.PaymentJpaEntity;

public interface SpringPaymentRepository extends JpaRepository<PaymentJpaEntity, UUID> {
    boolean existsByStripeSessionId(String stripeSessionId);
    Optional<PaymentJpaEntity> findByStripeSessionId(String stripeSessionId);
    @Query("select coalesce(sum(p.amountCents), 0) from PaymentJpaEntity p where p.status = 'PAID'")
    long sumPaidAmountCents();
    long countByStatus(String status);
}
