package com.food.resturant.controller;

import com.food.resturant.dto.RestaurantDTO;
import com.food.resturant.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurant")
@CrossOrigin
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/fetchAllRestaurant")
    public ResponseEntity<List<RestaurantDTO>> fetchAllRestaurant(){
       List<RestaurantDTO> restaurantList =  restaurantService.featchAllRestaurant();
       return new ResponseEntity<>(restaurantList, HttpStatus.OK);
    }

    @PostMapping("/addRestaurant")
    public ResponseEntity<RestaurantDTO> saveRestaurant(@RequestBody RestaurantDTO restaurantDTO){
        RestaurantDTO restaurant =  restaurantService.addRestaurant(restaurantDTO);
        return new ResponseEntity<>(restaurant, HttpStatus.CREATED);
    }

    @GetMapping("/fetchById/{id}")
    public ResponseEntity<RestaurantDTO> fetchRestaurantById(@PathVariable Integer id){
        RestaurantDTO restaurant =  restaurantService.fetchRestaurantById(id);
        return new ResponseEntity<>(restaurant, HttpStatus.OK);
    }
}
