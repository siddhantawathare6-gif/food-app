package com.food.order.service;

import com.food.order.dto.OrderDTO;
import com.food.order.dto.OrderDTOFromFE;
import com.food.order.dto.UserDTO;
import com.food.order.entity.Order;
import com.food.order.entity.OrderStatus;
import com.food.order.exception.OrderNotFoundException;
import com.food.order.mapper.OrderMapper;
import com.food.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        Order orderToBeSaved = new Order();
        orderToBeSaved.setOrderId(newOrderID);
        orderToBeSaved.setFoodItemsList(orderDetails.getFoodItemsList());
        orderToBeSaved.setRestaurant(orderDetails.getRestaurant());
        orderToBeSaved.setUserDTO(userDTO);

        // ===== SET NEW FIELDS =====
        orderToBeSaved.setStatus(OrderStatus.PENDING);
        orderToBeSaved.setDeliveryAddress(orderDetails.getDeliveryAddress());
        orderToBeSaved.setPaymentMethod(orderDetails.getPaymentMethod());
        orderToBeSaved.setTotalAmount(orderDetails.getTotalAmount());
        orderToBeSaved.setDeliveryInstructions(orderDetails.getDeliveryInstructions());

        LocalDateTime now = LocalDateTime.now();
        orderToBeSaved.setCreatedAt(now);
        orderToBeSaved.setUpdatedAt(now);

        Order savedOrder = orderRepo.save(orderToBeSaved);
        log.info("Order saved to database - orderId: {}, status: {}, total: {}",
                savedOrder.getOrderId(), savedOrder.getStatus(), savedOrder.getTotalAmount());

        return OrderMapper.INSTANCE.mapOrderToOrderDTO(orderToBeSaved);
    }

    public OrderDTO updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        log.info("Updating order status - orderId: {}, newStatus: {}", orderId, newStatus);

        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        // Set timestamp for specific status
        setStatusTimestamp(order, newStatus);

        Order updatedOrder = orderRepo.save(order);
        log.info("Order status updated - orderId: {}, status: {}",
                orderId, updatedOrder.getStatus());

        return OrderMapper.INSTANCE.mapOrderToOrderDTO(updatedOrder);
    }

    // ===== NEW: Get Order History by User ID =====
    public List<OrderDTO> getOrderHistory(Integer userId) {
        log.info("Fetching order history for userId: {}", userId);

        List<Order> orders = orderRepo.findByUserDTO_Id(userId);
        log.info("Found {} orders for userId: {}", orders.size(), userId);

        return orders.stream()
                .map(OrderMapper.INSTANCE::mapOrderToOrderDTO)
                .collect(Collectors.toList());
    }

    // ===== NEW: Get Order by ID =====
    public OrderDTO getOrderById(Integer orderId) {
        log.info("Fetching order by orderId: {}", orderId);

        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        log.info("Order found - orderId: {}, status: {}, total: {}", order.getOrderId(), order.getStatus(), order.getTotalAmount());
        return OrderMapper.INSTANCE.mapOrderToOrderDTO(order);
    }

    // ===== NEW: Cancel Order =====
    public OrderDTO cancelOrder(Integer orderId) {
        log.info("Cancelling order - orderId: {}", orderId);

        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        // Check if order can be cancelled
        if (!order.getStatus().isCancellable()) {
            throw new RuntimeException("Order cannot be cancelled. Current status: " + order.getStatus().getDisplayName());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        order.setCancelledAt(LocalDateTime.now());

        Order cancelledOrder = orderRepo.save(order);
        log.info("Order cancelled - orderId: {}", orderId);

        return OrderMapper.INSTANCE.mapOrderToOrderDTO(cancelledOrder);
    }

    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        log.info("Fetching orders by status: {}", status);

        List<Order> orders = orderRepo.findByStatus(status);
        log.info("Found {} orders with status: {}", orders.size(), status);

        return orders.stream()
                .map(OrderMapper.INSTANCE::mapOrderToOrderDTO)
                .collect(Collectors.toList());
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus newStatus) {
        // Prevent invalid transitions
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot change status of " + current + " order");
        }

        // Validate allowed transitions
        if (current == OrderStatus.PENDING) {
            if (newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED) {
                return;
            }
        } else if (current == OrderStatus.CONFIRMED) {
            if (newStatus == OrderStatus.PREPARING || newStatus == OrderStatus.CANCELLED) {
                return;
            }
        } else if (current == OrderStatus.PREPARING) {
            if (newStatus == OrderStatus.READY || newStatus == OrderStatus.CANCELLED) {
                return;
            }
        } else if (current == OrderStatus.READY) {
            if (newStatus == OrderStatus.OUT_FOR_DELIVERY || newStatus == OrderStatus.CANCELLED) {
                return;
            }
        } else if (current == OrderStatus.OUT_FOR_DELIVERY) {
            if (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED) {
                return;
            }
        }

        throw new RuntimeException("Invalid status transition from " + current + " to " + newStatus);
    }

    private void setStatusTimestamp(Order order, OrderStatus status) {
        switch (status) {
            case CONFIRMED:
                order.setConfirmedAt(LocalDateTime.now());
                break;
            case PREPARING:
                order.setPreparingAt(LocalDateTime.now());
                break;
            case READY:
                order.setReadyAt(LocalDateTime.now());
                break;
            case OUT_FOR_DELIVERY:
                order.setOutForDeliveryAt(LocalDateTime.now());
                break;
            case DELIVERED:
                order.setDeliveredAt(LocalDateTime.now());
                break;
            case CANCELLED:
                order.setCancelledAt(LocalDateTime.now());
                break;
            default:
                break;
        }
    }
}
