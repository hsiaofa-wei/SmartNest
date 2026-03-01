package com.rental.repository;

import com.rental.entity.HouseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseDetailRepository extends JpaRepository<HouseDetail, Long> {
    Optional<HouseDetail> findByHouseId(Long houseId);
}

