package com.food.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"veg"})
public class FoodItemsDTO {

    private Integer id;
    private String itemName;
    private String itemDescription;
    @JsonProperty("isVeg")
    private boolean isVeg;
    private Long price;
    private Integer restaurantId;
    private Integer quantity;


}
