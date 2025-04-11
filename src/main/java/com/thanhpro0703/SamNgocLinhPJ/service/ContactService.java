package com.thanhpro0703.SamNgocLinhPJ.service;

import com.thanhpro0703.SamNgocLinhPJ.entity.ContactEntity;
import com.thanhpro0703.SamNgocLinhPJ.repository.ContactRepository;
import com.thanhpro0703.SamNgocLinhPJ.utils.PhoneValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {
    private final ContactRepository contactRepository;


    public List<ContactEntity> getAllContacts() {
        log.info("Lấy danh sách tất cả liên hệ");
        return contactRepository.findAll();
    }

    public ContactEntity getContactById(Integer id) {
        log.info("Tìm liên hệ với ID: {}", id);
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Không tìm thấy liên hệ với ID: " + id));
    }



    public ContactEntity createContact(ContactEntity contact) {
        log.info("Tạo liên hệ mới với tên: {}", contact.getName());

        // Sử dụng PhoneValidator để kiểm tra số điện thoại
        if (!PhoneValidator.isValidPhone(contact.getPhone())) {
            log.warn("Số điện thoại '{}' không hợp lệ!", contact.getPhone());
            throw new IllegalArgumentException("Số điện thoại không hợp lệ!");
        }

        return contactRepository.save(contact);
    }


    public ContactEntity updateContact(Integer id, ContactEntity contactDetails) {
        log.info("Cập nhật liên hệ với ID: {}", id);
        ContactEntity contact = getContactById(id);

        contact.setName(contactDetails.getName());

        // Kiểm tra số điện thoại hợp lệ trước khi cập nhật
        if (!PhoneValidator.isValidPhone(contactDetails.getPhone())) {
            contact.setPhone(contactDetails.getPhone());
        } else {
            log.warn("Số điện thoại '{}' không hợp lệ!", contactDetails.getPhone());
            throw new IllegalArgumentException("Số điện thoại không hợp lệ!");
        }

        contact.setMessage(contactDetails.getMessage());

        return contactRepository.save(contact);
    }

    public void deleteContact(Integer id) {
        log.info("Xóa liên hệ với ID: {}", id);
        ContactEntity contact = getContactById(id);
        contactRepository.delete(contact);
        log.info("Đã xóa liên hệ với ID: {}", id);
    }


}
