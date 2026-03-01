package com.rental.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "house_detail")
@Data
public class HouseDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_id", unique = true, nullable = false)
    private Long houseId;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "total_floors")
    private Integer totalFloors;

    @Column(name = "room_type", length = 50)
    private String roomType; // 一室一厅、两室一厅等

    @Column(name = "orientation", length = 20)
    private String orientation; // 朝向：南、北、东、西等

    @Column(name = "decoration", length = 50)
    private String decoration; // 装修情况：精装、简装、毛坯

    @Column(name = "has_elevator")
    private Boolean hasElevator = false;

    @Column(name = "has_parking")
    private Boolean hasParking = false;

    @Column(name = "has_furniture")
    private Boolean hasFurniture = false;

    @Column(name = "has_air_conditioner")
    private Boolean hasAirConditioner = false;

    @Column(name = "has_washing_machine")
    private Boolean hasWashingMachine = false;

    @Column(name = "has_refrigerator")
    private Boolean hasRefrigerator = false;

    @Column(name = "has_wifi")
    private Boolean hasWifi = false;

    @Column(name = "facilities", columnDefinition = "TEXT")
    private String facilities; // JSON格式存储其他设施
}

