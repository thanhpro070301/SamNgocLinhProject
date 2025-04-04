package com.thanhpro0703.SamNgocLinhPJ.service;
import com.thanhpro0703.SamNgocLinhPJ.dto.UserDTO;
import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.reponsitory.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lấy danh sách tất cả người dùng
     */
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Lấy thông tin người dùng theo ID
     */
    public UserEntity getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy người dùng với ID: " + id));
    }

    /**
     * Cập nhật thông tin người dùng
     */
    public UserEntity updateUser(Long id, UserDTO userDetails) {
        UserEntity user = getUserById(id);
//        if (userRepository.existsByEmail(userDetails.getEmail()) && !user.getEmail().equals(userDetails.getEmail())) {
//            throw new RuntimeException("Email đã tồn tại!");
//        }
//
//        user.setEmail(userDetails.getEmail());
        // Password handling is done separately through a specific endpoint
        
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        UserEntity user = getUserById(id);
        userRepository.delete(user);
    }
}
