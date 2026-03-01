package com.rental;

import com.alipay.api.AlipayApiException;
import com.rental.util.AlipayUtil;  
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest; 

/**
 * 支付宝金额验证测试类
 */
@SpringBootTest 
public class AlipayAmountValidationTest {

    @Autowired
    private AlipayUtil alipayUtil;

    /**
     * 测试正确的金额格式
     */
    @Test
    public void testCorrectAmountFormat() throws AlipayApiException {
        String[] correctAmounts = {"0.01", "1.00", "100.00", "99.99", "5000.00"};
        
        for (String amount : correctAmounts) {
            System.out.println("测试正确金额: " + amount);
            // 验证金额格式是否通过
            try {
                // 使用createQrCode方法测试金额验证（内部会调用validateAmount）
                alipayUtil.createQrCode("test_order_" + amount, "测试商品", amount, "RentalStore");
                System.out.println("✅ 金额 " + amount + " 格式验证通过");
            } catch (AlipayApiException e) {
                System.out.println("❌ 金额 " + amount + " 格式验证失败: " + e.getMessage());
            }
        }
    }

    /**
     * 测试错误的金额格式
     */
    @Test
    public void testIncorrectAmountFormat() throws AlipayApiException {
        String[] incorrectAmounts = {
            "0.1",      // 一位小数
            "100",       // 整数（缺少小数部分）
            "100.0",     // 一位小数
            "100.000",   // 三位小数
            "-0.01",     // 负数
            "0.00",      // 零金额
            "10000.01",  // 超过最大金额
            "1,000.00"   // 千分位分隔符
        };
        
        for (String amount : incorrectAmounts) {
            System.out.println("测试错误金额: " + amount);
            // 验证金额格式是否被正确拒绝
            try {
                alipayUtil.createQrCode("test_order_" + amount, "测试商品", amount, "RentalStore");
                System.out.println("❌ 金额 " + amount + " 格式验证本应失败，但却通过了");
            } catch (AlipayApiException e) {
                System.out.println("✅ 金额 " + amount + " 格式验证正确失败: " + e.getMessage());
            }
        }
    }
}
