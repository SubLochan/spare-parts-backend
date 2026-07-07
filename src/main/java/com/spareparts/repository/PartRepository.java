package com.spareparts.repository;

import com.spareparts.entity.Part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {
    Optional<Part> findBySku(String sku);
    
    Page<Part> findAll(Pageable pageable);
    
    Page<Part> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    @Query("SELECT p FROM Part p WHERE p.quantity <= p.minStockLevel")
    List<Part> findLowStockParts();
    
    @Query("SELECT p FROM Part p WHERE p.quantity <= p.minStockLevel")
    Page<Part> findLowStockParts(Pageable pageable);
    
    Page<Part> findBySupplier_Id(Long supplierId, Pageable pageable);
    
    Page<Part> findByStatus(String status, Pageable pageable);
    
    Page<Part> findByCategory(String category, Pageable pageable);
    
    @Query("SELECT p FROM Part p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Part> searchParts(@Param("searchTerm") String searchTerm, Pageable pageable);
}
