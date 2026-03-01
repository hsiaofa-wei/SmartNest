package com.rental.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "house")
@Data
@EntityListeners(AuditingEntityListener.class)
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "landlord_id", nullable = false)
    private Long landlordId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal area;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(name = "province", length = 50)
    private String province;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "district", length = 50)
    private String district;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HouseStatus status = HouseStatus.AVAILABLE;

    @Column(name = "admin_audit_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditStatus adminAuditStatus = AuditStatus.PENDING;

    @Column(name = "audit_remark", length = 500)
    private String auditRemark;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Transient
    private String mainImageUrl;

    public enum HouseStatus {
        AVAILABLE, RENTED, OFFLINE
    }

    public enum AuditStatus {
        PENDING, APPROVED, REJECTED
    }
}

