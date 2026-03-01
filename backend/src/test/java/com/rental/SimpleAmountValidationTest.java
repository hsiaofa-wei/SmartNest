package com.rental;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Simple amount validation test class without Spring Boot and JUnit dependencies
 */
public class SimpleAmountValidationTest {

    // Alipay amount limits
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");

    /**
     * Validate amount meets Alipay requirements
     */
    public static boolean validateAmount(BigDecimal amount) {
        try {
            // Minimum amount: 0.01 yuan (1 cent)
            if (amount.compareTo(MIN_AMOUNT) < 0) {
                System.out.println("[FAIL] Amount must be at least 0.01: " + amount);
                return false;
            }

            // Maximum amount: 10000.00 yuan (sandbox environment)
            if (amount.compareTo(MAX_AMOUNT) > 0) {
                System.out.println("[FAIL] Amount cannot exceed 10000.00 in sandbox: " + amount);
                return false;
            }

            // Must be exactly two decimal places
            if (amount.scale() != 2) {
                System.out.println("[FAIL] Amount must have exactly two decimal places: " + amount);
                return false;
            }

            System.out.println("[PASS] Amount validation passed: " + amount);
            return true;
        } catch (Exception e) {
            System.out.println("[FAIL] Amount validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Format amount to Alipay required format
     */
    public static BigDecimal formatAmount(String amountStr) {
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            return amount.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            System.out.println("[FAIL] Amount formatting failed: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Alipay Amount Validation Test ===");
        
        // Test correct amounts
        System.out.println("\n1. Testing correct amounts:");
        String[] correctAmounts = {"0.01", "1.00", "100.00", "99.99", "5000.00"};
        
        for (String amountStr : correctAmounts) {
            BigDecimal formattedAmount = formatAmount(amountStr);
            if (formattedAmount != null) {
                validateAmount(formattedAmount);
            }
        }
        
        // Test incorrect amount formats
        System.out.println("\n2. Testing incorrect amount formats:");
        String[] incorrectAmounts = {
            "0.1",      // One decimal place
            "100",       // Integer (missing decimal part)
            "100.0",     // One decimal place
            "100.000",   // Three decimal places
            "-0.01",     // Negative number
            "0.00",      // Zero amount
            "10000.01",  // Exceeds maximum amount
            "1,000.00"   // Has thousand separator
        };
        
        for (String amountStr : incorrectAmounts) {
            BigDecimal formattedAmount = formatAmount(amountStr);
            if (formattedAmount != null) {
                validateAmount(formattedAmount);
            }
        }
        
        // Test amount formatting functionality
        System.out.println("\n3. Testing amount formatting:");
        String[] formatTestCases = {
            "0.01",     // Correct format
            "100",       // Missing decimal part
            "99.9",      // One decimal place
            "88.888",    // Three decimal places
            "1,000.00",  // Has thousand separator
            "99.995"     // Needs rounding
        };
        
        for (String test : formatTestCases) {
            BigDecimal formatted = formatAmount(test);
            System.out.println(test + " -> " + (formatted != null ? formatted : "Format error"));
        }
        
        System.out.println("\n=== Test Completed ===");
    }
}
