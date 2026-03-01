package com.rental.controller;

import com.rental.entity.SysFile;
import com.rental.service.FileService;
import com.rental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
@CrossOrigin
public class FileController {
    @Autowired
    private FileService fileService;
    
    @Autowired
    private UserService userService;
    
    @Value("${file.upload.path}")
    private String uploadPath;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD', 'TENANT')")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long userId) {
        try {
            SysFile sysFile = fileService.uploadFile(file, userId);
            
            // 如果传入 userId，则自动更新用户头像
            if (userId != null) {
                userService.getUserById(userId).ifPresent(user -> {
                    user.setAvatarUrl(sysFile.getFileUrl());
                    userService.updateUser(user);
                });
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", sysFile.getId());
            response.put("url", sysFile.getFileUrl());
            response.put("fileName", sysFile.getFileName());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LANDLORD', 'TENANT')")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.ok().build();
    }
    
    // 直接处理文件请求，绕过Spring Boot的资源映射机制
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        try {
            // 构建完整的文件路径
            Path filePath = Paths.get(uploadPath, fileName);
            File file = filePath.toFile();
            
            // 检查文件是否存在
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取文件的媒体类型
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            
            // 构建响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(file.length());
            // 移除attachment设置，让浏览器直接显示图片
            headers.setContentDispositionFormData("inline", fileName);
            
            // 构建并返回响应
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

