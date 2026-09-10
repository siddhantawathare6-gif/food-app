package com.food.order.entity;

import com.food.order.dto.FoodItemsDTO;
import com.food.order.dto.Restaurant;
import com.food.order.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("order")
public class Order {

    @Id
    private String id;

    private Integer orderId;
    private List<FoodItemsDTO> foodItemsList;
    private Restaurant restaurant;
    private UserDTO userDTO;
    private OrderStatus status;

    private String deliveryAddress;
    private String paymentMethod;
    private Double totalAmount;
    private String deliveryInstructions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime readyAt;
    private LocalDateTime outForDeliveryAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
}
