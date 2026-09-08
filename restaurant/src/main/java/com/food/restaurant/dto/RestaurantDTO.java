package com.food.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantDTO {

    private Integer id;
    private String name;
    private String address;
    private String city;
    private String restaurantDescription;
    private String imageUrl; // Add this field
    private Double rating; // Add rating
    private Integer reviewCount; // Add review count

}
