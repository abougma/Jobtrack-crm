package com.jobtrack.api.contact;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.jobtrack.application.service.ContactService;
import com.jobtrack.domain.crm.Contact;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {
    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    @GetMapping
    public List<ContactResponse> list(@AuthenticationPrincipal String userId) {
        return service.list(UUID.fromString(userId)).stream().map(ContactResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse create(@AuthenticationPrincipal String userId,
                                  @Valid @RequestBody ContactRequest request) {
        return ContactResponse.from(service.create(UUID.fromString(userId), request.toCommand()));
    }

    @PutMapping("/{id}")
    public ContactResponse update(@AuthenticationPrincipal String userId, @PathVariable UUID id,
                                  @Valid @RequestBody ContactRequest request) {
        return ContactResponse.from(service.update(UUID.fromString(userId), id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        service.delete(UUID.fromString(userId), id);
    }

    public record ContactRequest(
            @NotBlank @Size(max = 255) String name,
            @Size(max = 255) String role,
            @Size(max = 500) String linkedinUrl,
            @Email @Size(max = 320) String email) {
        ContactService.ContactCommand toCommand() {
            return new ContactService.ContactCommand(name, role, linkedinUrl, email);
        }
    }

    public record ContactResponse(UUID id, String name, String role, String linkedinUrl, String email,
                                  Instant createdAt) {
        static ContactResponse from(Contact contact) {
            return new ContactResponse(contact.id(), contact.name(), contact.role(), contact.linkedinUrl(),
                    contact.email(), contact.createdAt());
        }
    }
}
