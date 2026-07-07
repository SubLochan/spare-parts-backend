package com.spareparts.repository;

import com.spareparts.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByEmail(String email);
    
    Page<Supplier> findAll(Pageable pageable);
    
    Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    Page<Supplier> findByActive(Boolean active, Pageable pageable);
    
    Page<Supplier> findByCity(String city, Pageable pageable);
    
    Page<Supplier> findByCountry(String country, Pageable pageable);
    
    @Query("SELECT s FROM Supplier s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.city) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Supplier> searchSuppliers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    List<Supplier> findByActive(Boolean active);
}
