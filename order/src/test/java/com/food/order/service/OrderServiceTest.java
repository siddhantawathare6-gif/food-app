package com.food.order.service;

import com.food.order.dto.*;
import com.food.order.entity.Order;
import com.food.order.repository.OrderRepository;
import com.food.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private SequenceGenerator sequenceGenerator;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OrderRepository orderRepo;

    @InjectMocks
    private OrderService orderService;

    private OrderDTOFromFE orderDetails;
    private UserDTO userDTO;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        Restaurant restaurant = new Restaurant(); // adjust fields if Restaurant needs specific setup
        FoodItemsDTO foodItem = new FoodItemsDTO(); // adjust fields if FoodItemsDTO needs specific setup

        orderDetails = new OrderDTOFromFE();
        orderDetails.setUserId(101);
        orderDetails.setFoodItemsList(Collections.singletonList(foodItem));
        orderDetails.setRestaurant(restaurant);

        userDTO = new UserDTO();
        userDTO.setId(101);
        userDTO.setName("Test User");
        userDTO.setAddress("123 Main St");
        userDTO.setCity("Mumbai");

        savedOrder = new Order(1, orderDetails.getFoodItemsList(), orderDetails.getRestaurant(), userDTO);
    }

    @Test
    void saveOrderInDb_happyPath_returnsMappedOrderDTO() {
        // Arrange
        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(restTemplate.getForObject(
                eq("http://USER-SERVICE/user/fetchUserById/101"),
                eq(UserDTO.class)))
                .thenReturn(userDTO);
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        // Act — the real OrderMapper.INSTANCE runs here; it's a pure mapping,
        // so no mocking needed.
        OrderDTO result = orderService.saveOrderInDb(orderDetails);

        // Assert
        assertNotNull(result);
        verify(sequenceGenerator, times(1)).generateNextOrderId();
        verify(restTemplate, times(1))
                .getForObject("http://USER-SERVICE/user/fetchUserById/101", UserDTO.class);
        verify(orderRepo, times(1)).save(any(Order.class));
    }

    @Test
    void saveOrderInDb_generatesUniqueOrderId_beforeCallingUserService() {
        when(sequenceGenerator.generateNextOrderId()).thenReturn(42);
        when(restTemplate.getForObject(anyString(), eq(UserDTO.class))).thenReturn(userDTO);
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        orderService.saveOrderInDb(orderDetails);

        InOrder inOrder = inOrder(sequenceGenerator, restTemplate, orderRepo);
        inOrder.verify(sequenceGenerator).generateNextOrderId();
        inOrder.verify(restTemplate).getForObject(anyString(), eq(UserDTO.class));
        inOrder.verify(orderRepo).save(any(Order.class));
    }

    @Test
    void saveOrderInDb_callsUserServiceWithCorrectUserId() {
        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(restTemplate.getForObject(anyString(), eq(UserDTO.class))).thenReturn(userDTO);
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        orderDetails.setUserId(999);

        orderService.saveOrderInDb(orderDetails);

        verify(restTemplate).getForObject(
                "http://USER-SERVICE/user/fetchUserById/999", UserDTO.class);
    }

    @Test
    void saveOrderInDb_whenUserServiceReturnsNull_ordersSavedWithNullUser() {
        // Simulates USER-SERVICE returning no user (e.g. user not found).
        // Document/confirm this is the desired behavior; consider throwing
        // a custom exception instead if a null user should be treated as an error.
        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(restTemplate.getForObject(anyString(), eq(UserDTO.class))).thenReturn(null);
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        assertDoesNotThrow(() -> orderService.saveOrderInDb(orderDetails));

        verify(orderRepo).save(any(Order.class));
    }

    @Test
    void saveOrderInDb_whenUserServiceThrows_propagatesException() {
        // If USER-SERVICE (via Eureka/RestTemplate) is down or errors out,
        // the exception should propagate rather than silently saving a bad order.
        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(restTemplate.getForObject(anyString(), eq(UserDTO.class)))
                .thenThrow(new RuntimeException("USER-SERVICE unavailable"));

        assertThrows(RuntimeException.class,
                () -> orderService.saveOrderInDb(orderDetails));

        verify(orderRepo, never()).save(any(Order.class));
    }

    @Test
    void saveOrderInDb_whenRepoSaveFails_propagatesException() {
        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(restTemplate.getForObject(anyString(), eq(UserDTO.class))).thenReturn(userDTO);
        when(orderRepo.save(any(Order.class)))
                .thenThrow(new RuntimeException("DB write failed"));

        assertThrows(RuntimeException.class,
                () -> orderService.saveOrderInDb(orderDetails));
    }

    @Test
    void saveOrderInDb_withEmptyFoodItemsList_stillSavesOrder() {
        orderDetails.setFoodItemsList(Collections.emptyList());

        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(restTemplate.getForObject(anyString(), eq(UserDTO.class))).thenReturn(userDTO);
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        OrderDTO result = orderService.saveOrderInDb(orderDetails);

        assertNotNull(result);
        verify(orderRepo).save(any(Order.class));
    }

    @Test
    void saveOrderInDb_withMultipleFoodItems_savesAllItems() {
        orderDetails.setFoodItemsList(Arrays.asList(new FoodItemsDTO(), new FoodItemsDTO()));

        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(restTemplate.getForObject(anyString(), eq(UserDTO.class))).thenReturn(userDTO);
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        orderService.saveOrderInDb(orderDetails);

        verify(orderRepo).save(argThat(order ->
                order.getFoodItemsList() != null &&
                        order.getFoodItemsList().size() == 2));
    }

    @Test
    void saveOrderInDb_withNullRestaurant_stillCallsSave() {
        // Guards against a null Restaurant on the incoming request silently
        // slipping through to persistence without validation.
        orderDetails.setRestaurant(null);

        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(restTemplate.getForObject(anyString(), eq(UserDTO.class))).thenReturn(userDTO);
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        assertDoesNotThrow(() -> orderService.saveOrderInDb(orderDetails));

        verify(orderRepo).save(argThat(order -> order.getRestaurant() == null));
    }
}
