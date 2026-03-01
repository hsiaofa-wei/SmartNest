package com.rental.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ShapeCaptchaResponse {
    private String key;
    private String targetShape;
    private List<Shape> shapes;
    private int canvasWidth;
    private int canvasHeight;

    @Data
    public static class Shape {
        private String type; // circle, square, triangle, star
        private int x;
        private int y;
        private int size;
        private String color;
    }
}