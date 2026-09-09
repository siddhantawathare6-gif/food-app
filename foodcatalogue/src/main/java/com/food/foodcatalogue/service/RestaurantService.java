package com.food.foodcatalogue.service;

import com.food.foodcatalogue.dto.Restaurant;
import com.food.foodcatalogue.exception.FoodCatalogueServiceException;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);

    private static final String RESTAURANT_SERVICE_URL = "http://RESTAURANT-SERVICE/restaurant";

    @Autowired
    RestTemplate restTemplate;

    @Retry(name = "restaurantServiceRetry", fallbackMethod = "fetchRestaurantFallback")
    public Restaurant fetchRestaurantDetailsFromRestaurantMS(Integer restaurantId) {
        return restTemplate.getForObject(RESTAURANT_SERVICE_URL + "/fetchById/" + restaurantId, Restaurant.class);
    }

    public Restaurant createRestaurant(Restaurant restaurant) {
        log.info("Calling Restaurant Service to CREATE restaurant: {}", restaurant.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Restaurant> requestEntity = new HttpEntity<>(restaurant, headers);

        ResponseEntity<Restaurant> response = restTemplate.exchange(
                RESTAURANT_SERVICE_URL + "/addRestaurant",
                HttpMethod.POST,
                requestEntity,
                Restaurant.class
        );

        log.info("Restaurant created with ID: {}", response.getBody().getId());
        return response.getBody();
    }

    public Restaurant updateRestaurant(Integer id, Restaurant restaurant) {
        log.info("Calling Restaurant Service to UPDATE restaurant: {}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Restaurant> requestEntity = new HttpEntity<>(restaurant, headers);

        // Note: You need to add PUT endpoint in Restaurant Controller
        ResponseEntity<Restaurant> response = restTemplate.exchange(
                RESTAURANT_SERVICE_URL + "/updateRestaurant/" + id,
                HttpMethod.PUT,
                requestEntity,
                Restaurant.class
        );

        return response.getBody();
    }

    public void deleteRestaurant(Integer id) {
        log.info("Calling Restaurant Service to DELETE restaurant: {}", id);

        // Note: You need to add DELETE endpoint in Restaurant Controller
        restTemplate.delete(RESTAURANT_SERVICE_URL + "/deleteRestaurant/" + id);
        log.info("Restaurant deleted: {}", id);
    }

    private Restaurant fetchRestaurantFallback(Integer restaurantId, Exception ex) {
        log.error("All retries exhausted fetching restaurant {}: {}", restaurantId, ex.getMessage());
        throw new FoodCatalogueServiceException(HttpStatus.SERVICE_UNAVAILABLE, "Restaurant service is currently " +
                "unavailable. Please try again shortly.");
    }

}
