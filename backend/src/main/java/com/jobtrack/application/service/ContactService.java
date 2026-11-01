package com.jobtrack.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobtrack.application.port.ContactRepository;
import com.jobtrack.domain.crm.Contact;

@Service
public class ContactService {
    private final ContactRepository contacts;

    public ContactService(ContactRepository contacts) {
        this.contacts = contacts;
    }

    @Transactional(readOnly = true)
    public List<Contact> list(UUID userId) {
        return contacts.findAllByUserId(userId);
    }

    @Transactional
    public Contact create(UUID userId, ContactCommand command) {
        return contacts.save(Contact.create(userId, command.name().trim(), command.role(),
                command.linkedinUrl(), command.email()));
    }

    @Transactional
    public Contact update(UUID userId, UUID id, ContactCommand command) {
        Contact contact = findOwned(userId, id);
        return contacts.save(contact.update(command.name().trim(), command.role(),
                command.linkedinUrl(), command.email()));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        contacts.delete(findOwned(userId, id));
    }

    private Contact findOwned(UUID userId, UUID id) {
        return contacts.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Contact introuvable"));
    }

    public record ContactCommand(String name, String role, String linkedinUrl, String email) {}
}
