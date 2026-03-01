package com.rental.controller;

import com.rental.dto.AuthResponse;
import com.rental.dto.CaptchaResponse;
import com.rental.dto.LoginRequest;
import com.rental.dto.RegisterRequest;
import com.rental.dto.ShapeCaptchaResponse;
import com.rental.service.AuthService;
import com.rental.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {
    @Autowired
    private AuthService authService;
    
    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/captcha")
    public ResponseEntity<?> getCaptcha() {
        try {
            return ResponseEntity.ok(captchaService.generateCaptcha());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/shape-captcha")
    public ResponseEntity<?> getShapeCaptcha() {
        try {
            return ResponseEntity.ok(captchaService.generateShapeCaptcha());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "登录失败: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪以便调试
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "注册失败: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪以便调试
            return ResponseEntity.status(500).body(error);
        }
    }
}

