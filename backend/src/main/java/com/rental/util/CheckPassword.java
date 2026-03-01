package com.rental.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码检查和生成工具
 */
public class CheckPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 数据库中的密码哈希
        String dbPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8p8qO";
        
        // 测试常见密码
        String[] testPasswords = {"admin123", "admin", "123456", "password", "admin123456"};
        
        System.out.println("测试数据库中的密码哈希值对应的明文密码：");
        for (String testPwd : testPasswords) {
            if (encoder.matches(testPwd, dbPassword)) {
                System.out.println("✓ 匹配成功！明文密码是: " + testPwd);
            }
        }
        
        System.out.println("\n生成新的 admin123 密码哈希：");
        String newPassword = encoder.encode("admin123");
        System.out.println("新密码哈希: " + newPassword);
        
        System.out.println("\n更新SQL语句：");
        System.out.println("UPDATE sys_user SET password = '" + newPassword + "' WHERE username = 'admin';");
    }
}

