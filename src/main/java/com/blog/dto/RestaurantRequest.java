package com.blog.dto;

import java.util.List;

/**
 * Class Name: QuestionRequest
 * Package: dto
 * Description:
 * author:
 * Create: 2025/1/3
 * Version: 1.0
 */
public class RestaurantRequest {
    private String name;
    private String address;
    private List<String> openingHours;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(List<String> openingHours) {
        this.openingHours = openingHours;
    }
}
