package com.rental.config;

import com.rental.entity.SysUser;
import com.rental.repository.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 管理员账号初始化
 * 应用启动时自动创建默认管理员账号（如果不存在）
 */
@Component
public class AdminInitializer implements CommandLineRunner {
    
    @Autowired
    private SysUserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查管理员账号是否存在
        if (!userRepository.existsByUsername("admin")) {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setPhone("13800138000");
            admin.setRole(SysUser.UserRole.ADMIN);
            admin.setStatus(SysUser.UserStatus.ACTIVE);
            userRepository.save(admin);
            System.out.println("========================================");
            System.out.println("默认管理员账号已创建");
            System.out.println("用户名: admin");
            System.out.println("密码: admin123");
            System.out.println("========================================");
        }
    }
}

