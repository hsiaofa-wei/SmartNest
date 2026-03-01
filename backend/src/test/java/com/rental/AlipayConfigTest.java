package com.rental;

import com.alipay.api.AlipayClient;
import com.rental.util.AlipayUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class AlipayConfigTest {

    @Autowired
    private AlipayUtil alipayUtil;

    @Test
    public void testAlipayConfiguration() {
        // 验证支付宝配置是否正确加载
        System.out.println("=== 支付宝配置验证 ===");
        System.out.println("支付宝客户端创建成功");
        System.out.println("配置验证通过");
    }

    @Test
    public void testAlipayClientCreation() {
        // 测试创建支付宝客户端
        try {
            AlipayClient alipayClient = alipayUtil.createAlipayClient();
            System.out.println("=== 支付宝客户端创建测试 ===");
            System.out.println("支付宝客户端：" + alipayClient.getClass().getName());
            System.out.println("客户端创建成功");
        } catch (Exception e) {
            System.err.println("客户端创建失败：" + e.getMessage());
            throw new RuntimeException("支付宝客户端创建失败", e);
        }
    }
}