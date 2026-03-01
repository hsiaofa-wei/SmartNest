package com.rental.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 更新管理员密码工具
 * 运行此类的main方法，会输出更新SQL语句
 */
public class UpdateAdminPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成 admin123 的BCrypt密码哈希
        String password = "admin123";
        String encodedPassword = encoder.encode(password);
        
        System.out.println("========================================");
        System.out.println("管理员密码更新SQL");
        System.out.println("========================================");
        System.out.println("密码: " + password);
        System.out.println("BCrypt哈希: " + encodedPassword);
        System.out.println();
        System.out.println("执行以下SQL更新密码：");
        System.out.println("USE rental_db;");
        System.out.println("UPDATE sys_user SET password = '" + encodedPassword + "' WHERE username = 'admin';");
        System.out.println("========================================");
        
        // 验证密码
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("密码验证: " + (matches ? "✓ 成功" : "✗ 失败"));
    }
}

