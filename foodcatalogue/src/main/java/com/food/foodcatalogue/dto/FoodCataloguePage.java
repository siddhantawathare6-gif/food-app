package com.food.foodcatalogue.dto;

import com.food.foodcatalogue.entity.FoodItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodCataloguePage {

    private Restaurant restaurant;
    private List<FoodItemDTO> foodItemsList;

}
