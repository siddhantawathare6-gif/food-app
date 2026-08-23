package com.food.resturant.service;

import com.food.resturant.dto.RestaurantDTO;
import com.food.resturant.dto.RestaurantPageDto;

import java.util.List;

public interface RestaurantService {
    RestaurantPageDto featchAllRestaurant(int pageNo, int pageSize, String sortBy, String sortDir);

    RestaurantDTO addRestaurant(RestaurantDTO restaurantDTO);

    RestaurantDTO fetchRestaurantById(Integer id);
}
