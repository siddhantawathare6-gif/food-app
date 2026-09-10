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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantControllerTest {

    @InjectMocks
    RestaurantController restaurantController;

    @Mock
    RestaurantService restaurantService;

    // ============================================================
    // ===== fetchAllRestaurant =====
    // ============================================================

    @Test
    public void testFetchAllRestaurant() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "asc";

        // FIXED: Use no-args constructor + setters
        RestaurantDTO r1 = new RestaurantDTO();
        r1.setId(1);
        r1.setName("Taj");
        r1.setAddress("Mumbai street, 102");
        r1.setCity("Mumbai");
        r1.setRestaurantDescription("family village taste");

        RestaurantDTO r2 = new RestaurantDTO();
        r2.setId(2);
        r2.setName("Sidd");
        r2.setAddress("Red street, 203");
        r2.setCity("USA");
        r2.setRestaurantDescription("multi causin");

        List<RestaurantDTO> restaurantList = Arrays.asList(r1, r2);

        RestaurantPageDto restaurantPageDto = new RestaurantPageDto();
        restaurantPageDto.setRestaurantList(restaurantList);
        restaurantPageDto.setLast(true);
        restaurantPageDto.setPageNo(0);
        restaurantPageDto.setPageSize(5);
        restaurantPageDto.setTotalElement(2);
        restaurantPageDto.setTotalPage(1);

        when(restaurantService.featchAllRestaurant(pageNo, pageSize, sortBy, sortDir))
                .thenReturn(restaurantPageDto);

        ResponseEntity<RestaurantPageDto> response =
                restaurantController.fetchAllRestaurant(pageNo, pageSize, sortBy, sortDir);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(restaurantPageDto, response.getBody());
        verify(restaurantService, times(1))
                .featchAllRestaurant(pageNo, pageSize, sortBy, sortDir);
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

        ResponseEntity<RestaurantPageDto> response =
                restaurantController.fetchAllRestaurant(pageNo, pageSize, sortBy, sortDir);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getRestaurantList().isEmpty());
        assertEquals(0, response.getBody().getTotalElement());
    }

    // ============================================================
    // ===== saveRestaurant =====
    // ============================================================

    @Test
    public void saveRestaurant() {
        RestaurantDTO request = new RestaurantDTO();
        request.setId(1);
        request.setName("Taj");
        request.setAddress("Mumbai street, 102");
        request.setCity("Mumbai");
        request.setRestaurantDescription("family village taste");

        RestaurantDTO saved = new RestaurantDTO();
        saved.setId(1);
        saved.setName("Taj");
        saved.setAddress("Mumbai street, 102");
        saved.setCity("Mumbai");
        saved.setRestaurantDescription("family village taste");

        when(restaurantService.addRestaurant(request)).thenReturn(saved);

        ResponseEntity<RestaurantDTO> response = restaurantController.saveRestaurant(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(saved, response.getBody());
        verify(restaurantService, times(1)).addRestaurant(request);
    }

    // ============================================================
    // ===== fetchRestaurantById =====
    // ============================================================

    @Test
    public void testFindRestaurantById() {
        Integer mockRestaurantId = 1;

        RestaurantDTO mockRestaurant = new RestaurantDTO();
        mockRestaurant.setId(1);
        mockRestaurant.setName("Restaurant 1");
        mockRestaurant.setAddress("Address 1");
        mockRestaurant.setCity("city 1");
        mockRestaurant.setRestaurantDescription("Desc 1");

        when(restaurantService.fetchRestaurantById(mockRestaurantId)).thenReturn(mockRestaurant);

        ResponseEntity<RestaurantDTO> response =
                restaurantController.fetchRestaurantById(mockRestaurantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockRestaurant, response.getBody());
        verify(restaurantService, times(1)).fetchRestaurantById(mockRestaurantId);
    }

    // ============================================================
    // ===== NEW: updateRestaurant =====
    // ============================================================

    @Test
    public void testUpdateRestaurant() {
        Integer id = 1;
        RestaurantDTO updateDto = new RestaurantDTO();
        updateDto.setName("Updated Name");
        updateDto.setCity("Updated City");

        RestaurantDTO updated = new RestaurantDTO();
        updated.setId(id);
        updated.setName("Updated Name");
        updated.setCity("Updated City");

        when(restaurantService.updateRestaurant(id, updateDto)).thenReturn(updated);

        ResponseEntity<RestaurantDTO> response =
                restaurantController.updateRestaurant(id, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
        verify(restaurantService).updateRestaurant(id, updateDto);
    }

    // ============================================================
    // ===== NEW: deleteRestaurant =====
    // ============================================================

    @Test
    public void testDeleteRestaurant() {
        Integer id = 1;
        doNothing().when(restaurantService).deleteRestaurant(id);

        ResponseEntity<Void> response = restaurantController.deleteRestaurant(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(restaurantService).deleteRestaurant(id);
    }
}