package com.rental.service;

import com.rental.dto.AuthResponse;
import com.rental.dto.LoginRequest;
import com.rental.dto.RegisterRequest;
import com.rental.entity.SysUser;
import com.rental.repository.SysUserRepository;
import com.rental.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    @Autowired
    private SysUserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CaptchaService captchaService;

    public AuthResponse login(LoginRequest request) {
        try {
            // 验证验证码
            if (request.getCaptchaKey() == null || request.getCaptcha() == null) {
                throw new RuntimeException("验证码不能为空");
            }
            
            if (!captchaService.validateCaptcha(request.getCaptchaKey(), request.getCaptcha())) {
                throw new RuntimeException("验证码错误或已过期");
            }

            SysUser user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

            if (user.getStatus() == SysUser.UserStatus.DISABLED) {
                throw new RuntimeException("账号已被禁用");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("用户名或密码错误");
            }

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());
            
            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setUsername(user.getUsername());
            response.setRole(user.getRole().name());
            response.setUserId(user.getId());
            response.setAvatarUrl(user.getAvatarUrl());
            return response;
        } catch (RuntimeException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            // 包装其他异常
            throw new RuntimeException("登录失败: " + e.getMessage(), e);
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        try {
            // 验证验证码
            if (request.getCaptchaKey() == null || request.getCaptcha() == null) {
                throw new RuntimeException("验证码不能为空");
            }
            
            if (!captchaService.validateCaptcha(request.getCaptchaKey(), request.getCaptcha())) {
                throw new RuntimeException("验证码错误或已过期");
            }

            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }

            if (userRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("手机号已被注册");
            }

            SysUser user = new SysUser();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhone(request.getPhone());
            user.setRealName(request.getRealName());
            user.setRole(SysUser.UserRole.valueOf(request.getRole().toUpperCase()));
            user.setStatus(SysUser.UserStatus.ACTIVE);
            
            user = userRepository.save(user);

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());
            
            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setUsername(user.getUsername());
            response.setRole(user.getRole().name());
            response.setUserId(user.getId());
            return response;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("角色类型错误: " + request.getRole(), e);
        } catch (RuntimeException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            // 包装其他异常
            throw new RuntimeException("注册失败: " + e.getMessage(), e);
        }
    }
}

