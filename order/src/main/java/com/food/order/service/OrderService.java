package com.food.order.service;

import com.food.order.dto.OrderDTO;
import com.food.order.dto.OrderDTOFromFE;
import com.food.order.dto.UserDTO;
import com.food.order.entity.Order;
import com.food.order.mapper.OrderMapper;
import com.food.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    OrderRepository orderRepo;

    @Autowired
    SequenceGenerator sequenceGenerator;

    @Autowired
    CachedUserService cachedUserService;


    public OrderDTO saveOrderInDb(OrderDTOFromFE orderDetails) {

        log.info("Processing order save for userId: {}, restaurantId: {}", orderDetails.getUserId(), orderDetails.getRestaurant());

        Integer newOrderID = sequenceGenerator.generateNextOrderId();
        UserDTO userDTO = cachedUserService.getUserDetails(orderDetails.getUserId());
        Order orderToBeSaved = new Order(newOrderID, orderDetails.getFoodItemsList(), orderDetails.getRestaurant(), userDTO);

        Order savedOrder = orderRepo.save(orderToBeSaved);
        log.info("Order saved to database - orderId: {}", savedOrder.getOrderId());

        return OrderMapper.INSTANCE.mapOrderToOrderDTO(orderToBeSaved);
    }
}
