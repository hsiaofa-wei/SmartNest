package com.rental.service;

import com.rental.entity.House;
import com.rental.entity.RentOrder;
import com.rental.repository.HouseRepository;
import com.rental.repository.RentOrderRepository;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * A simple test to verify that order monthly rent cannot be lower than house predefined price.
 * This test uses Mockito to mock repository dependencies and test the validation logic directly.
 */
public class HousePriceValidationTest {

    public static void main(String[] args) {
        System.out.println("Testing House Price Validation Logic...");
        
        // Mock the repositories
        HouseRepository mockHouseRepo = Mockito.mock(HouseRepository.class);
        RentOrderRepository mockOrderRepo = Mockito.mock(RentOrderRepository.class);
        
        // Create test data
        House house = new House();
        house.setId(1L);
        house.setPrice(new BigDecimal("1000.00")); // House price is 1000.00
        
        // Create order with monthly rent equal to house price
        RentOrder orderEqual = new RentOrder();
        orderEqual.setHouseId(1L);
        orderEqual.setMonthlyRent(new BigDecimal("1000.00")); // Monthly rent equals house price
        orderEqual.setDeposit(new BigDecimal("2000.00"));
        orderEqual.setStartDate(LocalDate.now());
        orderEqual.setEndDate(LocalDate.now().plusMonths(1));
        orderEqual.setTotalAmount(new BigDecimal("3000.00"));
        
        // Create order with monthly rent higher than house price
        RentOrder orderHigher = new RentOrder();
        orderHigher.setHouseId(1L);
        orderHigher.setMonthlyRent(new BigDecimal("1200.00")); // Monthly rent is higher than house price
        orderHigher.setDeposit(new BigDecimal("2000.00"));
        orderHigher.setStartDate(LocalDate.now());
        orderHigher.setEndDate(LocalDate.now().plusMonths(1));
        orderHigher.setTotalAmount(new BigDecimal("3200.00"));
        
        // Create order with monthly rent lower than house price
        RentOrder orderLower = new RentOrder();
        orderLower.setHouseId(1L);
        orderLower.setMonthlyRent(new BigDecimal("800.00")); // Monthly rent is lower than house price
        orderLower.setDeposit(new BigDecimal("2000.00"));
        orderLower.setStartDate(LocalDate.now());
        orderLower.setEndDate(LocalDate.now().plusMonths(1));
        orderLower.setTotalAmount(new BigDecimal("2800.00"));
        
        // Configure mock behavior
        Mockito.when(mockHouseRepo.findById(1L)).thenReturn(Optional.of(house));
        Mockito.when(mockOrderRepo.save(Mockito.any(RentOrder.class))).thenReturn(Mockito.any(RentOrder.class));
        
        // Create OrderService instance with mocked dependencies
        OrderService orderService = new OrderService();
        injectMock(orderService, "houseRepository", mockHouseRepo);
        injectMock(orderService, "orderRepository", mockOrderRepo);
        
        // Test cases
        System.out.println("\nTest 1: Monthly rent equals house price (should pass)");
        try {
            orderService.createOrder(orderEqual);
            System.out.println("✓ Test passed: Order created successfully");
        } catch (Exception e) {
            System.out.println("✗ Test failed: " + e.getMessage());
        }
        
        System.out.println("\nTest 2: Monthly rent higher than house price (should pass)");
        try {
            orderService.createOrder(orderHigher);
            System.out.println("✓ Test passed: Order created successfully");
        } catch (Exception e) {
            System.out.println("✗ Test failed: " + e.getMessage());
        }
        
        System.out.println("\nTest 3: Monthly rent lower than house price (should fail)");
        try {
            orderService.createOrder(orderLower);
            System.out.println("✗ Test failed: Order should not be created when monthly rent is lower than house price");
        } catch (Exception e) {
            System.out.println("✓ Test passed: Correctly rejected with message: " + e.getMessage());
        }
        
        System.out.println("\nAll tests completed!");
    }
    
    /**
     * Helper method to inject mock dependencies using reflection
     */
    private static void injectMock(Object target, String fieldName, Object mock) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, mock);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
