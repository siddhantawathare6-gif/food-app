package com.food.restaurant.controller;

import com.food.restaurant.constant.ApplicationConstant;
import com.food.restaurant.dto.RestaurantDTO;
import com.food.restaurant.dto.RestaurantPageDto;
import com.food.restaurant.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/restaurant")
@CrossOrigin
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/fetchAllRestaurant")
    public ResponseEntity<RestaurantPageDto> fetchAllRestaurant(@RequestParam(value = "pageNo", defaultValue = ApplicationConstant.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                                                @RequestParam(value = "pageSize", defaultValue = ApplicationConstant.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                                                @RequestParam(value = "sortBy", defaultValue = ApplicationConstant.DEFAULT_SORT_BY, required = false) String sortBy,
                                                                @RequestParam(value = "sortDir", defaultValue = ApplicationConstant.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        RestaurantPageDto restaurantPageDto = restaurantService.featchAllRestaurant(pageNo, pageSize, sortBy, sortDir);
        return new ResponseEntity<>(restaurantPageDto, HttpStatus.OK);
    }

    @PostMapping("/addRestaurant")
    public ResponseEntity<RestaurantDTO> saveRestaurant(@RequestBody RestaurantDTO restaurantDTO) {
        RestaurantDTO restaurant = restaurantService.addRestaurant(restaurantDTO);
        return new ResponseEntity<>(restaurant, HttpStatus.CREATED);
    }

    @GetMapping("/fetchById/{id}")
    public ResponseEntity<RestaurantDTO> fetchRestaurantById(@PathVariable Integer id) {
        RestaurantDTO restaurant = restaurantService.fetchRestaurantById(id);
        return new ResponseEntity<>(restaurant, HttpStatus.OK);
    }
}
