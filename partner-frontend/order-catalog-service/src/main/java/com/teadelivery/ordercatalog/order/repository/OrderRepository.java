package com.teadelivery.ordercatalog.order.repository;

import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
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
    List<Order> findByCustomerId(UUID customerId);
    Page<Order> findByCustomerIdOrderByOrderedAtDesc(UUID customerId, Pageable pageable);
    List<Order> findByBranch(VendorBranch branch);
    Page<Order> findByBranchOrderByOrderedAtDesc(VendorBranch branch, Pageable pageable);
    Page<Order> findByBranchAndOrderStatusOrderByOrderedAtDesc(VendorBranch branch, String status, Pageable pageable);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.customerId = :customerId ORDER BY o.orderedAt DESC")
    List<Order> findCustomerOrdersOptimized(UUID customerId);
    
    List<Order> findByBranchAndOrderedAtBetween(VendorBranch branch, LocalDateTime startTime, LocalDateTime endTime);
}
