package com.food.restaurant.service;

import com.food.restaurant.dto.RestaurantDTO;
import com.food.restaurant.dto.RestaurantPageDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface RestaurantService {
    RestaurantPageDto featchAllRestaurant(int pageNo, int pageSize, String sortBy, String sortDir);

    RestaurantDTO addRestaurant(RestaurantDTO restaurantDTO);

    RestaurantDTO fetchRestaurantById(Integer id);

    String uploadRestaurantImage(Integer restaurantId, MultipartFile file) ;

    byte[] getRestaurantImage(Integer restaurantId);
}
