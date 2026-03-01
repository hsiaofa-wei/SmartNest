package com.rental.controller;

import com.rental.entity.House;
import com.rental.entity.RentOrder;
import com.rental.entity.ViewAppointment;
import com.rental.service.AppointmentService;
import com.rental.service.HouseService;
import com.rental.service.OrderService;
import com.rental.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/landlord")
@CrossOrigin
@PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
public class LandlordController {
    @Autowired
    private HouseService houseService;
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private PaymentService paymentService;

    @GetMapping("/houses")
    public ResponseEntity<Map<String, Object>> getMyHouses(
            @RequestParam Long landlordId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<House> houses = houseService.getHousesByLandlord(landlordId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", houses.getContent());
        response.put("totalElements", houses.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/appointments")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @RequestParam Long landlordId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ViewAppointment> appointments = appointmentService.getAppointmentsByLandlord(landlordId, status, startDate, endDate, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", appointments.getContent());
        response.put("totalElements", appointments.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<Void> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String reply) {
        appointmentService.updateAppointmentStatus(id, 
            ViewAppointment.AppointmentStatus.valueOf(status), reply);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getOrders(
            @RequestParam Long landlordId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RentOrder> orders = orderService.getOrdersByLandlord(landlordId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", orders.getContent());
        response.put("totalElements", orders.getTotalElements());
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        orderService.updateOrderStatus(id, RentOrder.OrderStatus.valueOf(status));
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/orders/{id}/payment-status")
    public ResponseEntity<Void> updateOrderPaymentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        orderService.updatePaymentStatus(id, RentOrder.PaymentStatus.valueOf(status));
        return ResponseEntity.ok().build();
    }
}

