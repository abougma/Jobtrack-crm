package com.jobtrack.application.service;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.domain.plan.Plan;
import com.jobtrack.domain.plan.UserPlan;
import com.jobtrack.domain.user.User;
import com.jobtrack.application.port.UserPlanRepository;
import com.jobtrack.application.port.UserRepository;
import com.jobtrack.infrastructure.persistence.entity.PaymentJpaEntity;
import com.jobtrack.infrastructure.persistence.repository.SpringPaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.net.Webhook;

@Service
public class PaymentService {
    private final SpringPaymentRepository payments;
    private final UserRepository users;
    private final UserPlanRepository plans;
    private final String secretKey;
    private final String webhookSecret;
    private final String frontendUrl;
    private final EmailService emailService;

    public PaymentService(SpringPaymentRepository payments, UserRepository users, UserPlanRepository plans,
                          @Value("${jobtrack.stripe.secret-key:}") String secretKey,
                          @Value("${jobtrack.stripe.webhook-secret:}") String webhookSecret,
                          @Value("${jobtrack.security.cors-origin:http://localhost:5173}") String frontendUrl,
                          EmailService emailService) {
        this.payments = payments;
        this.users = users;
        this.plans = plans;
        this.secretKey = secretKey;
        this.webhookSecret = webhookSecret;
        this.frontendUrl = frontendUrl;
        this.emailService = emailService;
    }

    public CheckoutView createCheckout(UUID userId, Plan plan) {
        if (plan == null || plan == Plan.FREE || plan == Plan.ORGANIZATION) {
            throw new ApiExceptions.BadRequestException("Ce plan ne nécessite pas de paiement en ligne.");
        }
        if (secretKey.isBlank() || !(secretKey.startsWith("sk_test_") || secretKey.startsWith("sk_live_"))) {
            throw new ApiExceptions.ConflictException(
                    "La clé secrète Stripe est absente ou invalide. Utilisez une clé sk_test_ ou sk_live_.");
        }
        User user = users.findById(userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Utilisateur introuvable"));
        long amount = plan == Plan.PRO ? 999 : 1999;
        try {
            Stripe.apiKey = secretKey;
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(frontendUrl + "/profile?payment=success")
                    .setCancelUrl(frontendUrl + "/profile?payment=cancelled")
                    .setCustomerEmail(user.email())
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("eur")
                                    .setUnitAmount(amount)
                                    .setRecurring(SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                            .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH).build())
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("JobTrack " + plan.name()).build())
                                    .build())
                            .build())
                    .putMetadata("userId", userId.toString())
                    .putMetadata("plan", plan.name())
                    .build();
            Session session = Session.create(params);
            payments.save(new PaymentJpaEntity(UUID.randomUUID(), userId, plan, amount, "EUR",
                    session.getId(), "PENDING", Instant.now(), null));
            return new CheckoutView(session.getUrl());
        } catch (StripeException exception) {
            throw new ApiExceptions.ConflictException("Impossible de créer le paiement Stripe.");
        }
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        if (webhookSecret.isBlank()) {
            throw new ApiExceptions.ConflictException("Le webhook Stripe n'est pas configuré.");
        }
        try {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            if (!"checkout.session.completed".equals(event.getType())) return;
            String sessionId = new ObjectMapper()
                    .readTree(event.getDataObjectDeserializer().getRawJson()).get("id").asText();
            Stripe.apiKey = secretKey;
            Session session = Session.retrieve(sessionId);
            String userId = session.getMetadata().get("userId");
            String planName = session.getMetadata().get("plan");
            if (userId == null || planName == null) return;
            Plan plan = Plan.valueOf(planName);
            UUID id = UUID.fromString(userId);
            UserPlan current = plans.findById(id).orElseGet(() -> UserPlan.create(id));
            plans.save(current.changePlan(plan));
            payments.findByStripeSessionId(sessionId).ifPresent(payment -> {
                boolean newlyPaid = !"PAID".equals(payment.getStatus());
                payments.save(new PaymentJpaEntity(payment.getId(), payment.getUserId(),
                            payment.getPlan(), payment.getAmountCents(), payment.getCurrency(),
                            payment.getStripeSessionId(), "PAID", payment.getCreatedAt(), Instant.now()));
                if (newlyPaid) {
                    users.findById(id).ifPresent(user -> emailService.sendSubscriptionConfirmation(user.email(), plan));
                }
            });
        } catch (Exception exception) {
            throw new ApiExceptions.BadRequestException("Webhook Stripe invalide.");
        }
    }

    @Transactional(readOnly = true)
    public RevenueView revenue() {
        return new RevenueView(payments.sumPaidAmountCents(), payments.countByStatus("PAID"),
                payments.countByStatus("PENDING"));
    }

    public record CheckoutView(String checkoutUrl) {}
    public record RevenueView(long paidAmountCents, long paidTransactions, long pendingTransactions) {}
}
