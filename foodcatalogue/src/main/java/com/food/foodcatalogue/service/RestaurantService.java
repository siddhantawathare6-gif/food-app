package com.food.foodcatalogue.service;

import com.food.foodcatalogue.dto.Restaurant;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);

    @Autowired
    RestTemplate restTemplate;

    @Retry(name = "restaurantServiceRetry", fallbackMethod = "fetchRestaurantFallback")
    public Restaurant fetchRestaurantDetailsFromRestaurantMS(Integer restaurantId) {
        return restTemplate.getForObject("http://RESTAURANT-SERVICE/restaurant/fetchById/" + restaurantId, Restaurant.class);
    }

    private Restaurant fetchRestaurantFallback(Integer restaurantId, Exception ex) {
        log.error("All retries exhausted fetching restaurant {}: {}", restaurantId, ex.getMessage());
        throw new RuntimeException("Restaurant service is currently unavailable.");
    }

}
