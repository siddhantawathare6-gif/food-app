package com.food.foodcatalogue.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantWithFoodItemsDTO {
    private Restaurant restaurant;
    private List<FoodItemDTO> foodItems;
}
