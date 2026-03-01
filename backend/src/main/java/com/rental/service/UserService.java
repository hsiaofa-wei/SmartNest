package com.rental.service;

import com.rental.entity.SysUser;
import com.rental.repository.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private SysUserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<SysUser> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<SysUser> getUsersByRole(SysUser.UserRole role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    public Optional<SysUser> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public SysUser updateUser(SysUser user) {
        return userRepository.save(user);
    }

    @Transactional
    public void updateUserStatus(Long userId, SysUser.UserStatus status) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus(status);
        userRepository.save(user);
    }
    
    @Transactional
    public void updateUserRole(Long userId, SysUser.UserRole role) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
    
    public boolean verifyPassword(SysUser user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}

