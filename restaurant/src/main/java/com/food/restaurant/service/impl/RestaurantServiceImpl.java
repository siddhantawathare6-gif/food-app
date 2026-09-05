package com.food.restaurant.service.impl;

import com.food.restaurant.exception.RestaurantNotFoundException;
import com.food.restaurant.dto.RestaurantDTO;
import com.food.restaurant.dto.RestaurantPageDto;
import com.food.restaurant.entity.Restaurant;
import com.food.restaurant.mapper.RestaurantMapper;
import com.food.restaurant.repository.RestaurantRepository;
import com.food.restaurant.service.RestaurantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantServiceImpl.class);
    private final RestaurantRepository restaurantRepository;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public RestaurantPageDto featchAllRestaurant(int pageNo, int pageSize, String sortBy, String sortDir) {

        log.info("Fetching all restaurants - pageNo={}, pageSize={}, sortBy={}, sortDir={}", pageNo, pageSize, sortBy, sortDir);

        log.debug("Creating sort order: sortBy={}, sortDir={}", sortBy, sortDir);
        //check sort order
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        //create Pageable instance
        //Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        log.debug("Pageable created: offset={}, pageSize={}", pageable.getOffset(), pageable.getPageSize());

        //List<Restaurant> restaurantList = restaurantRepository.findAll();
        log.debug("Executing database query for restaurants");
        Page<Restaurant> pageRestaurant = restaurantRepository.findAll(pageable);
        log.debug("Database query completed - found {} restaurants out of {} total", pageRestaurant.getContent().size(), pageRestaurant.getTotalElements());

        //get content from page object
        List<Restaurant> restaurantList = pageRestaurant.getContent();

        log.debug("Mapping {} restaurants to DTOs", restaurantList.size());
        List<RestaurantDTO> restaurantDTOS = restaurantList.stream().map(RestaurantMapper.INSTANCE::mapRestaurantToRestaurantDTO).collect(Collectors.toList());
        RestaurantPageDto restaurantPageDto = new RestaurantPageDto();
        restaurantPageDto.setRestaurantList(restaurantDTOS);
        restaurantPageDto.setPageNo(pageRestaurant.getNumber());
        restaurantPageDto.setPageSize(pageRestaurant.getSize());
        restaurantPageDto.setTotalPage(pageRestaurant.getTotalPages());
        restaurantPageDto.setTotalElement(pageRestaurant.getTotalElements());
        restaurantPageDto.setLast(pageRestaurant.isLast());

        log.info("Successfully fetched {} restaurants (page {}/{}), total: {}", restaurantDTOS.size(), pageRestaurant.getNumber() + 1,
                pageRestaurant.getTotalPages(), pageRestaurant.getTotalElements());

        return restaurantPageDto;
    }

    @Override
    public RestaurantDTO addRestaurant(RestaurantDTO restaurantDTO) {

        log.info("Adding new restaurant - name='{}', city='{}'", restaurantDTO.getName(), restaurantDTO.getCity());

        // Log full DTO at DEBUG level (mask sensitive data if any)
        log.debug("RestaurantDTO received: {}", restaurantDTO);

        Restaurant restaurant = RestaurantMapper.INSTANCE.mapRestaurantDTOToRestaurant(restaurantDTO);
        log.debug("Restaurant entity created: {}", restaurant);

        log.debug("Saving restaurant to database");
        Restaurant saveRestaurant = restaurantRepository.save(restaurant);
        log.debug("Restaurant saved with ID: {}", saveRestaurant.getId());


        RestaurantDTO savedDTO = RestaurantMapper.INSTANCE.mapRestaurantToRestaurantDTO(saveRestaurant);
        log.info("Restaurant created successfully - id={}, name='{}', city='{}'", savedDTO.getId(), savedDTO.getName(), savedDTO.getCity());

        return savedDTO;
    }

    @Override
    public RestaurantDTO fetchRestaurantById(Integer id) {

        log.info("Fetching restaurant by ID: {}", id);

        log.debug("Querying database for restaurant ID: {}", id);
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(
                () -> {
                    log.warn("Restaurant not found with ID: {}", id);
                    return new RestaurantNotFoundException("Restaurant not found with id: " + id);
                });

        log.debug("Restaurant found: id={}, name='{}', city='{}'", restaurant.getId(), restaurant.getName(), restaurant.getCity());

        return RestaurantMapper.INSTANCE.mapRestaurantToRestaurantDTO(restaurant);

    }
}
