package com.rental.controller;

import com.rental.dto.UserUpdateRequest;
import com.rental.entity.SysUser;
import com.rental.service.UserService;
import com.rental.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin
@PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD', 'TENANT')")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/profile")
    public ResponseEntity<SysUser> getProfile(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getClaimsFromToken(token).get("userId", Long.class);
        SysUser user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        // 不返回密码
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<SysUser> updateProfile(
            @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getClaimsFromToken(token).get("userId", Long.class);
        
        SysUser user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getIdCard() != null) {
            user.setIdCard(request.getIdCard());
        }
        
        return ResponseEntity.ok(userService.updateUser(user));
    }
    
    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getClaimsFromToken(token).get("userId", Long.class);
        
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        SysUser user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证旧密码
        if (!userService.verifyPassword(user, oldPassword)) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "原密码错误");
            return ResponseEntity.status(400).body(error);
        }
        
        // 更新密码
        userService.resetPassword(userId, newPassword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "密码修改成功");
        return ResponseEntity.ok(response);
    }
}

