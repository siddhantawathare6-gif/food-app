package com.food.foodcatalogue.service;

import com.food.foodcatalogue.dto.FoodCataloguePage;
import com.food.foodcatalogue.dto.FoodItemDTO;
import com.food.foodcatalogue.dto.Restaurant;
import com.food.foodcatalogue.entity.FoodItem;
import com.food.foodcatalogue.mapper.FoodItemMapper;
import com.food.foodcatalogue.repository.FoodItemRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class FoodCatalogueService {

    private static final Logger log = LoggerFactory.getLogger(FoodCatalogueService.class);

    @Autowired
    FoodItemRepo foodItemRepo;

    @Autowired
    RestTemplate restTemplate;


    public FoodItemDTO addFoodItem(FoodItemDTO foodItemDTO) {

        log.info("Adding new food item - name: '{}', price: {}, restaurantId: {}",
                foodItemDTO.getItemName(),
                foodItemDTO.getPrice(),
                foodItemDTO.getRestaurantId());

        log.debug("Full food item DTO: {}", foodItemDTO);
        FoodItem foodItemSavedInDB = foodItemRepo.save(FoodItemMapper.INSTANCE.mapFoodItemDTOToFoodItem(foodItemDTO));

        log.info("Food item added successfully - id: {}, name: '{}', price: {}, restaurantId: {}",
                foodItemSavedInDB.getId(),
                foodItemSavedInDB.getItemName(),
                foodItemSavedInDB.getPrice(),
                foodItemSavedInDB.getRestaurantId());

        log.debug("Full saved food item DTO: {}", foodItemSavedInDB);
        return FoodItemMapper.INSTANCE.mapFoodItemToFoodItemDto(foodItemSavedInDB);
    }

    public FoodCataloguePage fetchFoodCataloguePageDetails(Integer restaurantId) {

        log.info("Fetching food catalogue details for restaurantId: {}", restaurantId);

        List<FoodItem> foodItemList =  fetchFoodItemList(restaurantId);
        Restaurant restaurant = fetchRestaurantDetailsFromRestaurantMS(restaurantId);

        if (restaurant != null) {
            log.info("Restaurant details fetched successfully - id: {}, name: '{}', city: '{}'",
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getCity());
            log.debug("Full restaurant details: {}", restaurant);
        } else {
            log.warn("Restaurant not found for ID: {}", restaurantId);
            throw new RuntimeException("Restaurant not found with ID: " + restaurantId);
        }

        return createFoodCataloguePage(FoodItemMapper.INSTANCE.mapFoodItemListToFoodItemDtoList(foodItemList), restaurant);
    }

    private FoodCataloguePage createFoodCataloguePage(List<FoodItemDTO> foodItemList, Restaurant restaurant) {
        FoodCataloguePage foodCataloguePage = new FoodCataloguePage();
        foodCataloguePage.setFoodItemsList(foodItemList);
        foodCataloguePage.setRestaurant(restaurant);
        return foodCataloguePage;
    }

    private Restaurant fetchRestaurantDetailsFromRestaurantMS(Integer restaurantId) {
        return restTemplate.getForObject("http://RESTAURANT-SERVICE/restaurant/fetchById/"+restaurantId, Restaurant.class);
    }

    private List<FoodItem> fetchFoodItemList(Integer restaurantId) {
        return foodItemRepo.findByRestaurantId(restaurantId);
    }
}