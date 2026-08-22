package com.food.resturant.service;

import com.food.resturant.dto.RestaurantDTO;

import java.util.List;

public interface RestaurantService {
    List<RestaurantDTO> featchAllRestaurant();

    RestaurantDTO addRestaurant(RestaurantDTO restaurantDTO);

    RestaurantDTO fetchRestaurantById(Integer id);
}
