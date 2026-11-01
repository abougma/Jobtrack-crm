package com.jobtrack.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtrack.domain.crm.Contact;

public interface ContactRepository {
    List<Contact> findAllByUserId(UUID userId);
    Optional<Contact> findByIdAndUserId(UUID id, UUID userId);
    Contact save(Contact contact);
    void delete(Contact contact);
}
