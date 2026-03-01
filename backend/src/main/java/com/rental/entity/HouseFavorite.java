package com.rental.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "house_favorite")
@Data
@EntityListeners(AuditingEntityListener.class)
public class HouseFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "house_id", nullable = false)
    private Long houseId;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}

