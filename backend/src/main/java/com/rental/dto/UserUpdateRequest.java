package com.rental.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String phone;
    private String avatarUrl;
    private String address;
    private String realName;
    private String idCard;
}

