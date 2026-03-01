package com.rental.repository;

import com.rental.entity.House;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface HouseRepository extends JpaRepository<House, Long> {
    Page<House> findByLandlordId(Long landlordId, Pageable pageable);
    
    Page<House> findByAdminAuditStatus(House.AuditStatus status, Pageable pageable);
    
    @Query("SELECT h FROM House h WHERE (:keyword IS NULL OR h.title LIKE %:keyword% OR h.description LIKE %:keyword% OR h.address LIKE %:keyword%) " +
           "AND (:minPrice IS NULL OR h.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR h.price <= :maxPrice) " +
           "AND (:province IS NULL OR h.province = :province) " +
           "AND (:city IS NULL OR h.city = :city) " +
           "AND (:district IS NULL OR h.district = :district)")
    Page<House> searchHouses(@Param("keyword") String keyword,
                            @Param("minPrice") BigDecimal minPrice,
                            @Param("maxPrice") BigDecimal maxPrice,
                            @Param("province") String province,
                            @Param("city") String city,
                            @Param("district") String district,
                            Pageable pageable);
    
    @Query("SELECT h FROM House h WHERE h.status = 'AVAILABLE' AND h.adminAuditStatus = 'APPROVED' " +
           "AND h.city = :city " +
           "ORDER BY h.viewCount DESC, h.createTime DESC")
    List<House> findRecommendedByCity(@Param("city") String city, Pageable pageable);
    
    long countByAdminAuditStatus(House.AuditStatus status);
    
    @Query("SELECT COUNT(h) FROM House h WHERE FUNCTION('MONTH', h.createTime) = :month")
    long countByCreateTimeMonth(@Param("month") int month);
}

