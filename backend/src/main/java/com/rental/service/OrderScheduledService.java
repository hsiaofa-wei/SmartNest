package com.rental.service;

import com.rental.entity.RentOrder;
import com.rental.repository.RentOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@Service
public class OrderScheduledService {
    private static final Logger logger = Logger.getLogger(OrderScheduledService.class.getName());

    @Autowired
    private RentOrderRepository rentOrderRepository;

    @Autowired
    private OrderService orderService;

    /**
     * 每天凌晨1点执行，将已到期但状态仍为ACTIVE的订单转换为COMPLETED状态
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void autoCompleteExpiredOrders() {
        logger.info("开始自动处理到期订单...");
        LocalDate today = LocalDate.now();
        
        // 查询所有已到期但状态仍为ACTIVE的订单
        List<RentOrder> expiredOrders = rentOrderRepository.findByEndDateLessThanEqualAndOrderStatus(
                today, RentOrder.OrderStatus.ACTIVE);
        
        logger.info("发现 " + expiredOrders.size() + " 个已到期但未完成的订单");
        
        // 将这些订单的状态更新为COMPLETED
        for (RentOrder order : expiredOrders) {
            try {
                orderService.updateOrderStatus(order.getId(), RentOrder.OrderStatus.COMPLETED);
                logger.info("订单 " + order.getOrderNo() + " 已自动完成");
            } catch (Exception e) {
                logger.severe("处理订单 " + order.getOrderNo() + " 失败: " + e.getMessage());
            }
        }
        
        logger.info("到期订单处理完成");
    }
}