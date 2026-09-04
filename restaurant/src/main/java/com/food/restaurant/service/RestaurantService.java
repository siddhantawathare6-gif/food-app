package com.food.restaurant.service;

import com.food.restaurant.dto.RestaurantDTO;
import com.food.restaurant.dto.RestaurantPageDto;

public interface RestaurantService {
    RestaurantPageDto featchAllRestaurant(int pageNo, int pageSize, String sortBy, String sortDir);

    RestaurantDTO addRestaurant(RestaurantDTO restaurantDTO);

    RestaurantDTO fetchRestaurantById(Integer id);
}
