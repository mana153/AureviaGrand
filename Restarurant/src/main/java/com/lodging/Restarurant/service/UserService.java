package com.lodging.Restarurant.service;

import com.lodging.Restarurant.model.User;
import com.lodging.Restarurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found."));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Transactional
    public void updateProfile(Long id, String fullName, String phone) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found."));
        user.setFullName(fullName);
        user.setPhone(phone);
        userRepository.save(user);
    }
}