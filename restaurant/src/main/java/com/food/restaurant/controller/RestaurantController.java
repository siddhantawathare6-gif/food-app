package com.food.restaurant.controller;

import com.food.restaurant.constant.ApplicationConstant;
import com.food.restaurant.dto.RestaurantDTO;
import com.food.restaurant.dto.RestaurantPageDto;
import com.food.restaurant.service.RestaurantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/restaurant")
//@CrossOrigin (now that the gateway handles it centrally)
public class RestaurantController {

    private static final Logger log = LoggerFactory.getLogger(RestaurantController.class);
    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/fetchAllRestaurant")
    public ResponseEntity<RestaurantPageDto> fetchAllRestaurant(@RequestParam(value = "pageNo", defaultValue = ApplicationConstant.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                                                @RequestParam(value = "pageSize", defaultValue = ApplicationConstant.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                                                @RequestParam(value = "sortBy", defaultValue = ApplicationConstant.DEFAULT_SORT_BY, required = false) String sortBy,
                                                                @RequestParam(value = "sortDir", defaultValue = ApplicationConstant.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        log.info("GET /restaurant/fetchAllRestaurant - Request received with pageNo={}, pageSize={}, sortBy={}, sortDir={}", pageNo, pageSize, sortBy, sortDir);

        RestaurantPageDto restaurantPageDto = restaurantService.featchAllRestaurant(pageNo, pageSize, sortBy, sortDir);

        log.info("GET /restaurant/fetchAllRestaurant - Success: {} restaurants found (page {} of {})",
                restaurantPageDto.getTotalElement(), restaurantPageDto.getPageNo() + 1,restaurantPageDto.getTotalPage());

        return new ResponseEntity<>(restaurantPageDto, HttpStatus.OK);
    }

    @PostMapping("/addRestaurant")
    public ResponseEntity<RestaurantDTO> saveRestaurant(@RequestBody RestaurantDTO restaurantDTO) {
        log.info("🚀 POST /restaurant/addRestaurant - Creating new restaurant: name='{}', city='{}'", restaurantDTO.getName(), restaurantDTO.getCity());

        RestaurantDTO restaurant = restaurantService.addRestaurant(restaurantDTO);

        log.info("POST /restaurant/addRestaurant - Restaurant created successfully: id={}, name='{}'", restaurant.getId(), restaurant.getName());

        return new ResponseEntity<>(restaurant, HttpStatus.CREATED);
    }

    @GetMapping("/fetchById/{id}")
    public ResponseEntity<RestaurantDTO> fetchRestaurantById(@PathVariable Integer id) {
        log.info("GET /restaurant/fetchById/{} - Request received", id);

        RestaurantDTO restaurant = restaurantService.fetchRestaurantById(id);

        log.info("✅ GET /restaurant/fetchById/{} - Success: Found restaurant '{}' in city '{}'", id, restaurant.getName(), restaurant.getCity());

        return new ResponseEntity<>(restaurant, HttpStatus.OK);
    }
}
