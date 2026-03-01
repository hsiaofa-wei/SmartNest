package com.rental.service;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.rental.dto.CaptchaResponse;
import com.rental.dto.ShapeCaptchaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaService {
    private final DefaultKaptcha kaptcha;
    
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
    // 内存存储（当Redis不可用时使用）
    private final Map<String, CaptchaCache> memoryCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean useRedis = false;
    
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    
    // 几何体验证码相关配置
    private static final String[] SHAPE_TYPES = {"circle", "square", "triangle", "star"};
    private static final String[] COLORS = {"#FF5733", "#33FF57", "#3357FF", "#FF33A8", "#FFC300", "#8E44AD"};
    private static final int MIN_SIZE = 20;
    private static final int MAX_SIZE = 40;
    private static final int MIN_SHAPES = 5;
    private static final int MAX_SHAPES = 8;
    
    // 验证码缓存类
    private static class CaptchaCache {
        String code; // 文本验证码或几何体验证码的目标形状
        long expireTime;
        String type; // text 或 shape
        
        CaptchaCache(String code, long expireTime, String type) {
            this.code = code;
            this.expireTime = expireTime;
            this.type = type;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    public CaptchaService() {
        this.kaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", "yes");
        properties.setProperty("kaptcha.border.color", "105,179,90");
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        properties.setProperty("kaptcha.image.width", "120");
        properties.setProperty("kaptcha.image.height", "40");
        properties.setProperty("kaptcha.textproducer.font.size", "30");
        properties.setProperty("kaptcha.session.key", "code");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        properties.setProperty("kaptcha.noise.color", "white");
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.DefaultNoise");
        Config config = new Config(properties);
        kaptcha.setConfig(config);
    }
    
    @PostConstruct
    public void init() {
        // 检测Redis是否可用
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set("test", "test", 1, TimeUnit.SECONDS);
                useRedis = true;
                System.out.println("验证码服务: 使用Redis存储");
            } catch (Exception e) {
                useRedis = false;
                System.out.println("验证码服务: Redis不可用，使用内存存储（仅用于开发测试）");
            }
        } else {
            useRedis = false;
            System.out.println("验证码服务: Redis未配置，使用内存存储（仅用于开发测试）");
        }
        
        // 启动定时清理过期验证码
        scheduler.scheduleAtFixedRate(this::cleanExpiredCaptcha, 1, 1, TimeUnit.MINUTES);
    }
    
    private void cleanExpiredCaptcha() {
        if (!useRedis) {
            memoryCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
    }

    public CaptchaResponse generateCaptcha() {
        try {
            String key = UUID.randomUUID().toString();
            String code = kaptcha.createText();
            
            // 存储验证码
            if (useRedis && redisTemplate != null) {
                try {
                    redisTemplate.opsForValue().set(CAPTCHA_PREFIX + key, "text:" + code, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
                } catch (Exception e) {
                    // Redis失败，降级到内存存储
                    useRedis = false;
                    long expireTime = System.currentTimeMillis() + CAPTCHA_EXPIRE_MINUTES * 60 * 1000;
                    memoryCache.put(key, new CaptchaCache(code, expireTime, "text"));
                }
            } else {
                // 使用内存存储
                long expireTime = System.currentTimeMillis() + CAPTCHA_EXPIRE_MINUTES * 60 * 1000;
                memoryCache.put(key, new CaptchaCache(code, expireTime, "text"));
            }
            
            // 生成图片
            BufferedImage image = kaptcha.createImage(code);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", baos);
                String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                CaptchaResponse response = new CaptchaResponse();
                response.setKey(key);
                response.setImage("data:image/png;base64," + base64Image);
                return response;
            } catch (IOException e) {
                throw new RuntimeException("生成验证码图片失败", e);
            }
        } catch (Exception e) {
            throw new RuntimeException("生成验证码失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成几何体验证码
     */
    public ShapeCaptchaResponse generateShapeCaptcha() {
        try {
            String key = UUID.randomUUID().toString();
            Random random = new Random();
            int canvasWidth = 300;
            int canvasHeight = 200;
            
            // 随机选择目标形状
            String targetShape = SHAPE_TYPES[random.nextInt(SHAPE_TYPES.length)];
            
            // 生成形状
            int shapeCount = MIN_SHAPES + random.nextInt(MAX_SHAPES - MIN_SHAPES + 1);
            List<ShapeCaptchaResponse.Shape> shapes = new ArrayList<>();
            
            // 确保包含目标形状
            boolean hasTargetShape = false;
            
            for (int i = 0; i < shapeCount; i++) {
                ShapeCaptchaResponse.Shape shape = new ShapeCaptchaResponse.Shape();
                
                // 随机形状类型
                String shapeType;
                if (!hasTargetShape || random.nextBoolean()) {
                    shapeType = SHAPE_TYPES[random.nextInt(SHAPE_TYPES.length)];
                    if (shapeType.equals(targetShape)) {
                        hasTargetShape = true;
                    }
                } else {
                    // 确保至少有一个目标形状
                    shapeType = targetShape;
                    hasTargetShape = true;
                }
                
                shape.setType(shapeType);
                
                // 随机位置
                int size = MIN_SIZE + random.nextInt(MAX_SIZE - MIN_SIZE + 1);
                int x = size + random.nextInt(canvasWidth - 2 * size);
                int y = size + random.nextInt(canvasHeight - 2 * size);
                
                // 确保不重叠（简化检查）
                boolean overlaps = false;
                for (ShapeCaptchaResponse.Shape existing : shapes) {
                    int dx = Math.abs(x - existing.getX());
                    int dy = Math.abs(y - existing.getY());
                    if (dx < (size + existing.getSize()) && dy < (size + existing.getSize())) {
                        overlaps = true;
                        break;
                    }
                }
                
                if (overlaps) {
                    continue;
                }
                
                shape.setX(x);
                shape.setY(y);
                shape.setSize(size);
                shape.setColor(COLORS[random.nextInt(COLORS.length)]);
                
                shapes.add(shape);
            }
            
            // 如果没有目标形状，至少添加一个
            if (!hasTargetShape) {
                ShapeCaptchaResponse.Shape target = new ShapeCaptchaResponse.Shape();
                target.setType(targetShape);
                int size = MIN_SIZE + random.nextInt(MAX_SIZE - MIN_SIZE + 1);
                target.setX(size + random.nextInt(canvasWidth - 2 * size));
                target.setY(size + random.nextInt(canvasHeight - 2 * size));
                target.setSize(size);
                target.setColor(COLORS[random.nextInt(COLORS.length)]);
                shapes.add(target);
            }
            
            // 存储验证码
            if (useRedis && redisTemplate != null) {
                try {
                    redisTemplate.opsForValue().set(CAPTCHA_PREFIX + key, "shape:" + targetShape, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
                } catch (Exception e) {
                    // Redis失败，降级到内存存储
                    useRedis = false;
                    long expireTime = System.currentTimeMillis() + CAPTCHA_EXPIRE_MINUTES * 60 * 1000;
                    memoryCache.put(key, new CaptchaCache(targetShape, expireTime, "shape"));
                }
            } else {
                // 使用内存存储
                long expireTime = System.currentTimeMillis() + CAPTCHA_EXPIRE_MINUTES * 60 * 1000;
                memoryCache.put(key, new CaptchaCache(targetShape, expireTime, "shape"));
            }
            
            // 构建响应
            ShapeCaptchaResponse response = new ShapeCaptchaResponse();
            response.setKey(key);
            response.setTargetShape(targetShape);
            response.setShapes(shapes);
            response.setCanvasWidth(canvasWidth);
            response.setCanvasHeight(canvasHeight);
            
            return response;
        } catch (Exception e) {
            throw new RuntimeException("生成几何体验证码失败: " + e.getMessage(), e);
        }
    }

    public boolean validateCaptcha(String key, String code) {
        if (key == null || code == null) {
            return false;
        }
        
        boolean isValid = false;
        
        if (useRedis && redisTemplate != null) {
            try {
                String storedValue = redisTemplate.opsForValue().get(CAPTCHA_PREFIX + key);
                if (storedValue != null) {
                    if (storedValue.startsWith("text:")) {
                        // 文本验证码
                        String storedCode = storedValue.substring(5);
                        if (storedCode.equalsIgnoreCase(code)) {
                            isValid = true;
                        }
                    } else if (storedValue.startsWith("shape:")) {
                        // 几何体验证码
                        String targetShape = storedValue.substring(6);
                        if (targetShape.equalsIgnoreCase(code)) {
                            isValid = true;
                        }
                    }
                    
                    if (isValid) {
                        redisTemplate.delete(CAPTCHA_PREFIX + key);
                        return true;
                    }
                }
            } catch (Exception e) {
                // Redis失败，尝试从内存获取
                isValid = validateFromMemory(key, code);
                if (isValid) {
                    return true;
                }
            }
        } else {
            // 从内存获取
            isValid = validateFromMemory(key, code);
            if (isValid) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 从内存缓存验证验证码
     */
    private boolean validateFromMemory(String key, String code) {
        CaptchaCache cache = memoryCache.get(key);
        if (cache != null && !cache.isExpired()) {
            if ("text".equals(cache.type)) {
                // 文本验证码
                if (cache.code.equalsIgnoreCase(code)) {
                    memoryCache.remove(key);
                    return true;
                }
            } else if ("shape".equals(cache.type)) {
                // 几何体验证码
                if (cache.code.equalsIgnoreCase(code)) {
                    memoryCache.remove(key);
                    return true;
                }
            }
        }
        return false;
    }
}

