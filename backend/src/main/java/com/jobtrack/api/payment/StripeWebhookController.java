package com.jobtrack.api.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jobtrack.application.service.PaymentService;

@RestController
@RequestMapping("/api/v1/payments")
public class StripeWebhookController {
    private final PaymentService payments;

    public StripeWebhookController(PaymentService payments) {
        this.payments = payments;
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader("Stripe-Signature") String signature) {
        payments.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
