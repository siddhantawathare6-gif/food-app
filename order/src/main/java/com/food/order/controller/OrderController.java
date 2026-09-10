package com.food.order.controller;

import com.food.order.dto.OrderDTO;
import com.food.order.dto.OrderDTOFromFE;
import com.food.order.entity.OrderStatus;
import com.food.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // ===== NEW: Get Order History by User ID =====
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrderHistory(@PathVariable Integer userId) {
        log.info("GET /order/history/{} - Fetching order history", userId);
        List<OrderDTO> orders = orderService.getOrderHistory(userId);
        log.info("Found {} orders for userId: {}", orders.size(), userId);
        return ResponseEntity.ok(orders);
    }

    // ===== NEW: Get Order by ID =====
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer orderId) {
        log.info("GET /order/{} - Fetching order details", orderId);
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    // ===== NEW: Cancel Order =====
    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Integer orderId) {
        log.info("PUT /order/cancel/{} - Cancelling order", orderId);
        OrderDTO cancelledOrder = orderService.cancelOrder(orderId);
        log.info("Order cancelled successfully - orderId: {}", orderId);
        return ResponseEntity.ok(cancelledOrder);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("GET /order/status/{} - Fetching orders by status", status);
        List<OrderDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
}
