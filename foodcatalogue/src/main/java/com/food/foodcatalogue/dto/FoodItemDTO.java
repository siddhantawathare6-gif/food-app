package com.food.foodcatalogue.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"veg"})
public class FoodItemDTO {

    private Integer id;
    private String itemName;
    private String itemDescription;
    @JsonProperty("isVeg")
    private boolean isVeg;
    private Long price;
    private Integer restaurantId;
    private Integer quantity ;

}
