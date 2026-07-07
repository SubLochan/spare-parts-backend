package com.spareparts.repository;

import com.spareparts.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    
    Page<Order> findAll(Pageable pageable);
    
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    Page<Order> findBySupplier_Id(Long supplierId, Pageable pageable);
    
    Page<Order> findByPart_Id(Long partId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Page<Order> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate, 
                                Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.supplier.id = :supplierId " +
           "AND o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findSupplierOrdersByDateRange(@Param("supplierId") Long supplierId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
    
    List<Order> findByStatusAndDeliveryDateIsNull(Order.OrderStatus status);
}
