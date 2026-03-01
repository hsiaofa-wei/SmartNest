package com.rental.controller;

import com.rental.entity.House;
import com.rental.entity.HouseDetail;
import com.rental.entity.HouseImage;
import com.rental.service.HouseService;
import com.rental.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/houses")
@CrossOrigin
public class HouseController {
    @Autowired
    private HouseService houseService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchHouses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 前端可能传入空字符串，会导致精确匹配失败，这里统一转为 null
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        if (province != null && province.trim().isEmpty()) {
            province = null;
        }
        if (city != null && city.trim().isEmpty()) {
            city = null;
        }
        if (district != null && district.trim().isEmpty()) {
            district = null;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<House> houses = houseService.searchHouses(keyword, minPrice, maxPrice, province, city, district, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", houses.getContent());
        response.put("totalElements", houses.getTotalElements());
        response.put("totalPages", houses.getTotalPages());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<House>> getRecommendedHouses(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(0, size);
        List<House> houses = houseService.getRecommendedHouses(city, pageable);
        return ResponseEntity.ok(houses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getHouseDetail(@PathVariable Long id) {
        houseService.incrementViewCount(id);
        House house = houseService.getHouseById(id)
                .orElseThrow(() -> new RuntimeException("房源不存在"));
        HouseDetail detail = houseService.getHouseDetail(id);
        List<HouseImage> images = houseService.getHouseImages(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("house", house);
        response.put("detail", detail);
        response.put("images", images);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<Map<String, Object>> getPublicHouseDetail(@PathVariable Long id) {
        House house = houseService.getHouseById(id)
                .orElseThrow(() -> new RuntimeException("房源不存在"));
        if (house.getAdminAuditStatus() != House.AuditStatus.APPROVED || 
            house.getStatus() != House.HouseStatus.AVAILABLE) {
            throw new RuntimeException("房源不可查看");
        }
        houseService.incrementViewCount(id);
        HouseDetail detail = houseService.getHouseDetail(id);
        List<HouseImage> images = houseService.getHouseImages(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("house", house);
        response.put("detail", detail);
        response.put("images", images);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('LANDLORD') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createHouse(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String token) {
        try {
            Long landlordId = getUserIdFromToken(token);
            
            House house = new House();
            house.setLandlordId(landlordId);
            house.setTitle((String) request.get("title"));
            house.setDescription((String) request.get("description"));
            house.setPrice(new java.math.BigDecimal(request.get("price").toString()));
            house.setArea(new java.math.BigDecimal(request.get("area").toString()));
            house.setAddress((String) request.get("address"));
            house.setProvince((String) request.get("province"));
            house.setCity((String) request.get("city"));
            house.setDistrict((String) request.get("district"));
            if (request.get("longitude") != null) {
                house.setLongitude(new java.math.BigDecimal(request.get("longitude").toString()));
            }
            if (request.get("latitude") != null) {
                house.setLatitude(new java.math.BigDecimal(request.get("latitude").toString()));
            }
            house.setStatus(House.HouseStatus.AVAILABLE);
            house.setAdminAuditStatus(House.AuditStatus.PENDING);
            
            HouseDetail detail = null;
            if (request.containsKey("floor") || request.containsKey("roomType")) {
                detail = new HouseDetail();
                if (request.get("floor") != null) {
                    detail.setFloor(Integer.parseInt(request.get("floor").toString()));
                }
                if (request.get("totalFloors") != null) {
                    detail.setTotalFloors(Integer.parseInt(request.get("totalFloors").toString()));
                }
                detail.setRoomType((String) request.get("roomType"));
                detail.setOrientation((String) request.get("orientation"));
                detail.setDecoration((String) request.get("decoration"));
                if (request.get("hasElevator") != null) {
                    detail.setHasElevator((Boolean) request.get("hasElevator"));
                }
                if (request.get("hasParking") != null) {
                    detail.setHasParking((Boolean) request.get("hasParking"));
                }
                if (request.get("hasFurniture") != null) {
                    detail.setHasFurniture((Boolean) request.get("hasFurniture"));
                }
                if (request.get("hasAirConditioner") != null) {
                    detail.setHasAirConditioner((Boolean) request.get("hasAirConditioner"));
                }
                if (request.get("hasWashingMachine") != null) {
                    detail.setHasWashingMachine((Boolean) request.get("hasWashingMachine"));
                }
                if (request.get("hasRefrigerator") != null) {
                    detail.setHasRefrigerator((Boolean) request.get("hasRefrigerator"));
                }
                if (request.get("hasWifi") != null) {
                    detail.setHasWifi((Boolean) request.get("hasWifi"));
                }
                detail.setFacilities((String) request.get("facilities"));
            }
            
            House savedHouse = houseService.createHouse(house, detail);
            
            // 保存图片
            if (request.get("imageUrls") != null) {
                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) request.get("imageUrls");
                List<HouseImage> images = new java.util.ArrayList<>();
                for (int i = 0; i < imageUrls.size(); i++) {
                    HouseImage image = new HouseImage();
                    image.setHouseId(savedHouse.getId());
                    image.setImageUrl(imageUrls.get(i));
                    image.setIsMain(i == 0);
                    image.setSortOrder(i);
                    images.add(image);
                }
                houseService.saveHouseImages(savedHouse.getId(), images);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedHouse.getId());
            response.put("message", "房源发布成功，等待审核");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "发布房源失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getClaimsFromToken(token).get("userId", Long.class);
        }
        throw new RuntimeException("未授权");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LANDLORD') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateHouse(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String token) {
        try {
            Long landlordId = getUserIdFromToken(token);
            
            House house = houseService.getHouseById(id)
                    .orElseThrow(() -> new RuntimeException("房源不存在"));
            
            // 检查权限
            if (!house.getLandlordId().equals(landlordId)) {
                throw new RuntimeException("无权修改此房源");
            }
            
            if (request.get("title") != null) {
                house.setTitle((String) request.get("title"));
            }
            if (request.get("description") != null) {
                house.setDescription((String) request.get("description"));
            }
            if (request.get("price") != null) {
                house.setPrice(new java.math.BigDecimal(request.get("price").toString()));
            }
            if (request.get("area") != null) {
                house.setArea(new java.math.BigDecimal(request.get("area").toString()));
            }
            if (request.get("address") != null) {
                house.setAddress((String) request.get("address"));
            }
            if (request.get("province") != null) {
                house.setProvince((String) request.get("province"));
            }
            if (request.get("city") != null) {
                house.setCity((String) request.get("city"));
            }
            if (request.get("district") != null) {
                house.setDistrict((String) request.get("district"));
            }
            if (request.get("longitude") != null) {
                house.setLongitude(new java.math.BigDecimal(request.get("longitude").toString()));
            }
            if (request.get("latitude") != null) {
                house.setLatitude(new java.math.BigDecimal(request.get("latitude").toString()));
            }
            if (request.get("status") != null) {
                house.setStatus(House.HouseStatus.valueOf(request.get("status").toString()));
            }
            
            // 更新详情
            HouseDetail detail = houseService.getHouseDetail(id);
            if (detail == null) {
                detail = new HouseDetail();
                detail.setHouseId(id);
            }
            
            if (request.get("floor") != null) {
                detail.setFloor(Integer.parseInt(request.get("floor").toString()));
            }
            if (request.get("totalFloors") != null) {
                detail.setTotalFloors(Integer.parseInt(request.get("totalFloors").toString()));
            }
            if (request.get("roomType") != null) {
                detail.setRoomType((String) request.get("roomType"));
            }
            if (request.get("orientation") != null) {
                detail.setOrientation((String) request.get("orientation"));
            }
            if (request.get("decoration") != null) {
                detail.setDecoration((String) request.get("decoration"));
            }
            if (request.get("hasElevator") != null) {
                detail.setHasElevator((Boolean) request.get("hasElevator"));
            }
            if (request.get("hasParking") != null) {
                detail.setHasParking((Boolean) request.get("hasParking"));
            }
            if (request.get("hasFurniture") != null) {
                detail.setHasFurniture((Boolean) request.get("hasFurniture"));
            }
            if (request.get("hasAirConditioner") != null) {
                detail.setHasAirConditioner((Boolean) request.get("hasAirConditioner"));
            }
            if (request.get("hasWashingMachine") != null) {
                detail.setHasWashingMachine((Boolean) request.get("hasWashingMachine"));
            }
            if (request.get("hasRefrigerator") != null) {
                detail.setHasRefrigerator((Boolean) request.get("hasRefrigerator"));
            }
            if (request.get("hasWifi") != null) {
                detail.setHasWifi((Boolean) request.get("hasWifi"));
            }
            if (request.get("facilities") != null) {
                detail.setFacilities((String) request.get("facilities"));
            }
            
            houseService.updateHouse(house, detail);
            
            // 更新图片
            if (request.get("imageUrls") != null) {
                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) request.get("imageUrls");
                List<HouseImage> images = new java.util.ArrayList<>();
                for (int i = 0; i < imageUrls.size(); i++) {
                    HouseImage image = new HouseImage();
                    image.setHouseId(id);
                    image.setImageUrl(imageUrls.get(i));
                    image.setIsMain(i == 0);
                    image.setSortOrder(i);
                    images.add(image);
                }
                houseService.saveHouseImages(id, images);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "房源更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "更新房源失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LANDLORD') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteHouse(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            House house = houseService.getHouseById(id)
                    .orElseThrow(() -> new RuntimeException("房源不存在"));
            
            // 检查权限（管理员可以删除任何房源，房东只能删除自己的）
            String role = jwtUtil.getClaimsFromToken(token.substring(7)).get("role", String.class);
            if (!"ADMIN".equals(role) && !house.getLandlordId().equals(userId)) {
                throw new RuntimeException("无权删除此房源");
            }
            
            houseService.deleteHouse(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "房源删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("message", "删除房源失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}

