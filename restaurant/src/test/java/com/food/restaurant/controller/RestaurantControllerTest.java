package com.food.restaurant.controller;


import com.food.restaurant.dto.RestaurantDTO;
import com.food.restaurant.dto.RestaurantPageDto;
import com.food.restaurant.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantControllerTest {

    @InjectMocks
    RestaurantController restaurantController;

    @Mock
    RestaurantService restaurantService;


    @Test
    public void testFetchAllRestaurant() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "asc";

        List<RestaurantDTO> restaurantList = Arrays.asList(new RestaurantDTO(1, "Taj", "Mumbai street, 102", "Mumbai", "family village taste"),
                new RestaurantDTO(2, "Sidd", "Red street, 203", "USA", "multi causin"));
        RestaurantPageDto restaurantPageDto = new RestaurantPageDto();
        restaurantPageDto.setRestaurantList(restaurantList);
        restaurantPageDto.setLast(true);
        restaurantPageDto.setPageNo(0);
        restaurantPageDto.setPageSize(5);
        restaurantPageDto.setTotalElement(2);
        restaurantPageDto.setTotalPage(1);

        // Mock the service
        when(restaurantService.featchAllRestaurant(pageNo, pageSize, sortBy, sortDir))
                .thenReturn(restaurantPageDto);

        ResponseEntity<RestaurantPageDto> response = restaurantController.fetchAllRestaurant(pageNo, pageSize, sortBy, sortDir);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(restaurantPageDto, response.getBody());

        verify(restaurantService, times(1)).featchAllRestaurant(pageNo, pageSize, sortBy, sortDir);

    }

    @Test
    public void testFetchAllRestaurantWithEmptyResult() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "asc";

        RestaurantPageDto emptyPageDto = new RestaurantPageDto();
        emptyPageDto.setRestaurantList(Collections.emptyList());
        emptyPageDto.setTotalElement(0);
        emptyPageDto.setTotalPage(0);

        when(restaurantService.featchAllRestaurant(pageNo, pageSize, sortBy, sortDir))
                .thenReturn(emptyPageDto);

        ResponseEntity<RestaurantPageDto> response = restaurantController.fetchAllRestaurant(
                pageNo, pageSize, sortBy, sortDir);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getRestaurantList().isEmpty());
        assertEquals(0, response.getBody().getTotalElement());
    }


    @Test
    public void saveRestaurant() {
        RestaurantDTO request = new RestaurantDTO(1, "Taj", "Mumbai street, 102", "Mumbai", "family village taste");

        when(restaurantService.addRestaurant(request)).thenReturn(new RestaurantDTO(1, "Taj", "Mumbai street, 102",
                "Mumbai", "family village taste"));

        ResponseEntity<RestaurantDTO> response = restaurantController.saveRestaurant(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(request, response.getBody());

        verify(restaurantService, times(1)).addRestaurant(request);

    }

    @Test
    public void testFindRestaurantById() {
        // Create a mock restaurant ID
        Integer mockRestaurantId = 1;

        // Create a mock restaurant to be returned by the service
        RestaurantDTO mockRestaurant = new RestaurantDTO(1, "Restaurant 1", "Address 1", "city 1", "Desc 1");

        // Mock the service behavior
        when(restaurantService.fetchRestaurantById(mockRestaurantId)).thenReturn(mockRestaurant);

        // Call the controller method
        ResponseEntity<RestaurantDTO> response = restaurantController.fetchRestaurantById(mockRestaurantId);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockRestaurant, response.getBody());

        // Verify that the service method was called
        verify(restaurantService, times(1)).fetchRestaurantById(mockRestaurantId);
    }

}
