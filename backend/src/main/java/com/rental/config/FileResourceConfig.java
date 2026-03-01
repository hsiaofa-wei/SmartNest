package com.rental.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileResourceConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(FileResourceConfig.class);

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        logger.debug("文件上传路径: {}", uploadPath);
        
        // 处理Windows路径，确保格式正确
        String resourceLocation = uploadPath;
        // 将反斜杠转换为正斜杠
        resourceLocation = resourceLocation.replace("\\", "/");
        // 如果是绝对路径，添加file:///前缀
        if (resourceLocation.startsWith("C:/")) {
            resourceLocation = "file:///" + resourceLocation;
        } else if (!resourceLocation.startsWith("file://")) {
            resourceLocation = "file:" + resourceLocation;
        }
        
        logger.debug("转换后的资源位置: {}", resourceLocation);
        
        registry.addResourceHandler("/files/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(0); // 禁用缓存以便调试
    }
}

