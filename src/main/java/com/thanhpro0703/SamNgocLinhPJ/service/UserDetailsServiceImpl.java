package com.thanhpro0703.SamNgocLinhPJ.service;

import com.thanhpro0703.SamNgocLinhPJ.entity.UserEntity;
import com.thanhpro0703.SamNgocLinhPJ.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Primary
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> 
                    new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        // Chuyển đổi Role thành GrantedAuthority
        String roleName = "ROLE_" + user.getRole().name();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);
        
        logger.info("Loading user: {}, Role: {}, Authority: {}, Status: {}", 
                email, user.getRole().name(), roleName, user.getStatus());

        // Kiểm tra trạng thái user có đang ACTIVE không
        boolean isActive = user.getStatus() == UserEntity.Status.ACTIVE;

        if (!isActive) {
            logger.warn("User {} is not active! Authentication will fail.", email);
        }

        return new User(
                user.getEmail(),
                user.getPassword(),
                isActive, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singletonList(authority));
    }
} 