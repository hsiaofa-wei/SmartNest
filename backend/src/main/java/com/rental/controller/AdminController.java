package com.rental.controller;

import com.rental.entity.*;
import com.rental.service.HouseService;
import com.rental.service.UserService;
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

@RestController
@RequestMapping("/admin")
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private HouseService houseService;
    
    @Autowired
    private com.rental.repository.SysUserRepository userRepository;
    
    @Autowired
    private com.rental.repository.HouseRepository houseRepository;
    
    @Autowired
    private com.rental.repository.RentOrderRepository orderRepository;
    
    @Autowired
    private com.rental.repository.HouseImageRepository houseImageRepository;
    
    @Autowired
    private com.rental.repository.HouseDetailRepository houseDetailRepository;
    
    @Autowired
    private com.rental.repository.HouseFavoriteRepository houseFavoriteRepository;
    
    @Autowired
    private com.rental.repository.ViewAppointmentRepository viewAppointmentRepository;
    
    @Autowired
    private com.rental.repository.OperationLogRepository operationLogRepository;
    
    @Autowired
    private com.rental.repository.SysFileRepository sysFileRepository;

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SysUser> users = userService.getAllUsers(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", users.getContent());
        response.put("totalElements", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        userService.updateUserStatus(id, SysUser.UserStatus.valueOf(status));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/houses/audit")
    public ResponseEntity<Map<String, Object>> getPendingAuditHouses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<House> houses = houseService.getPendingAuditHouses(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", houses.getContent());
        response.put("totalElements", houses.getTotalElements());
        response.put("totalPages", houses.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/houses/{id}/audit")
    public ResponseEntity<Void> auditHouse(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String remark) {
        houseService.auditHouse(id, House.AuditStatus.valueOf(status), remark);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalHouses", houseRepository.count());
        stats.put("totalOrders", orderRepository.count());
        stats.put("pendingAudits", houseRepository.countByAdminAuditStatus(House.AuditStatus.PENDING));
        
        // 按角色统计用户数
        Map<String, Long> usersByRole = new HashMap<>();
        usersByRole.put("ADMIN", userRepository.countByRole(SysUser.UserRole.ADMIN));
        usersByRole.put("LANDLORD", userRepository.countByRole(SysUser.UserRole.LANDLORD));
        usersByRole.put("TENANT", userRepository.countByRole(SysUser.UserRole.TENANT));
        stats.put("usersByRole", usersByRole);
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/statistics/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 初始化12个月的数据
        Integer[] monthlyUsers = new Integer[12];
        Integer[] monthlyHouses = new Integer[12];
        Integer[] monthlyOrders = new Integer[12];
        
        for (int i = 0; i < 12; i++) {
            final int month = i + 1;
            // 统计每个月的用户数
            long userCount = userRepository.countByCreateTimeMonth(month);
            monthlyUsers[i] = Math.toIntExact(userCount);
            
            // 统计每个月的房源数
            long houseCount = houseRepository.countByCreateTimeMonth(month);
            monthlyHouses[i] = Math.toIntExact(houseCount);
            
            // 统计每个月的订单数
            long orderCount = orderRepository.countByCreateTimeMonth(month);
            monthlyOrders[i] = Math.toIntExact(orderCount);
        }
        
        stats.put("monthlyUsers", monthlyUsers);
        stats.put("monthlyHouses", monthlyHouses);
        stats.put("monthlyOrders", monthlyOrders);
        
        return ResponseEntity.ok(stats);
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/users/{id}")
    public ResponseEntity<SysUser> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        SysUser existingUser = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setId(id);
        user.setPassword(existingUser.getPassword()); // 保持原密码
        return ResponseEntity.ok(userService.updateUser(user));
    }
    
    @DeleteMapping("/houses/{id}")
    public ResponseEntity<Void> deleteHouse(@PathVariable Long id) {
        houseService.deleteHouse(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 数据库查看接口 - 用于查看所有表的数据（仅管理员）
     * 此接口返回所有主要表的数据，方便查看数据库状态
     */
    @GetMapping("/database/view")
    public ResponseEntity<Map<String, Object>> viewDatabase() {
        Map<String, Object> database = new HashMap<>();
        
        // 用户表
        List<SysUser> users = userRepository.findAll();
        database.put("users", users);
        database.put("userCount", users.size());
        
        // 房源表
        List<House> houses = houseRepository.findAll();
        database.put("houses", houses);
        database.put("houseCount", houses.size());
        
        // 房源详情表
        List<HouseDetail> houseDetails = houseDetailRepository.findAll();
        database.put("houseDetails", houseDetails);
        database.put("houseDetailCount", houseDetails.size());
        
        // 房源图片表
        List<HouseImage> houseImages = houseImageRepository.findAll();
        database.put("houseImages", houseImages);
        database.put("houseImageCount", houseImages.size());
        
        // 订单表
        List<RentOrder> orders = orderRepository.findAll();
        database.put("orders", orders);
        database.put("orderCount", orders.size());
        
        // 预约看房表
        List<ViewAppointment> appointments = viewAppointmentRepository.findAll();
        database.put("appointments", appointments);
        database.put("appointmentCount", appointments.size());
        
        // 收藏表
        List<HouseFavorite> favorites = houseFavoriteRepository.findAll();
        database.put("favorites", favorites);
        database.put("favoriteCount", favorites.size());
        
        // 操作日志表
        List<OperationLog> logs = operationLogRepository.findAll();
        database.put("operationLogs", logs);
        database.put("logCount", logs.size());
        
        // 文件表
        List<SysFile> files = sysFileRepository.findAll();
        database.put("files", files);
        database.put("fileCount", files.size());
        
        return ResponseEntity.ok(database);
    }
}

