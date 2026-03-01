package com.rental.controller;

import com.rental.entity.RentOrder;
import com.rental.repository.RentOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/quick-cleanup")
public class QuickDataCleanupController {
    
    @Autowired
    private RentOrderRepository rentOrderRepository;
    
    /**
     * 快速清理无效订单数据（临时接口，无需认证）
     */
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> cleanupOrders() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 查找所有订单
            List<RentOrder> allOrders = rentOrderRepository.findAll();
            int totalOrders = allOrders.size();
            int invalidCount = 0;
            
            // 清理无效订单
            for (RentOrder order : allOrders) {
                boolean invalid = false;
                
                // 检查订单ID是否有效
                if (order.getId() == null || order.getId() <= 0) {
                    invalid = true;
                }
                
                // 检查订单号是否有效
                if (order.getOrderNo() == null || order.getOrderNo().trim().isEmpty()) {
                    invalid = true;
                }
                
                // 检查关键外键是否有效
                if (order.getTenantId() == null || order.getTenantId() <= 0 ||
                    order.getLandlordId() == null || order.getLandlordId() <= 0 ||
                    order.getHouseId() == null || order.getHouseId() <= 0) {
                    invalid = true;
                }
                
                // 检查金额是否有效
                if (order.getMonthlyRent() == null || order.getMonthlyRent().signum() <= 0 ||
                    order.getTotalAmount() == null || order.getTotalAmount().signum() <= 0) {
                    invalid = true;
                }
                
                if (invalid) {
                    rentOrderRepository.delete(order);
                    invalidCount++;
                }
            }
            
            response.put("code", 200);
            response.put("message", "订单数据清理完成");
            response.put("totalOrders", totalOrders);
            response.put("invalidOrdersRemoved", invalidCount);
            response.put("validOrdersRemaining", totalOrders - invalidCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "订单数据清理失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}