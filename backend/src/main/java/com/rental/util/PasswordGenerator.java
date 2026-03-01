package com.rental.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类
 * 用于生成BCrypt加密的密码
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成管理员密码
        String adminPassword = encoder.encode("admin123");
        System.out.println("管理员密码 (admin123): " + adminPassword);
        
        // 生成其他测试密码
        String landlordPassword = encoder.encode("landlord123");
        System.out.println("房东密码 (landlord123): " + landlordPassword);
        
        String tenantPassword = encoder.encode("tenant123");
        System.out.println("租客密码 (tenant123): " + tenantPassword);
    }
}

