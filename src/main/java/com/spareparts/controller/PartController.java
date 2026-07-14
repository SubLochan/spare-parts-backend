package com.spareparts.controller;

import com.spareparts.dto.PartDTO;
import com.spareparts.entity.Part;
import com.spareparts.entity.Supplier;
import com.spareparts.exception.ResourceNotFoundException;
import com.spareparts.repository.PartRepository;
import com.spareparts.repository.SupplierRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parts")
public class PartController {

    @Autowired private PartRepository partRepository;
    @Autowired private SupplierRepository supplierRepository;

    @GetMapping
    public Page<Part> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "id,desc") String sort) {

        String[] s = sort.split(",");
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(s.length > 1 ? s[1] : "desc"), s[0]));

        if (keyword != null && !keyword.isBlank()) {
            return partRepository.searchParts(keyword, pageable);
        }
        if (status != null && !status.isBlank()) {
            return partRepository.findByStatus(status, pageable);
        }
        if (category != null && !category.isBlank()) {
            return partRepository.findByCategory(category, pageable);
        }
        return partRepository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Part getById(@PathVariable Long id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found: " + id));
    }

    @PostMapping
    public Part create(@Valid @RequestBody PartDTO dto) {
        Part part = new Part();
        mapDtoToEntity(dto, part);
        return partRepository.save(part);
    }

    @PutMapping("/{id}")
    public Part update(@PathVariable Long id, @Valid @RequestBody PartDTO dto) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found: " + id));
        mapDtoToEntity(dto, part);
        return partRepository.save(part);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!partRepository.existsById(id)) {
            throw new ResourceNotFoundException("Part not found: " + id);
        }
        partRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/low-stock")
    public java.util.List<Part> getLowStock() {
        return partRepository.findLowStockParts();
    }

    @GetMapping("/search")
    public Page<Part> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return partRepository.searchParts(keyword, pageable);
    }

    private void mapDtoToEntity(PartDTO dto, Part part) {
        part.setSku(dto.getSku());
        part.setName(dto.getName());
        part.setDescription(dto.getDescription());
        part.setQuantity(dto.getQuantity());
        part.setMinStockLevel(dto.getMinStockLevel());
        part.setUnitPrice(dto.getUnitPrice());
        part.setStatus(dto.getStatus());
        part.setCategory(dto.getCategory());
        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + dto.getSupplierId()));
            part.setSupplier(supplier);
        }
    }
}
