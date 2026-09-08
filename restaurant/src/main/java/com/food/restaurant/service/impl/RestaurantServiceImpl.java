package com.food.restaurant.service.impl;

import com.food.restaurant.exception.RestaurantNotFoundException;
import com.food.restaurant.dto.RestaurantDTO;
import com.food.restaurant.dto.RestaurantPageDto;
import com.food.restaurant.entity.Restaurant;
import com.food.restaurant.mapper.RestaurantMapper;
import com.food.restaurant.repository.RestaurantRepository;
import com.food.restaurant.service.FileStorageService;
import com.food.restaurant.service.RestaurantService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantServiceImpl.class);
    private final RestaurantRepository restaurantRepository;
    private final FileStorageService fileStorageService;

    @Value("${app.image.base-url:/restaurant/image}")
    private String baseImageUrl;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository, FileStorageService fileStorageService) {
        this.restaurantRepository = restaurantRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Cacheable(value = "restaurantPage", key = "#pageNo + '-' + #pageSize + '-' + #sortBy + '-' + #sortDir")
    public RestaurantPageDto featchAllRestaurant(int pageNo, int pageSize, String sortBy, String sortDir) {

        log.info("Fetching all restaurants - pageNo={}, pageSize={}, sortBy={}, sortDir={}", pageNo, pageSize, sortBy, sortDir);

        log.debug("Creating sort order: sortBy={}, sortDir={}", sortBy, sortDir);
        //check sort order
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

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
        List<RestaurantDTO> restaurantDTOS = restaurantList.stream().map(restaurant -> {
            log.debug("Mapping {} restaurants to DTOs", restaurant.getId());
            RestaurantDTO dto = RestaurantMapper.INSTANCE.mapRestaurantToRestaurantDTO(restaurant);
            dto.setImageUrl(baseImageUrl + "/" + restaurant.getId());
            return dto;
        }).collect(Collectors.toList());

        RestaurantPageDto restaurantPageDto = new RestaurantPageDto();
        restaurantPageDto.setRestaurantList(restaurantDTOS);
        restaurantPageDto.setPageNo(pageRestaurant.getNumber());
        restaurantPageDto.setPageSize(pageRestaurant.getSize());
        restaurantPageDto.setTotalPage(pageRestaurant.getTotalPages());
        restaurantPageDto.setTotalElement(pageRestaurant.getTotalElements());
        restaurantPageDto.setLast(pageRestaurant.isLast());

        log.info("Successfully fetched {} restaurants (page {}/{}), total: {}", restaurantDTOS.size(), pageRestaurant.getNumber() + 1, pageRestaurant.getTotalPages(), pageRestaurant.getTotalElements());

        return restaurantPageDto;
    }

    @Override
    @CacheEvict(value = "restaurantPage", allEntries = true)
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
        savedDTO.setImageUrl(baseImageUrl + "/" + savedDTO.getId());

        return savedDTO;
    }

    @Override
    @Cacheable(value = "restaurant", key = "#id")
    public RestaurantDTO fetchRestaurantById(Integer id) {

        log.info("Fetching restaurant by ID: {}", id);

        log.debug("Querying database for restaurant ID: {}", id);
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> {
            log.warn("Restaurant not found with ID: {}", id);
            return new RestaurantNotFoundException("Restaurant not found with id: " + id);
        });

        log.debug("Restaurant found: id={}, name='{}', city='{}'", restaurant.getId(), restaurant.getName(), restaurant.getCity());

        RestaurantDTO restaurantDTO = RestaurantMapper.INSTANCE.mapRestaurantToRestaurantDTO(restaurant);
        restaurantDTO.setImageUrl(baseImageUrl + "/" + restaurant.getId());
        return restaurantDTO;

    }

    @Override
    @CacheEvict(value = {"restaurant", "restaurantPage"}, allEntries = true)
    public String uploadRestaurantImage(Integer restaurantId, MultipartFile file) {
        log.info("Uploading image for restaurant ID: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + restaurantId));

        // Clean up any previously stored image before saving the new one,
        // in case the file extension changed (e.g. .jpg replaced with .png)
        if (restaurant.getImageName() != null) {
            fileStorageService.deleteFile(restaurant.getImageName());
        }

        String fileName = fileStorageService.storeFile(restaurantId, file);

        restaurant.setImageName(fileName);
        restaurantRepository.save(restaurant);

        log.info("Image uploaded successfully for restaurantId: {}, fileName: {}", restaurantId, fileName);

        return baseImageUrl + "/" + restaurantId;
    }

    @Override
    public byte[] getRestaurantImage(Integer restaurantId) {
        log.debug("Retrieving image for restaurant ID: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + restaurantId));

        if (restaurant.getImageName() == null) {
            log.warn("No image uploaded yet for restaurant {}, returning default", restaurantId);
            return fileStorageService.readFile("default.jpg");
        }

        return fileStorageService.readFile(restaurant.getImageName());
    }
}
