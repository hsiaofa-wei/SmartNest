package com.rental.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rent_order")
@Data
@EntityListeners(AuditingEntityListener.class)
public class RentOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", unique = true, nullable = false, length = 50)
    private String orderNo;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "landlord_id", nullable = false)
    private Long landlordId;

    @Column(name = "house_id", nullable = false)
    private Long houseId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "monthly_rent", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "deposit", precision = 10, scale = 2)
    private BigDecimal deposit;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "contract_url", length = 500)
    private String contractUrl;

    @Column(name = "remark", length = 500)
    private String remark;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    public enum OrderStatus {
        PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED
    }

    public enum PaymentStatus {
        UNPAID, PAID, REFUNDED
    }
}

