package com.rental.repository;

import com.rental.entity.RentOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentOrderRepository extends JpaRepository<RentOrder, Long> {
    Page<RentOrder> findByTenantId(Long tenantId, Pageable pageable);
    Page<RentOrder> findByLandlordId(Long landlordId, Pageable pageable);
    
    Optional<RentOrder> findByOrderNo(String orderNo);
    
    @Query("SELECT COUNT(ro) FROM RentOrder ro WHERE FUNCTION('MONTH', ro.createTime) = :month")
    long countByCreateTimeMonth(@Param("month") int month);
    
    // 查询已到期但状态仍为ACTIVE的订单
    List<RentOrder> findByEndDateLessThanEqualAndOrderStatus(LocalDate date, RentOrder.OrderStatus status);
}

