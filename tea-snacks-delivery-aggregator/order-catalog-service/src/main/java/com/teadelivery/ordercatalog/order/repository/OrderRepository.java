package com.teadelivery.ordercatalog.order.repository;

import com.teadelivery.ordercatalog.fsm.OrderState;
import com.teadelivery.ordercatalog.fsm.OrderType;
import com.teadelivery.ordercatalog.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    // ========== Customer Queries ==========
    List<Order> findByCustomerId(UUID customerId);
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.customerId = :customerId ORDER BY o.createdAt DESC")
    List<Order> findCustomerOrdersWithItems(UUID customerId);
    
    // ========== State Queries ==========
    List<Order> findByState(OrderState state);
    Page<Order> findByStateOrderByCreatedAtDesc(OrderState state, Pageable pageable);
    List<Order> findByStateIn(List<OrderState> states);
    
    // ========== Order Type Queries ==========
    List<Order> findByOrderType(OrderType orderType);
    List<Order> findByParentOrderId(UUID parentOrderId);
    
    // ========== Time Range Queries ==========
    List<Order> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    Page<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    // ========== Combined Queries ==========
    List<Order> findByCustomerIdAndState(UUID customerId, OrderState state);
    Page<Order> findByCustomerIdAndStateOrderByCreatedAtDesc(UUID customerId, OrderState state, Pageable pageable);
    
    // ========== Count Queries ==========
    long countByCustomerId(UUID customerId);
    long countByState(OrderState state);
    long countByCustomerIdAndState(UUID customerId, OrderState state);
}
