package com.rental.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "view_appointment")
@Data
@EntityListeners(AuditingEntityListener.class)
public class ViewAppointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "landlord_id", nullable = false)
    private Long landlordId;

    @Column(name = "house_id", nullable = false)
    private Long houseId;

    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(name = "landlord_reply", length = 500)
    private String landlordReply;

    @Column(name = "order_id")
    private Long orderId;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    public enum AppointmentStatus {
        PENDING, CONFIRMED, REJECTED, COMPLETED, CANCELLED
    }
}

