package com.food.restaurant.service;

import com.food.restaurant.dto.RestaurantDTO;
import com.food.restaurant.dto.RestaurantPageDto;
import com.food.restaurant.entity.Restaurant;
import com.food.restaurant.exception.RestaurantNotFoundException;
import com.food.restaurant.mapper.RestaurantMapper;
import com.food.restaurant.repository.RestaurantRepository;
import com.food.restaurant.service.FileStorageService;
import com.food.restaurant.service.impl.RestaurantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceImplTest {

    @Mock
    RestaurantRepository restaurantRepository;

    @Mock
    FileStorageService fileStorageService;   // ← ADD: needed for @InjectMocks

    @InjectMocks
    RestaurantServiceImpl restaurantService;

    private static final String BASE_IMAGE_URL = "/restaurant/image";

    @BeforeEach
    void setUp() {
        // Inject @Value field since Mockito doesn't set it
        ReflectionTestUtils.setField(restaurantService, "baseImageUrl", BASE_IMAGE_URL);
    }

    // ============================================================
    // ===== fetchAllRestaurant =====
    // ============================================================

    @Test
    public void testFeatchAllRestaurant() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "asc";

        // FIXED: Restaurant now has more fields — use no-args + setters
        Restaurant r1 = new Restaurant();
        r1.setId(1);
        r1.setName("Taj");
        r1.setAddress("Mumbai street, 102");
        r1.setCity("Mumbai");
        r1.setRestaurantDescription("family village taste");

        Restaurant r2 = new Restaurant();
        r2.setId(2);
        r2.setName("Sidd");
        r2.setAddress("Red street, 203");
        r2.setCity("USA");
        r2.setRestaurantDescription("multi causin");

        List<Restaurant> restaurantList = Arrays.asList(r1, r2);

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
        Page<Restaurant> pageRestaurant = new PageImpl<>(restaurantList, pageable, restaurantList.size());

        when(restaurantRepository.findAll(any(Pageable.class))).thenReturn(pageRestaurant);

        RestaurantPageDto response = restaurantService.featchAllRestaurant(pageNo, pageSize, sortBy, sortDir);

        assertNotNull(response);
        assertEquals(2, response.getRestaurantList().size());
        assertEquals(0, response.getPageNo());
        assertEquals(10, response.getPageSize());
        assertEquals(1, response.getTotalPage());
        assertEquals(2, response.getTotalElement());
        assertTrue(response.isLast());

        // Verify image URLs are set
        assertEquals(BASE_IMAGE_URL + "/1", response.getRestaurantList().get(0).getImageUrl());
        assertEquals(BASE_IMAGE_URL + "/2", response.getRestaurantList().get(1).getImageUrl());

        verify(restaurantRepository, times(1)).findAll(any(Pageable.class));
    }

    // ============================================================
    // ===== addRestaurant =====
    // ============================================================

    @Test
    public void testAddRestaurantInDB() {
        // FIXED: Use no-args + setters for both DTO and entity
        RestaurantDTO mockRestaurantDTO = new RestaurantDTO();
        mockRestaurantDTO.setId(1);
        mockRestaurantDTO.setName("Restaurant 1");
        mockRestaurantDTO.setAddress("Address 1");
        mockRestaurantDTO.setCity("city 1");
        mockRestaurantDTO.setRestaurantDescription("Desc 1");

        Restaurant mockRestaurant = new Restaurant();
        mockRestaurant.setId(1);
        mockRestaurant.setName("Restaurant 1");
        mockRestaurant.setAddress("Address 1");
        mockRestaurant.setCity("city 1");
        mockRestaurant.setRestaurantDescription("Desc 1");

        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(mockRestaurant);

        RestaurantDTO savedRestaurantDTO = restaurantService.addRestaurant(mockRestaurantDTO);

        assertNotNull(savedRestaurantDTO);
        assertEquals("Restaurant 1", savedRestaurantDTO.getName());
        assertEquals("city 1", savedRestaurantDTO.getCity());
        // imageUrl is set in the service
        assertEquals(BASE_IMAGE_URL + "/1", savedRestaurantDTO.getImageUrl());

        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    // ============================================================
    // ===== fetchRestaurantById =====
    // ============================================================

    @Test
    public void testFetchRestaurantById_ExistingId() {
        Integer mockRestaurantId = 1;

        Restaurant mockRestaurant = new Restaurant();
        mockRestaurant.setId(1);
        mockRestaurant.setName("Restaurant 1");
        mockRestaurant.setAddress("Address 1");
        mockRestaurant.setCity("city 1");
        mockRestaurant.setRestaurantDescription("Desc 1");

        when(restaurantRepository.findById(mockRestaurantId)).thenReturn(Optional.of(mockRestaurant));

        RestaurantDTO response = restaurantService.fetchRestaurantById(mockRestaurantId);

        assertNotNull(response);
        assertEquals(mockRestaurantId, response.getId());
        assertEquals("Restaurant 1", response.getName());
        assertEquals("Address 1", response.getAddress());
        assertEquals("city 1", response.getCity());
        assertEquals("Desc 1", response.getRestaurantDescription());
        assertEquals(BASE_IMAGE_URL + "/1", response.getImageUrl());

        verify(restaurantRepository, times(1)).findById(mockRestaurantId);
    }

    @Test
    public void testFetchRestaurantById_NonExistingId() {
        Integer mockRestaurantId = 999;

        when(restaurantRepository.findById(mockRestaurantId)).thenReturn(Optional.empty());

        RestaurantNotFoundException exception = assertThrows(
                RestaurantNotFoundException.class,
                () -> restaurantService.fetchRestaurantById(mockRestaurantId)
        );

        assertEquals(
                "Restaurant not found with id: " + mockRestaurantId,
                exception.getMessage()
        );

        verify(restaurantRepository, times(1)).findById(mockRestaurantId);
    }

    // ============================================================
    // ===== NEW: updateRestaurant =====
    // ============================================================

    @Test
    public void testUpdateRestaurant_Success() {
        Integer id = 1;

        Restaurant existing = new Restaurant();
        existing.setId(id);
        existing.setName("Old Name");
        existing.setCity("Old City");

        RestaurantDTO updateDto = new RestaurantDTO();
        updateDto.setName("New Name");
        updateDto.setAddress("New Address");
        updateDto.setCity("New City");
        updateDto.setRestaurantDescription("New Desc");

        when(restaurantRepository.findById(id)).thenReturn(Optional.of(existing));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(existing);

        RestaurantDTO result = restaurantService.updateRestaurant(id, updateDto);

        assertNotNull(result);
        verify(restaurantRepository).save(argThat(r ->
                "New Name".equals(r.getName()) &&
                        "New City".equals(r.getCity()) &&
                        "New Address".equals(r.getAddress())
        ));
    }

    @Test
    public void testUpdateRestaurant_NotFound() {
        Integer id = 999;
        RestaurantDTO updateDto = new RestaurantDTO();

        when(restaurantRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class,
                () -> restaurantService.updateRestaurant(id, updateDto));

        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    // ============================================================
    // ===== NEW: deleteRestaurant =====
    // ============================================================

    @Test
    public void testDeleteRestaurant_WithImage() {
        Integer id = 1;
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setImageUrl("1.jpg");

        when(restaurantRepository.findById(id)).thenReturn(Optional.of(restaurant));

        restaurantService.deleteRestaurant(id);

        verify(fileStorageService).deleteFile("1.jpg");
        verify(restaurantRepository).deleteById(id);
    }

    @Test
    public void testDeleteRestaurant_WithoutImage() {
        Integer id = 1;
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setImageUrl(null);

        when(restaurantRepository.findById(id)).thenReturn(Optional.of(restaurant));

        restaurantService.deleteRestaurant(id);

        verify(fileStorageService, never()).deleteFile(anyString());
        verify(restaurantRepository).deleteById(id);
    }

    @Test
    public void testDeleteRestaurant_NotFound() {
        Integer id = 999;
        when(restaurantRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class,
                () -> restaurantService.deleteRestaurant(id));

        verify(restaurantRepository, never()).deleteById(anyInt());
    }
}