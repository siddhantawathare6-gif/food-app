package com.food.order.controller;

import com.food.order.dto.OrderDTO;
import com.food.order.dto.OrderDTOFromFE;
import com.food.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
//@CrossOrigin (now that the gateway handles it centrally)
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    OrderService orderService;

    @PostMapping("/saveOrder")
    public ResponseEntity<OrderDTO> saveOrder(@RequestBody OrderDTOFromFE orderDetails) {

        log.info("POST /order/saveOrder - New order request received");

        OrderDTO orderSavedInDB = orderService.saveOrderInDb(orderDetails);

        log.info("Order created successfully - orderId: {}, restaurant: {}, foodItemsList: {}", orderSavedInDB.getOrderId(), orderSavedInDB.getRestaurant(), orderSavedInDB.getFoodItemsList());

        return new ResponseEntity<>(orderSavedInDB, HttpStatus.CREATED);
    }

}
