package com.rental.service;

import com.alipay.api.AlipayApiException;
import com.rental.entity.PaymentRecord;
import com.rental.entity.RentOrder;
import com.rental.entity.ViewAppointment;
import com.rental.repository.PaymentRecordRepository;
import com.rental.repository.RentOrderRepository;
import com.rental.repository.ViewAppointmentRepository;
import com.rental.util.AlipayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PaymentService {
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#0.00");
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    static {
        AMOUNT_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
    }
    
    @Autowired
    private RentOrderRepository orderRepository;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private AlipayUtil alipayUtil;
    
    @Autowired
    private PaymentRecordRepository paymentRecordRepository;
    
    @Autowired
    private ViewAppointmentRepository appointmentRepository;


    
    /**
     * 创建订单（包含定金）
     */
    @Transactional
    public RentOrder createOrderWithDeposit(RentOrder order, BigDecimal depositAmount) {
        order.setDeposit(depositAmount);
        order.setPaymentStatus(RentOrder.PaymentStatus.UNPAID);
        return orderService.createOrder(order);
    }

    /**
     * 创建支付宝支付订单（电脑网站支付）
     */
    @Transactional
    public String createAlipayOrder(Long orderId) throws AlipayApiException {
        logger.info("开始创建支付宝支付订单，orderId: {}", orderId);
        
        RentOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("创建支付宝支付订单失败：订单不存在，orderId: {}", orderId);
                    return new RuntimeException("订单不存在");
                });

        logger.info("找到订单，订单ID: {}, 订单号: {}, 当前支付状态: {}, 订单状态: {}", 
                   order.getId(), order.getOrderNo(), order.getPaymentStatus(), order.getOrderStatus());
                    
        if (order.getPaymentStatus() == RentOrder.PaymentStatus.PAID) {
            logger.error("创建支付宝支付订单失败：订单已支付，orderId: {}, orderNo: {}", 
                        order.getId(), order.getOrderNo());
            throw new RuntimeException("订单已支付");
        }

        // 验证月租金和押金都必须大于0
        if (order.getMonthlyRent().compareTo(BigDecimal.ZERO) <= 0 || order.getDeposit().compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("创建支付宝支付订单失败：金额错误，月租金: {}, 押金: {}", 
                        order.getMonthlyRent(), order.getDeposit());
            throw new RuntimeException("金额错误：月租金和押金必须均大于0");
        }

        // 创建支付记录
        logger.info("开始创建支付记录，orderId: {}, orderNo: {}", order.getId(), order.getOrderNo());
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrderId(order.getId());
        paymentRecord.setOrderNo(order.getOrderNo());
        paymentRecord.setUserId(order.getTenantId());
        
        // 计算支付金额（直接支付总金额，包含押金和租金）
        BigDecimal paymentAmount = order.getTotalAmount();
        paymentRecord.setPaymentAmount(paymentAmount);
        paymentRecord.setPaymentType("ALIPAY");
        paymentRecord.setStatus("PENDING");
        
        PaymentRecord savedPaymentRecord = paymentRecordRepository.save(paymentRecord);
        logger.info("支付记录创建成功，支付记录ID: {}, orderId: {}, orderNo: {}, paymentAmount: {}, paymentType: {}, status: {}", 
                   savedPaymentRecord.getId(), savedPaymentRecord.getOrderId(), savedPaymentRecord.getOrderNo(), 
                   savedPaymentRecord.getPaymentAmount(), savedPaymentRecord.getPaymentType(), savedPaymentRecord.getStatus());

        // 创建支付宝支付表单
        String outTradeNo = order.getOrderNo();
        String subject = "房屋租金支付 - " + order.getOrderNo();
        // 直接使用BigDecimal格式化，避免精度丢失
        String totalAmount = paymentAmount.setScale(2, RoundingMode.HALF_UP).toString();
        String body = "订单号：" + order.getOrderNo() + "，支付金额：" + paymentAmount;

        try {
            return alipayUtil.createPagePayForm(outTradeNo, subject, totalAmount, body, order.getId());
        } catch (AlipayApiException e) {
            // 如果是交易已支付的错误，直接返回成功，不抛出异常
            if (e.getMessage() != null && e.getMessage().contains("TRADE_HAS_SUCCESS")) {
                // 更新支付记录状态
                paymentRecord.setStatus("SUCCESS");
                paymentRecord.setRemark("交易已支付");
                paymentRecordRepository.save(paymentRecord);
                
                // 更新订单状态
                order.setPaymentStatus(RentOrder.PaymentStatus.PAID);
                if (order.getDeposit() == null || order.getDeposit().compareTo(order.getTotalAmount()) == 0) {
                    order.setOrderStatus(RentOrder.OrderStatus.CONFIRMED);
                }
                orderRepository.save(order);
                
                // 抛出订单已支付的异常，让前端处理
                throw new RuntimeException("订单已支付");
            }
            // 其他错误继续抛出
            throw e;
        }
    }
    
    /**
     * 创建支付宝二维码支付（当面付）
     */
    @Transactional
    public String createAlipayQrCode(Long orderId) throws AlipayApiException {
        RentOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (order.getPaymentStatus() == RentOrder.PaymentStatus.PAID) {
            throw new RuntimeException("订单已支付");
        }

        // 验证月租金和押金都必须大于0
        if (order.getMonthlyRent().compareTo(BigDecimal.ZERO) <= 0 || order.getDeposit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("金额错误：月租金和押金必须均大于0");
        }

        // 创建支付记录
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrderId(order.getId());
        paymentRecord.setOrderNo(order.getOrderNo());
        paymentRecord.setUserId(order.getTenantId());
        
        // 计算支付金额（直接支付总金额，包含押金和租金）
        BigDecimal paymentAmount = order.getTotalAmount();
        paymentRecord.setPaymentAmount(paymentAmount);
        paymentRecord.setPaymentType("ALIPAY_QR");
        paymentRecord.setStatus("PENDING");
        paymentRecordRepository.save(paymentRecord);

        // 生成动态二维码
        String outTradeNo = order.getOrderNo();
        String subject = "房屋租金支付 - " + order.getOrderNo();
        // 直接使用BigDecimal格式化，避免精度丢失
        String totalAmount = paymentAmount.setScale(2, RoundingMode.HALF_UP).toString();
        String storeId = "RentalStore";

        // 尝试生成二维码
        try {
            return alipayUtil.createQrCode(outTradeNo, subject, totalAmount, storeId);
        } catch (AlipayApiException e) {
            // 如果是交易已支付的错误，直接返回成功，不抛出异常
            if (e.getMessage() != null && e.getMessage().contains("TRADE_HAS_SUCCESS")) {
                // 更新支付记录状态
                paymentRecord.setStatus("SUCCESS");
                paymentRecord.setRemark("交易已支付");
                paymentRecordRepository.save(paymentRecord);
                
                // 更新订单状态
                order.setPaymentStatus(RentOrder.PaymentStatus.PAID);
                if (order.getDeposit() == null || order.getDeposit().compareTo(order.getTotalAmount()) == 0) {
                    order.setOrderStatus(RentOrder.OrderStatus.CONFIRMED);
                }
                orderRepository.save(order);
                
                // 抛出订单已支付的异常，让前端处理
                throw new RuntimeException("订单已支付");
            }
            // 其他错误继续抛出
            throw e;
        }
    }

    /**
     * 主动查询并更新订单支付状态
     */
    @Transactional
    public boolean queryAndUpdateOrderStatus(Long orderId) throws AlipayApiException {
        logger.info("开始主动查询订单支付状态，orderId: {}", orderId);
        
        // 查找订单
        RentOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("主动查询订单支付状态失败：订单不存在，orderId: {}", orderId);
                    return new RuntimeException("订单不存在");
                });
        
        // 如果订单已经是已支付状态，直接返回成功
        if (order.getPaymentStatus() == RentOrder.PaymentStatus.PAID) {
            logger.info("订单已经是已支付状态，无需查询，orderId: {}", orderId);
            return true;
        }
        
        logger.info("订单当前支付状态：{}", order.getPaymentStatus());
        
        // 调用支付宝API查询订单状态
        String tradeStatus = alipayUtil.queryOrderStatus(order.getOrderNo());
        logger.info("支付宝返回的交易状态：{}", tradeStatus);
        
        // 如果支付成功，更新订单状态
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            logger.info("支付成功，更新订单状态，orderId: {}", orderId);
            
            // 查找或创建支付记录
            List<PaymentRecord> paymentRecords = paymentRecordRepository.findByOrderId(order.getId());
            PaymentRecord paymentRecord = paymentRecords.isEmpty() ? null : paymentRecords.get(0);
            if (paymentRecord == null) {
                logger.info("未找到支付记录，创建新的支付记录");
                paymentRecord = new PaymentRecord();
                paymentRecord.setOrderId(order.getId());
                paymentRecord.setOrderNo(order.getOrderNo());
                paymentRecord.setUserId(order.getTenantId());
                paymentRecord.setPaymentAmount(order.getTotalAmount()); // 直接支付总金额，包含押金和租金
                paymentRecord.setPaymentType("ALIPAY");
            }
            
            // 更新支付记录
            paymentRecord.setStatus("SUCCESS");
            paymentRecordRepository.save(paymentRecord);
            logger.info("支付记录已更新为成功状态");
            
            // 更新订单支付状态
            order.setPaymentStatus(RentOrder.PaymentStatus.PAID);
            
            // 如果支付的是全款，更新订单状态为已确认
            if (order.getDeposit() == null || order.getDeposit().compareTo(order.getTotalAmount()) == 0) {
                logger.info("支付的是全款，更新订单状态为已确认");
                order.setOrderStatus(RentOrder.OrderStatus.CONFIRMED);
            }
            
            RentOrder savedOrder = orderRepository.save(order);
            logger.info("订单状态已更新：orderStatus={}, paymentStatus={}", 
                       savedOrder.getOrderStatus(), savedOrder.getPaymentStatus());
            
            return true;
        }
        
        logger.info("支付尚未完成，交易状态：{}", tradeStatus);
        return false;
    }
    
    /**
     * 处理支付宝支付结果回调
     */
    @Transactional
    public boolean handleAlipayCallback(Map<String, String> params) throws AlipayApiException {
        logger.info("收到支付宝回调请求，参数数量: {}, 参数: {}", params.size(), params);
        
        // 打印关键参数
        logger.info("回调关键参数: trade_status={}, out_trade_no={}, trade_no={}, total_amount={}", 
                   params.get("trade_status"), params.get("out_trade_no"), 
                   params.get("trade_no"), params.get("total_amount"));
        
        // 验证签名
        boolean signValid = false;
        try {
            signValid = alipayUtil.verifyCallback(params);
            logger.info("支付宝回调签名验证结果: {}", signValid);
        } catch (AlipayApiException e) {
            logger.error("支付宝回调签名验证异常: {}", e.getMessage());
            throw e;
        }
        
        if (!signValid) {
            logger.error("支付宝回调签名验证失败");
            return false;
        }

        // 获取回调参数
        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String totalAmount = params.get("total_amount");
        
        logger.info("支付宝回调参数解析: tradeStatus={}, outTradeNo={}, tradeNo={}, totalAmount={}", 
                   tradeStatus, outTradeNo, tradeNo, totalAmount);

        // 查找订单
        Optional<RentOrder> optionalOrder = orderRepository.findByOrderNo(outTradeNo);
        if (!optionalOrder.isPresent()) {
            logger.error("支付宝回调处理失败：订单不存在，订单号: {}", outTradeNo);
            return false;
        }
        
        RentOrder order = optionalOrder.get();
        logger.info("找到订单，当前订单状态: orderStatus={}, paymentStatus={}", 
                   order.getOrderStatus(), order.getPaymentStatus());

        // 如果订单已经是已支付状态，直接返回成功，避免重复处理
        if (order.getPaymentStatus() == RentOrder.PaymentStatus.PAID) {
            logger.info("订单已经是已支付状态，无需重复处理");
            return true;
        }

        // 查找支付记录
        List<PaymentRecord> paymentRecords = paymentRecordRepository.findByOrderId(order.getId());
        PaymentRecord paymentRecord = paymentRecords.isEmpty() ? null : paymentRecords.get(0);
        if (paymentRecord == null) {
            logger.info("未找到支付记录，创建新的支付记录，orderId: {}, orderNo: {}", order.getId(), outTradeNo);
            paymentRecord = new PaymentRecord();
            paymentRecord.setOrderId(order.getId());
            paymentRecord.setOrderNo(outTradeNo);
            paymentRecord.setUserId(order.getTenantId());
            // 处理totalAmount可能为null的情况
            BigDecimal amount = totalAmount != null ? new BigDecimal(totalAmount) : BigDecimal.ZERO;
            paymentRecord.setPaymentAmount(amount);
            paymentRecord.setPaymentType("ALIPAY");
            paymentRecord.setStatus("PENDING");
            logger.info("新创建的支付记录信息: orderId={}, orderNo={}, paymentAmount={}, paymentType={}, status={}", 
                       paymentRecord.getOrderId(), paymentRecord.getOrderNo(), paymentRecord.getPaymentAmount(), 
                       paymentRecord.getPaymentType(), paymentRecord.getStatus());
        } else {
            logger.info("找到支付记录，支付记录ID: {}, 当前状态: {}, 支付金额: {}", 
                       paymentRecord.getId(), paymentRecord.getStatus(), paymentRecord.getPaymentAmount());
        }

        // 更新支付状态
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            logger.info("支付成功，更新支付记录和订单状态");
            
            paymentRecord.setStatus("SUCCESS");
            paymentRecord.setTransactionId(tradeNo);
            paymentRecordRepository.save(paymentRecord);
            logger.info("支付记录已更新为成功状态");

            // 更新订单支付状态
            order.setPaymentStatus(RentOrder.PaymentStatus.PAID);
            
            // 如果支付的是全款，更新订单状态为已确认
            if (order.getDeposit() == null || order.getDeposit().compareTo(order.getTotalAmount()) == 0) {
                logger.info("支付的是全款，更新订单状态为已确认");
                order.setOrderStatus(RentOrder.OrderStatus.CONFIRMED);
            }
            
            RentOrder savedOrder = orderRepository.save(order);
            logger.info("订单状态已更新：orderStatus={}, paymentStatus={}", 
                       savedOrder.getOrderStatus(), savedOrder.getPaymentStatus());

            // 更新相关预约状态为已完成
            try {
                // 根据房屋ID、租客ID和房东ID查找待处理的预约
                ViewAppointment appointment = appointmentRepository.findByHouseIdAndTenantIdAndLandlordIdAndStatus(
                        order.getHouseId(), order.getTenantId(), order.getLandlordId(), 
                        ViewAppointment.AppointmentStatus.PENDING);
                
                if (appointment != null) {
                    appointment.setStatus(ViewAppointment.AppointmentStatus.COMPLETED);
                    appointment.setOrderId(order.getId()); // 设置关联的订单ID
                    appointmentRepository.save(appointment);
                    logger.info("预约状态已更新为已完成：appointmentId={}, orderId={}", 
                               appointment.getId(), order.getId());
                } else {
                    logger.info("未找到与订单相关的待处理预约：orderId={}, houseId={}, tenantId={}, landlordId={}", 
                               order.getId(), order.getHouseId(), order.getTenantId(), order.getLandlordId());
                }
            } catch (Exception e) {
                logger.error("更新预约状态失败：orderId={}, error={}", order.getId(), e.getMessage());
            }

            return true;
        } else {
            logger.info("支付失败，更新支付记录状态为失败，tradeStatus: {}", tradeStatus);
            paymentRecord.setStatus("FAILURE");
            paymentRecord.setTransactionId(tradeNo);
            paymentRecord.setRemark("支付失败，状态：" + tradeStatus);
            paymentRecordRepository.save(paymentRecord);
            return false;
        }
    }
    
    /**
     * 退款订单
     */
    @Transactional
    public boolean refundOrder(Long orderId, String refundReason) throws AlipayApiException {
        logger.info("开始退款操作，orderId: {}, refundReason: {}", orderId, refundReason);
        
        // 查找订单
        RentOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("退款失败：订单不存在，orderId: {}", orderId);
                    return new RuntimeException("订单不存在");
                });
        
        logger.info("找到订单，订单ID: {}, 订单号: {}, 当前支付状态: {}, 订单状态: {}", 
                   order.getId(), order.getOrderNo(), order.getPaymentStatus(), order.getOrderStatus());
        
        // 验证订单是否已支付
        if (order.getPaymentStatus() != RentOrder.PaymentStatus.PAID) {
            logger.error("退款失败：订单未支付，orderId: {}, orderNo: {}, paymentStatus: {}", 
                        order.getId(), order.getOrderNo(), order.getPaymentStatus());
            throw new RuntimeException("订单未支付，无法退款");
        }
        
        // 验证订单是否已退款
        if (order.getPaymentStatus() == RentOrder.PaymentStatus.REFUNDED) {
            logger.error("退款失败：订单已退款，orderId: {}, orderNo: {}", 
                        order.getId(), order.getOrderNo());
            throw new RuntimeException("订单已退款");
        }
        
        // 查找支付记录
        List<PaymentRecord> paymentRecords = paymentRecordRepository.findByOrderId(order.getId());
        PaymentRecord paymentRecord = paymentRecords.isEmpty() ? null : paymentRecords.get(0);
        if (paymentRecord == null) {
            logger.error("退款失败：支付记录不存在，orderId: {}", order.getId());
            throw new RuntimeException("支付记录不存在，无法退款");
        }
        
        logger.info("找到支付记录，支付记录ID: {}, 支付金额: {}, 交易ID: {}", 
                   paymentRecord.getId(), paymentRecord.getPaymentAmount(), paymentRecord.getTransactionId());
        
        // 生成退款请求号，用于部分退款和多次退款
        String outRequestNo = "REFUND_" + order.getOrderNo() + "_" + System.currentTimeMillis();
        
        // 调用支付宝退款API
        boolean refundSuccess = alipayUtil.refund(
                order.getOrderNo(), 
                paymentRecord.getTransactionId(), 
                paymentRecord.getPaymentAmount().setScale(2).toString(), 
                refundReason, 
                outRequestNo);
        
        if (refundSuccess) {
            logger.info("支付宝退款成功，orderId: {}, orderNo: {}", order.getId(), order.getOrderNo());
            
            // 更新支付记录状态
            paymentRecord.setStatus("REFUNDED");
            paymentRecord.setRemark("退款成功，退款原因：" + refundReason);
            paymentRecordRepository.save(paymentRecord);
            logger.info("支付记录状态已更新为退款成功");
            
            // 更新订单支付状态为已退款
            order.setPaymentStatus(RentOrder.PaymentStatus.REFUNDED);
            
            // 如果订单状态是已确认或进行中，更新为已取消
            if (order.getOrderStatus() == RentOrder.OrderStatus.CONFIRMED || 
                order.getOrderStatus() == RentOrder.OrderStatus.ACTIVE) {
                order.setOrderStatus(RentOrder.OrderStatus.CANCELLED);
                logger.info("订单状态已更新为已取消");
            }
            
            orderRepository.save(order);
            logger.info("订单支付状态已更新为已退款");
            
            return true;
        } else {
            logger.error("支付宝退款失败，orderId: {}, orderNo: {}", order.getId(), order.getOrderNo());
            throw new RuntimeException("支付宝退款失败");
        }
    }
}

