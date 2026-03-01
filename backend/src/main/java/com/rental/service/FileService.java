package com.rental.service;

import com.rental.entity.SysFile;
import com.rental.repository.SysFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {
    @Autowired
    private SysFileRepository fileRepository;
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    @Value("${file.upload.url}")
    private String uploadUrl;

    @Transactional
    public SysFile uploadFile(MultipartFile file, Long userId) throws IOException {
        // 创建上传目录
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;
        
        // 保存文件
        Path filePath = Paths.get(uploadPath, fileName);
        Files.write(filePath, file.getBytes());
        
        // 保存文件记录
        SysFile sysFile = new SysFile();
        sysFile.setFileName(fileName);
        sysFile.setOriginalName(originalFilename);
        sysFile.setFilePath(filePath.toString());
        sysFile.setFileUrl(uploadUrl + fileName);
        sysFile.setFileSize(file.getSize());
        sysFile.setFileType(file.getContentType());
        sysFile.setUploadUserId(userId);
        
        return fileRepository.save(sysFile);
    }
    
    public void deleteFile(Long fileId) {
        SysFile sysFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        
        // 删除物理文件
        try {
            Files.deleteIfExists(Paths.get(sysFile.getFilePath()));
        } catch (IOException e) {
            // 忽略删除失败
        }
        
        // 删除数据库记录
        fileRepository.delete(sysFile);
    }
}

