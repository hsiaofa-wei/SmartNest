package com.rental.service;

import com.alipay.api.AlipayApiException;
import com.rental.entity.PaymentRecord;
import com.rental.entity.RentOrder;
import com.rental.repository.PaymentRecordRepository;
import com.rental.repository.RentOrderRepository;
import com.rental.util.AlipayUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class RefundServiceTest {

    @Mock
    private RentOrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private AlipayUtil alipayUtil;

    @Mock
    private PaymentRecordRepository paymentRecordRepository;

    @InjectMocks
    private PaymentService paymentService;

    private RentOrder testOrder;
    private PaymentRecord testPaymentRecord;

    @BeforeEach
    void setUp() {
        // 创建测试订单
        testOrder = new RentOrder();
        testOrder.setId(1L);
        testOrder.setOrderNo("TEST_ORDER_12345");
        testOrder.setPaymentStatus(RentOrder.PaymentStatus.PAID);
        testOrder.setOrderStatus(RentOrder.OrderStatus.CONFIRMED);
        testOrder.setMonthlyRent(new BigDecimal("1000.00"));
        testOrder.setDeposit(new BigDecimal("2000.00"));
        testOrder.setTotalAmount(new BigDecimal("3000.00"));

        // 创建测试支付记录
        testPaymentRecord = new PaymentRecord();
        testPaymentRecord.setId(1L);
        testPaymentRecord.setOrderId(1L);
        testPaymentRecord.setOrderNo("TEST_ORDER_12345");
        testPaymentRecord.setPaymentAmount(new BigDecimal("3000.00"));
        testPaymentRecord.setPaymentType("ALIPAY");
        testPaymentRecord.setStatus("SUCCESS");
        testPaymentRecord.setTransactionId("ALIPAY_TRADE_12345");
    }

    @Test
    void testRefundOrderSuccess() throws AlipayApiException {
        // 模拟订单和支付记录的查询
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRecordRepository.findByOrderId(1L)).thenReturn(java.util.Collections.singletonList(testPaymentRecord));
        when(alipayUtil.refund(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(true);
        when(paymentRecordRepository.save(any(PaymentRecord.class))).thenReturn(testPaymentRecord);
        when(orderRepository.save(any(RentOrder.class))).thenReturn(testOrder);

        // 执行退款操作
        boolean result = paymentService.refundOrder(1L, "测试退款原因");

        // 验证结果
        assertTrue(result);
        verify(alipayUtil, times(1)).refund("TEST_ORDER_12345", "ALIPAY_TRADE_12345", "3000.00", "测试退款原因", anyString());
        verify(paymentRecordRepository, times(1)).save(any(PaymentRecord.class));
        verify(orderRepository, times(1)).save(any(RentOrder.class));
        assertEquals(RentOrder.PaymentStatus.REFUNDED, testOrder.getPaymentStatus());
        assertEquals(RentOrder.OrderStatus.CANCELLED, testOrder.getOrderStatus());
    }

    @Test
    void testRefundOrderNotFound() {
        // 模拟订单不存在
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行退款操作，预期抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.refundOrder(1L, "测试退款原因");
        });

        // 验证异常信息
        assertEquals("订单不存在", exception.getMessage());
    }

    @Test
    void testRefundOrderNotPaid() {
        // 设置订单为未支付状态
        testOrder.setPaymentStatus(RentOrder.PaymentStatus.UNPAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // 执行退款操作，预期抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.refundOrder(1L, "测试退款原因");
        });

        // 验证异常信息
        assertEquals("订单未支付，无法退款", exception.getMessage());
    }

    @Test
    void testRefundOrderAlipayFailed() throws AlipayApiException {
        // 模拟支付宝退款失败
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRecordRepository.findByOrderId(1L)).thenReturn(java.util.Collections.singletonList(testPaymentRecord));
        when(alipayUtil.refund(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(false);

        // 执行退款操作，预期抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.refundOrder(1L, "测试退款原因");
        });

        // 验证异常信息
        assertEquals("支付宝退款失败", exception.getMessage());
    }

    @Test
    void testRefundOrderAlipayException() throws AlipayApiException {
        // 模拟支付宝API异常
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRecordRepository.findByOrderId(1L)).thenReturn(java.util.Collections.singletonList(testPaymentRecord));
        when(alipayUtil.refund(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new AlipayApiException("退款API调用失败"));

        // 执行退款操作，预期抛出异常
        AlipayApiException exception = assertThrows(AlipayApiException.class, () -> {
            paymentService.refundOrder(1L, "测试退款原因");
        });

        // 验证异常信息
        assertEquals("退款API调用失败", exception.getMessage());
    }
}