package com.thanhpro0703.SamNgocLinhPJ.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/encode-password")
    public String encodePassword(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }
} 