package com.food.order.service;

import com.food.order.dto.*;
import com.food.order.entity.Order;
import com.food.order.entity.OrderStatus;
import com.food.order.exception.OrderNotFoundException;
import com.food.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private SequenceGenerator sequenceGenerator;

    @Mock
    private CachedUserService cachedUserService;   // ← FIXED: Was UserService

    @Mock
    private OrderRepository orderRepo;

    @InjectMocks
    private OrderService orderService;

    private OrderDTOFromFE orderDetails;
    private UserDTO userDTO;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1);
        restaurant.setName("Test Restaurant");
        restaurant.setAddress("MG Road");
        restaurant.setCity("Mumbai");

        FoodItemsDTO foodItem = new FoodItemsDTO();
        foodItem.setId(1);
        foodItem.setItemName("Pizza");
        foodItem.setPrice(250L);
        foodItem.setQuantity(1);

        // ===== FIXED: Set all new fields =====
        orderDetails = new OrderDTOFromFE();
        orderDetails.setUserId(101);
        orderDetails.setFoodItemsList(Collections.singletonList(foodItem));
        orderDetails.setRestaurant(restaurant);
        orderDetails.setDeliveryAddress("123 Main St, Mumbai");
        orderDetails.setPaymentMethod("CASH");
        orderDetails.setTotalAmount(250.0);
        orderDetails.setDeliveryInstructions("Ring the bell");

        userDTO = new UserDTO();
        userDTO.setId(101);
        userDTO.setName("Test User");
        userDTO.setCity("Mumbai");

        // ===== FIXED: Use no-args constructor + setters =====
        savedOrder = new Order();
        savedOrder.setOrderId(1);
        savedOrder.setFoodItemsList(orderDetails.getFoodItemsList());
        savedOrder.setRestaurant(orderDetails.getRestaurant());
        savedOrder.setUserDTO(userDTO);
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setDeliveryAddress(orderDetails.getDeliveryAddress());
        savedOrder.setPaymentMethod(orderDetails.getPaymentMethod());
        savedOrder.setTotalAmount(orderDetails.getTotalAmount());
        savedOrder.setDeliveryInstructions(orderDetails.getDeliveryInstructions());
        savedOrder.setCreatedAt(LocalDateTime.now());
        savedOrder.setUpdatedAt(LocalDateTime.now());
    }

    // ============================================================
    // ===== SAVE ORDER TESTS =====
    // ============================================================

    @Test
    void saveOrderInDb_happyPath_returnsMappedOrderDTO() {
        // Arrange
        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(cachedUserService.getUserDetails(101)).thenReturn(userDTO);  // ← FIXED
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderDTO result = orderService.saveOrderInDb(orderDetails);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        verify(sequenceGenerator, times(1)).generateNextOrderId();
        verify(cachedUserService, times(1)).getUserDetails(101);  // ← FIXED
        verify(orderRepo, times(1)).save(any(Order.class));
    }

    @Test
    void saveOrderInDb_generatesUniqueOrderId_beforeCallingUserService() {
        when(sequenceGenerator.generateNextOrderId()).thenReturn(42);
        when(cachedUserService.getUserDetails(anyInt())).thenReturn(userDTO);  // ← FIXED
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        orderService.saveOrderInDb(orderDetails);

        InOrder inOrder = inOrder(sequenceGenerator, cachedUserService, orderRepo);
        inOrder.verify(sequenceGenerator).generateNextOrderId();
        inOrder.verify(cachedUserService).getUserDetails(anyInt());  // ← FIXED
        inOrder.verify(orderRepo).save(any(Order.class));
    }

    @Test
    void saveOrderInDb_callsUserServiceWithCorrectUserId() {
        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(cachedUserService.getUserDetails(anyInt())).thenReturn(userDTO);  // ← FIXED
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        orderDetails.setUserId(999);

        orderService.saveOrderInDb(orderDetails);

        verify(cachedUserService).getUserDetails(999);  // ← FIXED
    }

    @Test
    void saveOrderInDb_whenUserServiceReturnsNull_ordersSavedWithNullUser() {
        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(cachedUserService.getUserDetails(anyInt())).thenReturn(null);  // ← FIXED
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        assertDoesNotThrow(() -> orderService.saveOrderInDb(orderDetails));

        verify(orderRepo).save(any(Order.class));
    }

    @Test
    void saveOrderInDb_whenUserServiceThrows_propagatesException() {
        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(cachedUserService.getUserDetails(anyInt()))  // ← FIXED
                .thenThrow(new RuntimeException("USER-SERVICE unavailable"));

        assertThrows(RuntimeException.class,
                () -> orderService.saveOrderInDb(orderDetails));

        verify(orderRepo, never()).save(any(Order.class));
    }

    @Test
    void saveOrderInDb_whenRepoSaveFails_propagatesException() {
        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(cachedUserService.getUserDetails(anyInt())).thenReturn(userDTO);  // ← FIXED
        when(orderRepo.save(any(Order.class)))
                .thenThrow(new RuntimeException("DB write failed"));

        assertThrows(RuntimeException.class,
                () -> orderService.saveOrderInDb(orderDetails));
    }

    @Test
    void saveOrderInDb_withEmptyFoodItemsList_stillSavesOrder() {
        orderDetails.setFoodItemsList(Collections.emptyList());

        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(cachedUserService.getUserDetails(anyInt())).thenReturn(userDTO);  // ← FIXED
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        OrderDTO result = orderService.saveOrderInDb(orderDetails);

        assertNotNull(result);
        verify(orderRepo).save(any(Order.class));
    }

    @Test
    void saveOrderInDb_withMultipleFoodItems_savesAllItems() {
        orderDetails.setFoodItemsList(Arrays.asList(new FoodItemsDTO(), new FoodItemsDTO()));

        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(cachedUserService.getUserDetails(anyInt())).thenReturn(userDTO);  // ← FIXED
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        orderService.saveOrderInDb(orderDetails);

        verify(orderRepo).save(argThat(order ->
                order.getFoodItemsList() != null &&
                        order.getFoodItemsList().size() == 2));
    }

    @Test
    void saveOrderInDb_withNullRestaurant_stillCallsSave() {
        orderDetails.setRestaurant(null);

        when(sequenceGenerator.generateNextOrderId()).thenReturn(1);
        when(cachedUserService.getUserDetails(anyInt())).thenReturn(userDTO);  // ← FIXED
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        assertDoesNotThrow(() -> orderService.saveOrderInDb(orderDetails));

        verify(orderRepo).save(argThat(order -> order.getRestaurant() == null));
    }

    // ============================================================
    // ===== GET ORDER BY ID TESTS =====
    // ============================================================

    @Test
    void getOrderById_whenExists_returnsOrderDTO() {
        when(orderRepo.findByOrderId(1)).thenReturn(Optional.of(savedOrder));  // ← FIXED (was findById)

        OrderDTO result = orderService.getOrderById(1);

        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        verify(orderRepo).findByOrderId(1);
    }

    @Test
    void getOrderById_whenNotExists_throwsOrderNotFoundException() {
        when(orderRepo.findByOrderId(999)).thenReturn(Optional.empty());  // ← FIXED

        assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById(999));
    }

    // ============================================================
    // ===== CANCEL ORDER TESTS =====
    // ============================================================

    @Test
    void cancelOrder_whenPending_setsStatusToCancelled() {
        savedOrder.setStatus(OrderStatus.PENDING);
        when(orderRepo.findByOrderId(1)).thenReturn(Optional.of(savedOrder));
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        OrderDTO result = orderService.cancelOrder(1);

        assertNotNull(result);
        verify(orderRepo).save(argThat(order -> order.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    void cancelOrder_whenDelivered_throwsException() {
        savedOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepo.findByOrderId(1)).thenReturn(Optional.of(savedOrder));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.cancelOrder(1));

        assertTrue(exception.getMessage().contains("cannot be cancelled"));
        verify(orderRepo, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_whenAlreadyCancelled_throwsException() {
        savedOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepo.findByOrderId(1)).thenReturn(Optional.of(savedOrder));

        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(1));

        verify(orderRepo, never()).save(any(Order.class));
    }

    // ============================================================
    // ===== GET ORDER HISTORY TESTS =====
    // ============================================================

    @Test
    void getOrderHistory_returnsMappedList() {
        when(orderRepo.findByUserDTO_Id(101))
                .thenReturn(Arrays.asList(savedOrder, savedOrder));

        List<OrderDTO> result = orderService.getOrderHistory(101);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepo).findByUserDTO_Id(101);
    }

    @Test
    void getOrderHistory_whenNoOrders_returnsEmptyList() {
        when(orderRepo.findByUserDTO_Id(101)).thenReturn(Collections.emptyList());

        List<OrderDTO> result = orderService.getOrderHistory(101);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ============================================================
    // ===== GET ORDERS BY STATUS TESTS =====
    // ============================================================

    @Test
    void getOrdersByStatus_returnsMappedList() {
        when(orderRepo.findByStatus(OrderStatus.PENDING))
                .thenReturn(Collections.singletonList(savedOrder));

        List<OrderDTO> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepo).findByStatus(OrderStatus.PENDING);
    }

    // ============================================================
    // ===== UPDATE ORDER STATUS TESTS =====
    // ============================================================

    @Test
    void updateOrderStatus_validTransition_updatesStatus() {
        savedOrder.setStatus(OrderStatus.PENDING);
        when(orderRepo.findByOrderId(1)).thenReturn(Optional.of(savedOrder));
        when(orderRepo.save(any(Order.class))).thenReturn(savedOrder);

        OrderDTO result = orderService.updateOrderStatus(1, OrderStatus.CONFIRMED);

        assertNotNull(result);
        verify(orderRepo).save(argThat(order -> order.getStatus() == OrderStatus.CONFIRMED));
    }

    @Test
    void updateOrderStatus_invalidTransition_throwsException() {
        savedOrder.setStatus(OrderStatus.PENDING);
        when(orderRepo.findByOrderId(1)).thenReturn(Optional.of(savedOrder));

        assertThrows(RuntimeException.class,
                () -> orderService.updateOrderStatus(1, OrderStatus.DELIVERED));

        verify(orderRepo, never()).save(any(Order.class));
    }
}