package com.jobtrack.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jobtrack.application.port.ContactRepository;
import com.jobtrack.domain.crm.Contact;
import com.jobtrack.infrastructure.persistence.repository.SpringContactRepository;

@Component
public class ContactRepositoryAdapter implements ContactRepository {
    private final SpringContactRepository repository;

    public ContactRepositoryAdapter(SpringContactRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Contact> findAllByUserId(UUID userId) {
        return repository.findAllByUserIdOrderByNameAsc(userId).stream().map(DomainMapper::toDomain).toList();
    }

    @Override
    public Optional<Contact> findByIdAndUserId(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId).map(DomainMapper::toDomain);
    }

    @Override
    public Contact save(Contact contact) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(contact)));
    }

    @Override
    public void delete(Contact contact) {
        repository.deleteById(contact.id());
    }
}
