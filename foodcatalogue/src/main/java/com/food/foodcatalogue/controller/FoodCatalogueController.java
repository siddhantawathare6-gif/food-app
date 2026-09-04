package com.food.foodcatalogue.controller;

import com.food.foodcatalogue.dto.FoodCataloguePage;
import com.food.foodcatalogue.dto.FoodItemDTO;
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
        log.info("✅ Food item created successfully - id: {}, name: '{}', restaurantId: {}, price: {}",
                foodItemSaved.getId(),
                foodItemSaved.getItemName(),
                foodItemSaved.getRestaurantId(),
                foodItemSaved.getPrice());
        return new ResponseEntity<>(foodItemSaved, HttpStatus.CREATED);
    }

    @GetMapping("/fetchRestaurantAndFoodItemsById/{restaurantId}")
    public ResponseEntity<FoodCataloguePage> fetchRestauDetailsWithFoodMenu(@PathVariable Integer restaurantId){

        log.info("🔍 GET /foodCatalogue/fetchRestaurantAndFoodItemsById/{} -",
                restaurantId);

        FoodCataloguePage foodCataloguePage = foodCatalogueService.fetchFoodCataloguePageDetails(restaurantId);

        log.info("fetch restaurant details with food menu {}", foodCataloguePage);
        return new ResponseEntity<>(foodCataloguePage, HttpStatus.OK);


    }



}
