package com.rental;

import com.rental.entity.House;
import com.rental.repository.HouseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
public class DatabaseTest {

    @Autowired
    private HouseRepository houseRepository;

    @Test
    public void testSearchApprovedHouses() {
        System.out.println("Testing search for approved and available houses...");
        
        // 测试searchHouses方法，使用与前端相同的条件
        Page<House> houses = houseRepository.searchHouses(
            null, // keyword
            null, // minPrice
            null, // maxPrice
            null, // province
            null, // city
            null, // district
            PageRequest.of(0, 10)
        );
        
        System.out.println("Total approved and available houses: " + houses.getTotalElements());
        System.out.println("House list:");
        for (House house : houses.getContent()) {
            System.out.println("- ID: " + house.getId() + ", Title: " + house.getTitle() + ", Status: " + house.getStatus() + ", Audit Status: " + house.getAdminAuditStatus());
        }
    }
    
    @Test
    public void testAllHouses() {
        System.out.println("Testing query for all houses...");
        
        Page<House> allHouses = houseRepository.findAll(PageRequest.of(0, 20));
        
        System.out.println("Total houses in database: " + allHouses.getTotalElements());
        System.out.println("House details:");
        for (House house : allHouses.getContent()) {
            System.out.println("- ID: " + house.getId() + ", Title: " + house.getTitle() + ", Status: " + house.getStatus() + ", Audit Status: " + house.getAdminAuditStatus() + ", Landlord ID: " + house.getLandlordId());
        }
    }
}
