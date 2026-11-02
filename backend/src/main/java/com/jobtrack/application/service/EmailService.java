package com.jobtrack.application.service;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.jobtrack.domain.plan.Plan;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenders;
    private final boolean enabled;
    private final String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSenders,
                        @Value("${jobtrack.mail.enabled:false}") boolean enabled,
                        @Value("${jobtrack.mail.from:noreply@jobtrack.local}") String from) {
        this.mailSenders = mailSenders;
        this.enabled = enabled;
        this.from = from;
    }

    public void sendWelcome(String recipient) {
        send(recipient, "Bienvenue sur JobTrack",
                "Votre compte JobTrack a bien été créé.\n\n"
                        + "Vous pouvez maintenant compléter votre profil, suivre vos candidatures "
                        + "et générer votre CV.");
    }

    public void sendSubscriptionConfirmation(String recipient, Plan plan) {
        send(recipient, "Votre abonnement JobTrack est confirmé",
                "Votre abonnement " + plan.name() + " est maintenant actif.\n\n"
                        + "Merci pour votre confiance.");
    }

    private void send(String recipient, String subject, String body) {
        if (!enabled) {
            log.debug("E-mails désactivés, notification ignorée pour {}", recipient);
            return;
        }
        JavaMailSender sender = mailSenders.getIfAvailable();
        if (sender == null) {
            log.warn("E-mails activés mais aucun expéditeur SMTP n'est configuré pour {}", recipient);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(Objects.requireNonNull(recipient));
            message.setSubject(subject);
            message.setText(body);
            sender.send(message);
        } catch (RuntimeException exception) {
            log.error("Impossible d'envoyer l'e-mail '{}' à {}", subject, recipient, exception);
        }
    }
}
