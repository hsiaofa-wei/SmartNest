package com.rental.controller;

import com.rental.entity.HouseFavorite;
import com.rental.entity.RentOrder;
import com.rental.entity.ViewAppointment;
import com.rental.service.AppointmentService;
import com.rental.service.FavoriteService;
import com.rental.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tenant")
@CrossOrigin
@PreAuthorize("hasAnyRole('TENANT', 'ADMIN')")
public class TenantController {
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private OrderService orderService;

    @PostMapping("/favorites/{houseId}")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable Long houseId,
            @RequestParam Long tenantId) {
        HouseFavorite favorite = favoriteService.toggleFavorite(tenantId, houseId);
        Map<String, Object> response = new HashMap<>();
        response.put("isFavorited", favorite != null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites")
    public ResponseEntity<Map<String, Object>> getFavorites(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<HouseFavorite> favorites = favoriteService.getFavorites(tenantId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", favorites.getContent());
        response.put("totalElements", favorites.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/appointments")
    public ResponseEntity<?> createAppointment(@RequestBody ViewAppointment appointment) {
        // 基础校验，避免必填为空触发数据库约束错误
        if (appointment.getTenantId() == null) {
            return ResponseEntity.badRequest().body(error("租客ID不能为空"));
        }
        if (appointment.getHouseId() == null) {
            return ResponseEntity.badRequest().body(error("房源ID不能为空"));
        }
        if (appointment.getLandlordId() == null) {
            return ResponseEntity.badRequest().body(error("房东ID不能为空"));
        }
        if (appointment.getAppointmentTime() == null) {
            return ResponseEntity.badRequest().body(error("预约时间不能为空"));
        }
        if (appointment.getContactPhone() == null || appointment.getContactPhone().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(error("联系电话不能为空"));
        }
        return ResponseEntity.ok(appointmentService.createAppointment(appointment));
    }

    @GetMapping("/appointments")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ViewAppointment> appointments = appointmentService.getAppointmentsByTenant(tenantId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", appointments.getContent());
        response.put("totalElements", appointments.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getOrders(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RentOrder> orders = orderService.getOrdersByTenant(tenantId, pageable);
        
        // 过滤掉没有有效ID的订单
        List<RentOrder> validOrders = orders.getContent().stream()
                .filter(order -> order != null && order.getId() != null && order.getId() > 0)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", validOrders);
        response.put("totalElements", orders.getTotalElements());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody RentOrder order) {
        try {
            // 检查是否有已确认的预约
            List<ViewAppointment> confirmedAppointments = appointmentService.getConfirmedAppointments(order.getTenantId(), order.getHouseId());
            if (confirmedAppointments.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("message", "请先完成房源预约并等待房东确认");
                return ResponseEntity.badRequest().body(error);
            }
            
            RentOrder savedOrder = orderService.createOrder(order);
            Map<String, Object> response = new HashMap<>();
            response.put("order", savedOrder);
            response.put("message", "订单创建成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "创建订单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrder(@PathVariable String id) {
        // 参数验证
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(error("订单ID不能为空"));
        }
        
        Long orderId;
        try {
            orderId = Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(error("订单ID格式错误: " + id));
        }
        
        if (orderId <= 0) {
            return ResponseEntity.badRequest().body(error("无效的订单ID: " + orderId));
        }
        
        RentOrder order = orderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 400);
        map.put("message", message);
        return map;
    }
}

