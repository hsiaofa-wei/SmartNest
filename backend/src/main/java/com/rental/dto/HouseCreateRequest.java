package com.rental.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class HouseCreateRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal area;
    private String address;
    private String province;
    private String city;
    private String district;
    private BigDecimal longitude;
    private BigDecimal latitude;
    
    // 详情信息
    private Integer floor;
    private Integer totalFloors;
    private String roomType;
    private String orientation;
    private String decoration;
    private Boolean hasElevator;
    private Boolean hasParking;
    private Boolean hasFurniture;
    private Boolean hasAirConditioner;
    private Boolean hasWashingMachine;
    private Boolean hasRefrigerator;
    private Boolean hasWifi;
    private String facilities;
    
    // 图片URL列表
    private List<String> imageUrls;
}

