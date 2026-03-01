package com.rental.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "house_image")
@Data
@EntityListeners(AuditingEntityListener.class)
public class HouseImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_id", nullable = false)
    private Long houseId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "is_main")
    private Boolean isMain = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}

