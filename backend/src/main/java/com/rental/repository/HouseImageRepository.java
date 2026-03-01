package com.rental.repository;

import com.rental.entity.HouseImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseImageRepository extends JpaRepository<HouseImage, Long> {
    List<HouseImage> findByHouseIdOrderBySortOrderAsc(Long houseId);
    void deleteByHouseId(Long houseId);
}

