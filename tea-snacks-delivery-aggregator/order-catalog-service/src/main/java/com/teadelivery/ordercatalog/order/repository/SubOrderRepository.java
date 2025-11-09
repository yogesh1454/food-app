package com.teadelivery.ordercatalog.order.repository;

import com.teadelivery.ordercatalog.fsm.SubOrderState;
import com.teadelivery.ordercatalog.order.model.SubOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubOrderRepository extends JpaRepository<SubOrder, UUID> {
    
    List<SubOrder> findByParentOrderId(UUID parentOrderId);
    List<SubOrder> findByVendorId(UUID vendorId);
    List<SubOrder> findByBranchId(Long branchId);
    List<SubOrder> findByState(SubOrderState state);
    List<SubOrder> findByParentOrderIdAndState(UUID parentOrderId, SubOrderState state);
    List<SubOrder> findByVendorIdAndState(UUID vendorId, SubOrderState state);
    
    long countByParentOrderId(UUID parentOrderId);
    long countByVendorIdAndState(UUID vendorId, SubOrderState state);
}
