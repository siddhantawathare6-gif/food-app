package com.food.resturant.controller;


import com.food.resturant.dto.RestaurantDTO;
import com.food.resturant.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantControllerTest {

    @InjectMocks
    RestaurantController restaurantController;

    @Mock
    RestaurantService restaurantService;


    @Test
    public void testFetchAllRestaurant() {
        List<RestaurantDTO> restaurantList = Arrays.asList(new RestaurantDTO(1, "Taj", "Mumbai street, 102", "Mumbai", "family village taste"),
                new RestaurantDTO(2, "Sidd", "Red street, 203", "USA", "multi causin"));
        when(restaurantService.featchAllRestaurant()).thenReturn(restaurantList);

        ResponseEntity<List<RestaurantDTO>> response = restaurantController.fetchAllRestaurant();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(restaurantList, response.getBody());

        verify(restaurantService, times(1)).featchAllRestaurant();

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
