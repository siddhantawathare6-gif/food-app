package com.food.resturant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantPageDto {
    private List<RestaurantDTO> restaurantList;
    private int pageNo;
    private int pageSize;
    private long totalElement;
    private int totalPage;
    private boolean isLast;
}
