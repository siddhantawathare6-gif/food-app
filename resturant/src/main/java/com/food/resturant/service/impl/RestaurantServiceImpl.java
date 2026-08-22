package com.food.resturant.service.impl;

import com.food.resturant.RestaurantNotFoundException;
import com.food.resturant.dto.RestaurantDTO;
import com.food.resturant.entity.Restaurant;
import com.food.resturant.mapper.RestaurantMapper;
import com.food.resturant.repository.RestaurantRepository;
import com.food.resturant.service.RestaurantService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public List<RestaurantDTO> featchAllRestaurant() {
        List<Restaurant> restaurantList = restaurantRepository.findAll();
        return restaurantList.stream().map(RestaurantMapper.INSTANCE::mapRestaurantToRestaurantDTO).collect(Collectors.toList());
    }

    @Override
    public RestaurantDTO addRestaurant(RestaurantDTO restaurantDTO) {
        Restaurant restaurant = RestaurantMapper.INSTANCE.mapRestaurantDTOToRestaurant(restaurantDTO);
        Restaurant saveRestaurant = restaurantRepository.save(restaurant);
        return RestaurantMapper.INSTANCE.mapRestaurantToRestaurantDTO(saveRestaurant);
    }

    @Override
    public RestaurantDTO fetchRestaurantById(Integer id) {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(
                () -> new RestaurantNotFoundException("Restaurant not found with id: " + id));
        return RestaurantMapper.INSTANCE.mapRestaurantToRestaurantDTO(restaurant);

    }
}
