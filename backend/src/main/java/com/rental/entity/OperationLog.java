package com.rental.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "operation_log")
@Data
@EntityListeners(AuditingEntityListener.class)
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "operation", length = 100)
    private String operation;

    @Column(name = "method", length = 10)
    private String method;

    @Column(name = "request_url", length = 500)
    private String requestUrl;

    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    @Column(name = "execution_time")
    private Long executionTime;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}

