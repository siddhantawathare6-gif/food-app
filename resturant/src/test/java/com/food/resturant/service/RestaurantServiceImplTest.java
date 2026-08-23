package com.food.resturant.service;

import com.food.resturant.RestaurantNotFoundException;
import com.food.resturant.dto.RestaurantDTO;
import com.food.resturant.dto.RestaurantPageDto;
import com.food.resturant.entity.Restaurant;
import com.food.resturant.mapper.RestaurantMapper;
import com.food.resturant.repository.RestaurantRepository;
import com.food.resturant.service.impl.RestaurantServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceImplTest {

    @InjectMocks
    RestaurantServiceImpl restaurantService;

    @Mock
    RestaurantRepository restaurantRepository;

    RestaurantMapper restaurantMapper;


    @Test
    public void testFeatchAllRestaurant() {
        // Arrange
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "asc";

        List<Restaurant> restaurantList = Arrays.asList(new Restaurant(1, "Taj", "Mumbai street, 102", "Mumbai",
                        "family village taste"),
                new Restaurant(2, "Sidd", "Red street, 203", "USA", "multi causin"));

        // Create Pageable object
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        // Create Page with proper constructor - includes pageable and total elements
        Page<Restaurant> pageRestaurant = new PageImpl<>(
                restaurantList,           // content
                pageable,                 // pageable
                restaurantList.size()     // total elements
        );

        // Mock the paginated repository call
        when(restaurantRepository.findAll(any(Pageable.class))).thenReturn(pageRestaurant);

        // Act
        RestaurantPageDto response = restaurantService.featchAllRestaurant(pageNo, pageSize, sortBy, sortDir);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getRestaurantList().size());
        assertEquals(0, response.getPageNo());
        assertEquals(10, response.getPageSize());
        assertEquals(1, response.getTotalPage());
        assertEquals(2, response.getTotalElement());
        assertTrue(response.isLast());

        // Verify the repository was called with correct Pageable
        verify(restaurantRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    public void testAddRestaurantInDB() {
        // Create a mock restaurant to be saved
        RestaurantDTO mockRestaurantDTO = new RestaurantDTO(1, "Restaurant 1", "Address 1", "city 1", "Desc 1");
        Restaurant mockRestaurant = RestaurantMapper.INSTANCE.mapRestaurantDTOToRestaurant(mockRestaurantDTO);

        // Mock the repository behavior
        when(restaurantRepository.save(mockRestaurant)).thenReturn(mockRestaurant);

        // Call the service method
        RestaurantDTO savedRestaurantDTO = restaurantService.addRestaurant(mockRestaurantDTO);

        // Verify the result
        assertEquals(mockRestaurantDTO, savedRestaurantDTO);

        // Verify that the repository method was called
        verify(restaurantRepository, times(1)).save(mockRestaurant);
    }

    @Test
    public void testFetchRestaurantById_ExistingId() {
        // Create a mock restaurant ID
        Integer mockRestaurantId = 1;

        // Create a mock restaurant to be returned by the repository
        Restaurant mockRestaurant = new Restaurant(1, "Restaurant 1", "Address 1", "city 1", "Desc 1");

        // Mock the repository behavior
        when(restaurantRepository.findById(mockRestaurantId)).thenReturn(Optional.of(mockRestaurant));

        // Call the service method
        RestaurantDTO response = restaurantService.fetchRestaurantById(mockRestaurantId);

        // Verify the response
        assertNotNull(response);
        assertEquals(mockRestaurantId, response.getId());
        assertEquals("Restaurant 1", response.getName());
        assertEquals("Address 1", response.getAddress());
        assertEquals("city 1", response.getCity());
        assertEquals("Desc 1", response.getRestaurantDescription());

        // Verify that the repository method was called
        verify(restaurantRepository, times(1)).findById(mockRestaurantId);
    }

    @Test
    public void testFetchRestaurantById_NonExistingId() {
        // Create a mock non-existing restaurant ID
        Integer mockRestaurantId = 1;

        // Mock the repository behavior
        when(restaurantRepository.findById(mockRestaurantId)).thenReturn(Optional.empty());

        // Verify that exception is thrown
        RestaurantNotFoundException exception = assertThrows(
                RestaurantNotFoundException.class,
                () -> restaurantService.fetchRestaurantById(mockRestaurantId)
        );

        // Verify the response
        assertEquals(
                "Restaurant not found with id: " + mockRestaurantId,
                exception.getMessage()
        );

        // Verify that the repository method was called
        verify(restaurantRepository, times(1)).findById(mockRestaurantId);
    }

}
