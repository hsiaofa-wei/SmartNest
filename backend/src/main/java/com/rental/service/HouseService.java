package com.rental.service;

import com.rental.entity.House;
import com.rental.entity.HouseDetail;
import com.rental.entity.HouseImage;
import com.rental.repository.HouseDetailRepository;
import com.rental.repository.HouseImageRepository;
import com.rental.repository.HouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class HouseService {
    @Autowired
    private HouseRepository houseRepository;
    
    @Autowired
    private HouseDetailRepository houseDetailRepository;
    
    @Autowired
    private HouseImageRepository houseImageRepository;

    public Page<House> searchHouses(String keyword, BigDecimal minPrice, BigDecimal maxPrice, 
                                    String province, String city, String district, Pageable pageable) {
        Page<House> page = houseRepository.searchHouses(keyword, minPrice, maxPrice, province, city, district, pageable);
        // 为列表数据填充首图，便于前端展示
        page.getContent().forEach(house -> {
            List<HouseImage> images = houseImageRepository.findByHouseIdOrderBySortOrderAsc(house.getId());
            if (!images.isEmpty()) {
                house.setMainImageUrl(images.get(0).getImageUrl());
            }
        });
        return page;
    }

    public List<House> getRecommendedHouses(String city, Pageable pageable) {
        return houseRepository.findRecommendedByCity(city, pageable);
    }

    public Optional<House> getHouseById(Long id) {
        return houseRepository.findById(id);
    }

    public HouseDetail getHouseDetail(Long houseId) {
        return houseDetailRepository.findByHouseId(houseId).orElse(null);
    }

    public List<HouseImage> getHouseImages(Long houseId) {
        return houseImageRepository.findByHouseIdOrderBySortOrderAsc(houseId);
    }

    @Transactional
    public House createHouse(House house, HouseDetail detail) {
        House savedHouse = houseRepository.save(house);
        if (detail != null) {
            detail.setHouseId(savedHouse.getId());
            houseDetailRepository.save(detail);
        }
        return savedHouse;
    }

    @Transactional
    public House updateHouse(House house, HouseDetail detail) {
        House savedHouse = houseRepository.save(house);
        if (detail != null) {
            detail.setHouseId(savedHouse.getId());
            houseDetailRepository.save(detail);
        }
        return savedHouse;
    }

    public Page<House> getHousesByLandlord(Long landlordId, Pageable pageable) {
        return houseRepository.findByLandlordId(landlordId, pageable);
    }

    public Page<House> getAllHouses(Pageable pageable) {
        Page<House> page = houseRepository.findAll(pageable);
        page.getContent().forEach(this::fillMainImage);
        return page;
    }

    public Page<House> getHousesByAuditStatus(House.AuditStatus status, Pageable pageable) {
        Page<House> page = houseRepository.findByAdminAuditStatus(status, pageable);
        page.getContent().forEach(this::fillMainImage);
        return page;
    }

    public Page<House> getPendingAuditHouses(Pageable pageable) {
        return houseRepository.findByAdminAuditStatus(House.AuditStatus.PENDING, pageable);
    }

    @Transactional
    public void auditHouse(Long houseId, House.AuditStatus status, String remark) {
        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new RuntimeException("房源不存在"));
        house.setAdminAuditStatus(status);
        house.setAuditRemark(remark);
        
        // 当审核通过时，自动将房源状态设置为可用
        if (status == House.AuditStatus.APPROVED) {
            house.setStatus(House.HouseStatus.AVAILABLE);
        }
        
        houseRepository.save(house);
    }

    public void incrementViewCount(Long houseId) {
        House house = houseRepository.findById(houseId).orElse(null);
        if (house != null) {
            house.setViewCount(house.getViewCount() + 1);
            houseRepository.save(house);
        }
    }
    
    @Transactional
    public void deleteHouse(Long houseId) {
        // 删除关联的图片
        houseImageRepository.deleteByHouseId(houseId);
        // 删除关联的详情
        houseDetailRepository.findByHouseId(houseId).ifPresent(detail -> 
            houseDetailRepository.delete(detail));
        // 删除房源
        houseRepository.deleteById(houseId);
    }
    
    @Transactional
    public void saveHouseImages(Long houseId, List<HouseImage> images) {
        // 删除旧图片
        houseImageRepository.deleteByHouseId(houseId);
        // 保存新图片
        for (HouseImage image : images) {
            image.setHouseId(houseId);
            houseImageRepository.save(image);
        }
    }

    private void fillMainImage(House house) {
        if (house == null) return;
        List<HouseImage> images = houseImageRepository.findByHouseIdOrderBySortOrderAsc(house.getId());
        if (!images.isEmpty()) {
            house.setMainImageUrl(images.get(0).getImageUrl());
        }
    }
}

