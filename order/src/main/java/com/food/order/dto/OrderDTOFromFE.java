package com.food.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTOFromFE {

    private List<FoodItemsDTO> foodItemsList;
    private Integer userId;
    private Restaurant restaurant;

    private String deliveryAddress;
    private String paymentMethod;
    private Double totalAmount;
    private String deliveryInstructions;

}
