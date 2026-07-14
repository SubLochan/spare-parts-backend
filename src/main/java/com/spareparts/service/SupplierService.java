package com.spareparts.service;

import com.spareparts.dto.SupplierDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierService {
    Page<SupplierDTO> getAllSuppliers(Pageable pageable);
    SupplierDTO getSupplierById(Long id);
    SupplierDTO createSupplier(SupplierDTO supplierDTO);
    SupplierDTO updateSupplier(Long id, SupplierDTO supplierDTO);
    void deleteSupplier(Long id);
    Page<SupplierDTO> searchSuppliers(String keyword, Pageable pageable);
}
