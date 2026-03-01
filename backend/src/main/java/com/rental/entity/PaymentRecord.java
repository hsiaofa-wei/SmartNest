package com.rental.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_record")
@Data
@EntityListeners(AuditingEntityListener.class)
public class PaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "payment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_type", nullable = false, length = 20)
    private String paymentType; // deposit: 定金, full: 全款

    @Column(name = "payment_time")
    @CreatedDate
    private LocalDateTime paymentTime;

    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "SUCCESS"; // SUCCESS: 成功, FAILURE: 失败

    @Column(name = "remark", length = 500)
    private String remark;
}