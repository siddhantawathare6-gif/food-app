package com.food.foodcatalogue.service;

import com.food.foodcatalogue.dto.FoodCataloguePage;
import com.food.foodcatalogue.dto.FoodItemDTO;
import com.food.foodcatalogue.dto.Restaurant;
import com.food.foodcatalogue.dto.RestaurantWithFoodItemsDTO;
import com.food.foodcatalogue.entity.FoodItem;
import com.food.foodcatalogue.exception.FoodCatalogueServiceException;
import com.food.foodcatalogue.mapper.FoodItemMapper;
import com.food.foodcatalogue.repository.FoodItemRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FoodCatalogueService {

    private static final Logger log = LoggerFactory.getLogger(FoodCatalogueService.class);

    @Autowired
    FoodItemRepo foodItemRepo;

    @Autowired
    CachedRestaurantService cachedRestaurantService;

    @Autowired
    RestaurantService restaurantService;


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

        List<FoodItem> foodItemList = fetchFoodItemList(restaurantId);
        Restaurant restaurant = cachedRestaurantService.getRestaurantDetails(restaurantId);

        if (restaurant != null) {
            log.info("Restaurant details fetched successfully - id: {}, name: '{}', city: '{}'",
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getCity());
            log.debug("Full restaurant details: {}", restaurant);
        } else {
            log.warn("Restaurant not found for ID: {}", restaurantId);
            throw new FoodCatalogueServiceException(HttpStatus.NOT_FOUND, "Restaurant not found with ID: " + restaurantId);
        }

        return createFoodCataloguePage(FoodItemMapper.INSTANCE.mapFoodItemListToFoodItemDtoList(foodItemList), restaurant);
    }

    private FoodCataloguePage createFoodCataloguePage(List<FoodItemDTO> foodItemList, Restaurant restaurant) {
        FoodCataloguePage foodCataloguePage = new FoodCataloguePage();
        foodCataloguePage.setFoodItemsList(foodItemList);
        foodCataloguePage.setRestaurant(restaurant);
        return foodCataloguePage;
    }

    private List<FoodItem> fetchFoodItemList(Integer restaurantId) {
        return foodItemRepo.findByRestaurantId(restaurantId);
    }

    @Transactional
    public RestaurantWithFoodItemsDTO addRestaurantWithFoodItems(RestaurantWithFoodItemsDTO request) {
        log.info("Adding restaurant with food items - Restaurant: {}, Food items: {}",
                request.getRestaurant().getName(),
                request.getFoodItems() != null ? request.getFoodItems().size() : 0);

        // Step 1: Create restaurant via Restaurant Service
        Restaurant createdRestaurant = restaurantService.createRestaurant(request.getRestaurant());
        log.info("Restaurant created with ID: {}", createdRestaurant.getId());

        // Step 2: Save food items with restaurant ID
        if (request.getFoodItems() != null && !request.getFoodItems().isEmpty()) {
            List<FoodItemDTO> foodItems = request.getFoodItems().stream()
                    .peek(item -> item.setRestaurantId(createdRestaurant.getId()))
                    .collect(Collectors.toList());

            List<FoodItem> foodItemsToSave = foodItems.stream()
                    .map(FoodItemMapper.INSTANCE::mapFoodItemDTOToFoodItem)
                    .collect(Collectors.toList());

            List<FoodItem> savedItems = foodItemRepo.saveAll(foodItemsToSave);
            log.info("Saved {} food items for restaurant ID: {}", savedItems.size(), createdRestaurant.getId());

            request.setFoodItems(savedItems.stream()
                    .map(FoodItemMapper.INSTANCE::mapFoodItemToFoodItemDto)
                    .collect(Collectors.toList()));
        }

        request.setRestaurant(createdRestaurant);
        return request;
    }

    @Transactional
    public RestaurantWithFoodItemsDTO updateRestaurantWithFoodItems(Integer restaurantId, RestaurantWithFoodItemsDTO request) {
        log.info("Updating restaurant with food items - Restaurant ID: {}", restaurantId);

        // Step 1: Update restaurant via Restaurant Service
        Restaurant updatedRestaurant = restaurantService.updateRestaurant(restaurantId, request.getRestaurant());
        log.info("Restaurant updated with ID: {}", updatedRestaurant.getId());

        // 2. Delete all existing food items
        foodItemRepo.deleteByRestaurantId(restaurantId);

        // Step 2: Save new food items (with id = null)
        if (request.getFoodItems() != null && !request.getFoodItems().isEmpty()) {
            // Delete existing food items
//            foodItemRepo.deleteByRestaurantId(restaurantId);

            // Save new food items
            List<FoodItem> foodItems = request.getFoodItems().stream()
                    .peek(item -> {
                        item.setRestaurantId(restaurantId);
                        item.setId(null);
                    })
                    .map(FoodItemMapper.INSTANCE::mapFoodItemDTOToFoodItem)
                    .collect(Collectors.toList());

//            List<FoodItem> foodItemsToSave = foodItems.stream()
//                    .map(FoodItemMapper.INSTANCE::mapFoodItemDTOToFoodItem)
//                    .collect(Collectors.toList());

            List<FoodItem> savedItems = foodItemRepo.saveAll(foodItems);
            log.info("Updated {} food items for restaurant ID: {}", savedItems.size(), restaurantId);

            request.setFoodItems(savedItems.stream()
                    .map(FoodItemMapper.INSTANCE::mapFoodItemToFoodItemDto)
                    .collect(Collectors.toList()));
        }

//        request.setRestaurant(updatedRestaurant);
        return request;
    }

    @Transactional
    public void deleteRestaurantWithFoodItems(Integer restaurantId) {
        log.info("Deleting restaurant with food items - Restaurant ID: {}", restaurantId);

        // Step 1: Delete food items from Food Catalogue DB
        foodItemRepo.deleteByRestaurantId(restaurantId);
        log.info("Deleted food items for restaurant ID: {}", restaurantId);

        // Step 2: Delete restaurant via Restaurant Service
        restaurantService.deleteRestaurant(restaurantId);
        log.info("Restaurant deleted with ID: {}", restaurantId);
    }

}