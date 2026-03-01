package com.rental.dto;

import lombok.Data;

@Data
public class CaptchaResponse {
    private String key;
    private String image; // Base64编码的图片
}

