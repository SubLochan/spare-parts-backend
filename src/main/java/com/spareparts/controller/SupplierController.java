package com.spareparts.controller;

import com.spareparts.dto.SupplierDTO;
import com.spareparts.entity.Supplier;
import com.spareparts.exception.ResourceNotFoundException;
import com.spareparts.repository.SupplierRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired private SupplierRepository supplierRepository;

    @GetMapping
    public Page<Supplier> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "id,desc") String sort) {

        String[] s = sort.split(",");
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(s.length > 1 ? s[1] : "desc"), s[0]));

        if (keyword != null && !keyword.isBlank()) {
            return supplierRepository.searchSuppliers(keyword, pageable);
        }
        if (active != null) {
            return supplierRepository.findByActive(active, pageable);
        }
        return supplierRepository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Supplier getById(@PathVariable Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
    }

    @PostMapping
    public Supplier create(@Valid @RequestBody SupplierDTO dto) {
        Supplier supplier = new Supplier();
        mapDtoToEntity(dto, supplier);
        return supplierRepository.save(supplier);
    }

    @PutMapping("/{id}")
    public Supplier update(@PathVariable Long id, @Valid @RequestBody SupplierDTO dto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
        mapDtoToEntity(dto, supplier);
        return supplierRepository.save(supplier);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found: " + id);
        }
        supplierRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public Page<Supplier> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return supplierRepository.searchSuppliers(keyword, pageable);
    }

    private void mapDtoToEntity(SupplierDTO dto, Supplier supplier) {
        supplier.setName(dto.getName());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());
        supplier.setCity(dto.getCity());
        supplier.setState(dto.getState());
        supplier.setPostalCode(dto.getPostalCode());
        supplier.setCountry(dto.getCountry());
        supplier.setDescription(dto.getDescription());
        supplier.setRating(dto.getRating());
        if (dto.getActive() != null) {
            supplier.setActive(dto.getActive());
        }
    }
}
