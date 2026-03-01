package com.rental.service;

import com.rental.entity.House;
import com.rental.entity.RentOrder;
import com.rental.repository.HouseRepository;
import com.rental.repository.RentOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; 
import java.util.UUID;

@Service
public class OrderService {
    @Autowired
    private RentOrderRepository orderRepository;
    
    @Autowired
    private HouseRepository houseRepository;

    public Page<RentOrder> getOrdersByTenant(Long tenantId, Pageable pageable) {
        return orderRepository.findByTenantId(tenantId, pageable);
    }

    public Page<RentOrder> getOrdersByLandlord(Long landlordId, Pageable pageable) {
        return orderRepository.findByLandlordId(landlordId, pageable);
    }

    @Transactional
    public RentOrder createOrder(RentOrder order) {
        // 验证月租金和押金都必须大于0
        if (order.getMonthlyRent().compareTo(BigDecimal.ZERO) <= 0 || order.getDeposit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("金额错误：月租金和押金必须均大于0");
        }
        
        // 验证订单月租金不能低于房源设定价格
        House house = houseRepository.findById(order.getHouseId())
                .orElseThrow(() -> new RuntimeException("房源不存在"));
        if (order.getMonthlyRent().compareTo(house.getPrice()) < 0) {
            throw new RuntimeException("金额错误：订单月租金不能低于房源设定价格");
        }
        
        order.setOrderNo(generateOrderNo());
        return orderRepository.save(order);
    }
    
    public RentOrder getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }
    
    @Transactional
    public RentOrder updateOrderStatus(Long orderId, RentOrder.OrderStatus status) {
        RentOrder order = getOrderById(orderId);
        order.setOrderStatus(status);
        return orderRepository.save(order);
    }
    
    @Transactional
    public RentOrder updatePaymentStatus(Long orderId, RentOrder.PaymentStatus status) {
        RentOrder order = getOrderById(orderId);
        order.setPaymentStatus(status);
        return orderRepository.save(order);
    }

    private String generateOrderNo() {
        return "RENT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

