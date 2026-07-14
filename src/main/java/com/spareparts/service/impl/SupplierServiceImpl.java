package com.spareparts.service.impl;

import com.spareparts.dto.SupplierDTO;
import com.spareparts.entity.Supplier;
import com.spareparts.exception.DuplicateResourceException;
import com.spareparts.exception.ResourceNotFoundException;
import com.spareparts.repository.SupplierRepository;
import com.spareparts.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public Page<SupplierDTO> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public SupplierDTO getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return convertToDTO(supplier);
    }

    @Override
    public SupplierDTO createSupplier(SupplierDTO supplierDTO) {
        if (supplierRepository.findByEmail(supplierDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Supplier with email " + supplierDTO.getEmail() + " already exists");
        }

        Supplier supplier = new Supplier();
        supplier.setName(supplierDTO.getName());
        supplier.setContactPerson(supplierDTO.getContactPerson());
        supplier.setEmail(supplierDTO.getEmail());
        supplier.setPhone(supplierDTO.getPhone());
        supplier.setAddress(supplierDTO.getAddress());
        supplier.setCity(supplierDTO.getCity());
        supplier.setState(supplierDTO.getState());
        supplier.setPostalCode(supplierDTO.getPostalCode());
        supplier.setCountry(supplierDTO.getCountry());
        supplier.setDescription(supplierDTO.getDescription());
        supplier.setActive(true);

        Supplier savedSupplier = supplierRepository.save(supplier);
        log.info("Supplier created: {}", savedSupplier.getName());
        return convertToDTO(savedSupplier);
    }

    @Override
    public SupplierDTO updateSupplier(Long id, SupplierDTO supplierDTO) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        supplier.setName(supplierDTO.getName());
        supplier.setContactPerson(supplierDTO.getContactPerson());
        supplier.setPhone(supplierDTO.getPhone());
        supplier.setAddress(supplierDTO.getAddress());
        supplier.setCity(supplierDTO.getCity());
        supplier.setState(supplierDTO.getState());
        supplier.setPostalCode(supplierDTO.getPostalCode());
        supplier.setCountry(supplierDTO.getCountry());
        supplier.setDescription(supplierDTO.getDescription());

        Supplier updatedSupplier = supplierRepository.save(supplier);
        log.info("Supplier updated: {}", id);
        return convertToDTO(updatedSupplier);
    }

    @Override
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        supplierRepository.delete(supplier);
        log.info("Supplier deleted: {}", id);
    }

    @Override
    public Page<SupplierDTO> searchSuppliers(String keyword, Pageable pageable) {
        return supplierRepository.searchSuppliers(keyword, pageable).map(this::convertToDTO);
    }

    private SupplierDTO convertToDTO(Supplier supplier) {
        SupplierDTO dto = new SupplierDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setContactPerson(supplier.getContactPerson());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        dto.setAddress(supplier.getAddress());
        dto.setCity(supplier.getCity());
        dto.setState(supplier.getState());
        dto.setPostalCode(supplier.getPostalCode());
        dto.setCountry(supplier.getCountry());
        dto.setDescription(supplier.getDescription());
        dto.setRating(supplier.getRating());
        dto.setActive(supplier.getActive());
        dto.setCreatedAt(supplier.getCreatedAt());
        dto.setUpdatedAt(supplier.getUpdatedAt());
        return dto;
    }
}
