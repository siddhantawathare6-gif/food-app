package com.food.restaurant.service.impl;

import com.food.restaurant.exception.RestaurantNotFoundException;
import com.food.restaurant.dto.RestaurantDTO;
import com.food.restaurant.dto.RestaurantPageDto;
import com.food.restaurant.entity.Restaurant;
import com.food.restaurant.mapper.RestaurantMapper;
import com.food.restaurant.repository.RestaurantRepository;
import com.food.restaurant.service.RestaurantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public RestaurantPageDto featchAllRestaurant(int pageNo, int pageSize, String sortBy, String sortDir) {

        //check sort order
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        //create Pageable instance
        //Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        //List<Restaurant> restaurantList = restaurantRepository.findAll();
        Page<Restaurant> pageRestaurant = restaurantRepository.findAll(pageable);

        //get content from page object
        List<Restaurant> restaurantList = pageRestaurant.getContent();
        List<RestaurantDTO> restaurantDTOS = restaurantList.stream().map(RestaurantMapper.INSTANCE::mapRestaurantToRestaurantDTO).collect(Collectors.toList());
        RestaurantPageDto restaurantPageDto = new RestaurantPageDto();
        restaurantPageDto.setRestaurantList(restaurantDTOS);
        restaurantPageDto.setPageNo(pageRestaurant.getNumber());
        restaurantPageDto.setPageSize(pageRestaurant.getSize());
        restaurantPageDto.setTotalPage(pageRestaurant.getTotalPages());
        restaurantPageDto.setTotalElement(pageRestaurant.getTotalElements());
        restaurantPageDto.setLast(pageRestaurant.isLast());
        return restaurantPageDto;
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
