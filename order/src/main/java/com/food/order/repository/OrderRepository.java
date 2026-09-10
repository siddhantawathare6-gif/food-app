package com.food.order.repository;

import com.food.order.entity.Order;
import com.food.order.entity.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, Integer> {

    List<Order> findByUserDTO_Id(Integer userId);

    List<Order> findByUserDTO_IdAndStatus(Integer userId, String status);

    Optional<Order> findByOrderId(Integer orderId);

    List<Order> findByStatusNotIn(List<OrderStatus> cancelled);

    List<Order> findByStatus(OrderStatus status);
}
