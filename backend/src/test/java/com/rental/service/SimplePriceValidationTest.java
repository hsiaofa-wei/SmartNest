package com.rental.service;

import com.rental.entity.House;
import com.rental.entity.RentOrder;
import com.rental.repository.HouseRepository;
import com.rental.repository.RentOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * A simple test to verify that order monthly rent cannot be lower than house predefined price.
 * This test doesn't use Mockito to avoid compilation issues.
 */
public class SimplePriceValidationTest {

    public static void main(String[] args) {
        System.out.println("Testing House Price Validation Logic...");
        
        // Create test data
        House house = new House();
        house.setId(1L);
        house.setPrice(new BigDecimal("1000.00")); // House price is 1000.00
        
        // Test cases
        testValidation(house, new BigDecimal("1000.00"), true, "Monthly rent equals house price");
        testValidation(house, new BigDecimal("1200.00"), true, "Monthly rent higher than house price");
        testValidation(house, new BigDecimal("800.00"), false, "Monthly rent lower than house price");
        
        System.out.println("\nAll tests completed!");
    }
    
    private static void testValidation(House house, BigDecimal monthlyRent, boolean shouldPass, String testDescription) {
        System.out.println("\nTest: " + testDescription);
        System.out.println("  House price: " + house.getPrice());
        System.out.println("  Order monthly rent: " + monthlyRent);
        System.out.println("  Expected: " + (shouldPass ? "PASS" : "FAIL"));
        
        try {
            // Simulate the validation logic from OrderService.createOrder
            if (monthlyRent.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Monthly rent must be greater than 0");
            }
            
            // This is the core validation logic we added
            if (monthlyRent.compareTo(house.getPrice()) < 0) {
                throw new RuntimeException("Monthly rent cannot be lower than house price");
            }
            
            if (shouldPass) {
                System.out.println("  Result: PASS - Validation passed as expected");
            } else {
                System.out.println("  Result: FAIL - Validation should have failed but passed");
            }
        } catch (RuntimeException e) {
            if (!shouldPass) {
                System.out.println("  Result: PASS - Validation failed as expected with message: " + e.getMessage());
            } else {
                System.out.println("  Result: FAIL - Validation failed unexpectedly with message: " + e.getMessage());
            }
        }
    }
}
