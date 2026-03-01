package com.rental;

import java.math.BigDecimal;

/**
 * Simple validation test for monthly rent and deposit
 */
public class SimpleValidationTest {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal POSITIVE = new BigDecimal("1000.00");
    private static final BigDecimal NEGATIVE = new BigDecimal("-100.00");

    public static void main(String[] args) {
        System.out.println("=== Monthly Rent and Deposit Validation Test ===");
        System.out.println("Rule: Both monthly rent and deposit must be greater than 0");
        System.out.println();

        // Test cases
        testCase("Monthly rent > 0, Deposit > 0", POSITIVE, POSITIVE, true);
        testCase("Monthly rent = 0, Deposit > 0", ZERO, POSITIVE, false);
        testCase("Monthly rent > 0, Deposit = 0", POSITIVE, ZERO, false);
        testCase("Monthly rent = 0, Deposit = 0", ZERO, ZERO, false);
        testCase("Monthly rent < 0, Deposit > 0", NEGATIVE, POSITIVE, false);
        testCase("Monthly rent > 0, Deposit < 0", POSITIVE, NEGATIVE, false);
        
        System.out.println();
        System.out.println("=== All tests completed ===");
    }

    private static void testCase(String description, BigDecimal monthlyRent, BigDecimal deposit, boolean expected) {
        System.out.print(description + " ... ");
        
        try {
            validateAmounts(monthlyRent, deposit);
            if (expected) {
                System.out.println("PASS");
            } else {
                System.out.println("FAIL - Expected validation error");
            }
        } catch (Exception e) {
            if (!expected) {
                System.out.println("PASS - Correctly rejected: " + e.getMessage());
            } else {
                System.out.println("FAIL - Unexpected error: " + e.getMessage());
            }
        }
    }

    private static void validateAmounts(BigDecimal monthlyRent, BigDecimal deposit) {
        if (monthlyRent.compareTo(ZERO) <= 0) {
            throw new RuntimeException("Monthly rent must be greater than 0");
        }
        if (deposit.compareTo(ZERO) <= 0) {
            throw new RuntimeException("Deposit must be greater than 0");
        }
    }
}
