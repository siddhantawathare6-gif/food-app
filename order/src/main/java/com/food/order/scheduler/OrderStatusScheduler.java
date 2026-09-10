package com.food.order.scheduler;

import com.food.order.entity.Order;
import com.food.order.entity.OrderStatus;
import com.food.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@EnableScheduling
public class OrderStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusScheduler.class);

    @Autowired
    private OrderRepository orderRepository;

    @Value("${scheduler.order.status.delay:60000}")
    private long delayInMilliseconds;

    @Value("${scheduler.order.status.enabled:true}")
    private boolean schedulerEnabled;

    @Scheduled(fixedDelayString = "${scheduler.order.status.delay:60000}")
    @Transactional
    public void updateOrderStatus() {
        log.info("=== Order Status Scheduler Started ===");

        if (!schedulerEnabled) {
            log.info("Order Status Scheduler is disabled");
            return;
        }

        try {
            List<Order> activeOrders = orderRepository.findByStatusNotIn(
                    List.of(OrderStatus.CANCELLED, OrderStatus.DELIVERED)
            );
            log.info("Found {} active orders to process", activeOrders.size());
            for (Order order : activeOrders) {
                if (order.getCreatedAt() == null) {
                    log.warn("Skipping order {} because createdAt is null", order.getOrderId());
                    continue;
                }

                if (order.getStatus() == null) {
                    log.warn("Skipping order {} because status is null", order.getOrderId());
                    continue;
                }

                OrderStatus currentStatus = order.getStatus();
                OrderStatus newStatus = getStatusBasedOnTime(ChronoUnit.MINUTES.between(order.getCreatedAt(), LocalDateTime.now()));

                if (newStatus != currentStatus) {
                    order.setStatus(newStatus);
                    order.setUpdatedAt(LocalDateTime.now());
                    setStatusTimestamp(order, newStatus);
                    orderRepository.save(order);
                    log.info("Order {} status updated: {} → {}", order.getOrderId(), currentStatus.getDisplayName(), newStatus.getDisplayName());
                }
            }

            log.info("=== Order Status Scheduler Completed ===");
        } catch (Exception exception) {
            log.error("Error in Order Status Scheduler: {}", exception.getMessage(), exception);

        }
    }

    // ===== GET STATUS BASED ON TIME ELAPSED =====
    private OrderStatus getStatusBasedOnTime(long minutesSinceCreation) {
        // Simulate order progress based on minutes elapsed
        if (minutesSinceCreation < 2) {
            return OrderStatus.PENDING;
        } else if (minutesSinceCreation < 4) {
            return OrderStatus.CONFIRMED;
        } else if (minutesSinceCreation < 7) {
            return OrderStatus.PREPARING;
        } else if (minutesSinceCreation < 10) {
            return OrderStatus.READY;
        } else if (minutesSinceCreation < 14) {
            return OrderStatus.OUT_FOR_DELIVERY;
        } else {
            return OrderStatus.DELIVERED;
        }
    }

    // ===== SET STATUS TIMESTAMP =====
    private void setStatusTimestamp(Order order, OrderStatus status) {
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case CONFIRMED:
                order.setConfirmedAt(now);
                break;
            case PREPARING:
                order.setPreparingAt(now);
                break;
            case READY:
                order.setReadyAt(now);
                break;
            case OUT_FOR_DELIVERY:
                order.setOutForDeliveryAt(now);
                break;
            case DELIVERED:
                order.setDeliveredAt(now);
                break;
            default:
                break;
        }
    }

}
