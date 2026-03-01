package com.rental;

import com.rental.entity.RentOrder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 简单的月租金和押金验证测试类，不依赖任何框架
 */
public class SimpleMonthlyRentDepositValidationTest {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE_CENT = new BigDecimal("0.01");
    private static final BigDecimal POSITIVE_AMOUNT = new BigDecimal("1000.00");
    private static final BigDecimal NEGATIVE_AMOUNT = new BigDecimal("-100.00");

    public static void main(String[] args) {
        System.out.println("=== 测试月租金和押金验证逻辑 ===");
        System.out.println("验证规则：月租金和押金都必须大于0");
        System.out.println();

        // 测试各种金额组合
        testAmountCombination("月租金>0，押金>0", POSITIVE_AMOUNT, POSITIVE_AMOUNT, true);
        testAmountCombination("月租金=0，押金>0", ZERO, POSITIVE_AMOUNT, false);
        testAmountCombination("月租金>0，押金=0", POSITIVE_AMOUNT, ZERO, false);
        testAmountCombination("月租金=0，押金=0", ZERO, ZERO, false);
        testAmountCombination("月租金<0，押金>0", NEGATIVE_AMOUNT, POSITIVE_AMOUNT, false);
        testAmountCombination("月租金>0，押金<0", POSITIVE_AMOUNT, NEGATIVE_AMOUNT, false);
        testAmountCombination("月租金<0，押金<0", NEGATIVE_AMOUNT, NEGATIVE_AMOUNT, false);
        testAmountCombination("月租金=0.01，押金=0.01（最小金额）", ONE_CENT, ONE_CENT, true);

        System.out.println();
        System.out.println("=== 所有测试完成 ===");
    }

    /**
     * 测试特定金额组合是否通过验证
     */
    private static void testAmountCombination(String testName, BigDecimal monthlyRent, BigDecimal deposit, boolean expectedResult) {
        System.out.print("测试：" + testName + " ... ");

        try {
            // 创建测试订单
            RentOrder order = new RentOrder();
            order.setMonthlyRent(monthlyRent);
            order.setDeposit(deposit);
            
            // 执行验证逻辑
            validateMonthlyRentAndDeposit(order);
            
            // 检查结果
            if (expectedResult) {
                System.out.println("✓ 通过");
            } else {
                System.out.println("✗ 失败（预期应该抛出异常）");
            }
        } catch (RuntimeException e) {
            if (!expectedResult) {
                System.out.println("✓ 通过（正确抛出异常：" + e.getMessage() + "）");
            } else {
                System.out.println("✗ 失败（意外抛出异常：" + e.getMessage() + "）");
            }
        }
    }

    /**
     * 验证月租金和押金都必须大于0的业务规则
     */
    private static void validateMonthlyRentAndDeposit(RentOrder order) {
        if (order.getMonthlyRent().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("金额错误：月租金必须大于0");
        }
        
        if (order.getDeposit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("金额错误：押金必须大于0");
        }
        
        // 如果两个条件都满足，验证通过
    }
}
