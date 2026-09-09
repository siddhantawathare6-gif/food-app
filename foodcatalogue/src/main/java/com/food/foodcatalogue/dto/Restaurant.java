package com.food.foodcatalogue.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Restaurant {

    private Integer id;
    private String name;
    private String address;
    private String city;
    private String restaurantDescription;
    private String imageUrl; // Store image filename
    private Double rating;
    private Integer reviewCount;

}
