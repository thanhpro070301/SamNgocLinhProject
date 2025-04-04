package com.thanhpro0703.SamNgocLinhPJ.controller;

import com.thanhpro0703.SamNgocLinhPJ.dto.ContactDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.ContactEntity;
import com.thanhpro0703.SamNgocLinhPJ.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService contactService;

    // Lấy tất cả danh bạ
    @GetMapping
    public ResponseEntity<List<ContactEntity>> getAllContacts() {
        List<ContactEntity> contacts = contactService.getAllContacts();
        return ResponseEntity.ok(contacts);
    }

    // Lấy danh bạ theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ContactEntity> getContactById(@PathVariable Integer id) {
        ContactEntity contact = contactService.getContactById(id);
        return ResponseEntity.ok(contact);
    }

    // Tạo danh bạ mới
    @PostMapping
    public ResponseEntity<ContactEntity> createContact(@Valid @RequestBody ContactDTO contactDTO) {
        ContactEntity createdContact = contactService.createContact(contactDTO.toEntity());
        return ResponseEntity.created(URI.create("/api/contacts/" + createdContact.getId()))
                .body(createdContact);
    }

    // Cập nhật danh bạ
    @PutMapping("/{id}")
    public ResponseEntity<ContactEntity> updateContact(
            @PathVariable Integer id,
            @Valid @RequestBody ContactDTO contactDTO) {
        ContactEntity updatedContact = contactService.updateContact(id, contactDTO.toEntity());
        return ResponseEntity.ok(updatedContact);
    }

    // Xóa danh bạ
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Integer id) {
        contactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }
}
