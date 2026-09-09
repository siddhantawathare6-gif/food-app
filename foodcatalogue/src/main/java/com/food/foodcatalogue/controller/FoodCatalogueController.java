package com.food.foodcatalogue.controller;

import com.food.foodcatalogue.dto.FoodCataloguePage;
import com.food.foodcatalogue.dto.FoodItemDTO;
import com.food.foodcatalogue.dto.RestaurantWithFoodItemsDTO;
import com.food.foodcatalogue.service.FoodCatalogueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/foodCatalogue")
//@CrossOrigin (now that the gateway handles it centrally)
public class FoodCatalogueController {

    private static final Logger log = LoggerFactory.getLogger(FoodCatalogueController.class);

    private final FoodCatalogueService foodCatalogueService;

    public FoodCatalogueController(FoodCatalogueService foodCatalogueService) {
        this.foodCatalogueService = foodCatalogueService;
    }

    @PostMapping("/addFoodItem")
    public ResponseEntity<FoodItemDTO> addFoodItem(@RequestBody FoodItemDTO foodItemDTO){

        log.info("POST /foodCatalogue/addFoodItem - New food item request {}", foodItemDTO);

        log.info("Food item details - name: '{}', price: {}, restaurantId: {}, price: {}, quantity{}",
                foodItemDTO.getItemName(),
                foodItemDTO.getPrice(),
                foodItemDTO.getRestaurantId(),
                foodItemDTO.getPrice(),
                foodItemDTO.getQuantity());

        // Log full request at DEBUG level
        log.debug("Full food item request: {}", foodItemDTO);
        FoodItemDTO foodItemSaved = foodCatalogueService.addFoodItem(foodItemDTO);
        log.info("Food item created successfully - id: {}, name: '{}', restaurantId: {}, price: {}",
                foodItemSaved.getId(),
                foodItemSaved.getItemName(),
                foodItemSaved.getRestaurantId(),
                foodItemSaved.getPrice());
        return new ResponseEntity<>(foodItemSaved, HttpStatus.CREATED);
    }

    @GetMapping("/fetchRestaurantAndFoodItemsById/{restaurantId}")
    public ResponseEntity<FoodCataloguePage> fetchRestauDetailsWithFoodMenu(@PathVariable Integer restaurantId){

        log.info("GET /foodCatalogue/fetchRestaurantAndFoodItemsById/{} -",
                restaurantId);

        FoodCataloguePage foodCataloguePage = foodCatalogueService.fetchFoodCataloguePageDetails(restaurantId);

        log.info("fetch restaurant details with food menu {}", foodCataloguePage);
        return new ResponseEntity<>(foodCataloguePage, HttpStatus.OK);
    }

    @PostMapping("/addRestaurantWithFoodItems")
    public ResponseEntity<RestaurantWithFoodItemsDTO> addRestaurantWithFoodItems(
            @RequestBody RestaurantWithFoodItemsDTO request) {
        log.info("POST /foodCatalogue/addRestaurantWithFoodItems - Restaurant: {}",
                request.getRestaurant().getName());
        RestaurantWithFoodItemsDTO response = foodCatalogueService.addRestaurantWithFoodItems(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ========== NEW: Update Restaurant with Food Items ==========
    @PutMapping("/updateRestaurantWithFoodItems/{restaurantId}")
    public ResponseEntity<RestaurantWithFoodItemsDTO> updateRestaurantWithFoodItems(
            @PathVariable Integer restaurantId,
            @RequestBody RestaurantWithFoodItemsDTO request) {
        log.info("PUT /foodCatalogue/updateRestaurantWithFoodItems/{}", restaurantId);
        RestaurantWithFoodItemsDTO response = foodCatalogueService.updateRestaurantWithFoodItems(restaurantId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ========== NEW: Delete Restaurant with Food Items ==========
    @DeleteMapping("/deleteRestaurantWithFoodItems/{restaurantId}")
    public ResponseEntity<Void> deleteRestaurantWithFoodItems(@PathVariable Integer restaurantId) {
        log.info("DELETE /foodCatalogue/deleteRestaurantWithFoodItems/{}", restaurantId);
        foodCatalogueService.deleteRestaurantWithFoodItems(restaurantId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
