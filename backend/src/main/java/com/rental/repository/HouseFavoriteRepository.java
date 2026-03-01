package com.rental.repository;

import com.rental.entity.HouseFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseFavoriteRepository extends JpaRepository<HouseFavorite, Long> {
    Optional<HouseFavorite> findByTenantIdAndHouseId(Long tenantId, Long houseId);
    boolean existsByTenantIdAndHouseId(Long tenantId, Long houseId);
    Page<HouseFavorite> findByTenantId(Long tenantId, Pageable pageable);
}

