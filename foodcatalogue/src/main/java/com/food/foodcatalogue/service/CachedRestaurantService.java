package com.food.foodcatalogue.service;

import com.food.foodcatalogue.dto.Restaurant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CachedRestaurantService {

    private static final Logger log = LoggerFactory.getLogger(CachedRestaurantService.class);

    @Autowired
    RestaurantService restaurantService;

    @Cacheable(value = "foodCatalogueRestaurant", key = "#restaurantId")
    public Restaurant getRestaurantDetails(Integer restaurantId) {
        log.info("Cache miss - calling RestaurantService (with retry) for restaurantId: {}", restaurantId);
        return restaurantService.fetchRestaurantDetailsFromRestaurantMS(restaurantId);
    }


}
