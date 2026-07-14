package com.spareparts.service.impl;

import com.spareparts.dto.PartDTO;
import com.spareparts.entity.Part;
import com.spareparts.entity.Supplier;
import com.spareparts.exception.DuplicateResourceException;
import com.spareparts.exception.ResourceNotFoundException;
import com.spareparts.repository.PartRepository;
import com.spareparts.repository.SupplierRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
public class PartServiceImpl {

    @Autowired private PartRepository partRepository;
    @Autowired private SupplierRepository supplierRepository;

    public Page<PartDTO> getAllParts(Pageable pageable) {
        return partRepository.findAll(pageable).map(this::toDTO);
    }

    public PartDTO getPartById(Long id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found: " + id));
        return toDTO(part);
    }

    public PartDTO createPart(PartDTO partDTO) {
        if (partRepository.existsBySku(partDTO.getSku())) {
            throw new DuplicateResourceException("SKU already exists: " + partDTO.getSku());
        }
        Part part = new Part();
        mapDtoToEntity(partDTO, part);
        Part savedPart = partRepository.save(part);
        log.info("Created part with SKU: {}", savedPart.getSku());
        return toDTO(savedPart);
    }

    public PartDTO updatePart(Long id, PartDTO partDTO) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found: " + id));
        mapDtoToEntity(partDTO, part);
        Part savedPart = partRepository.save(part);
        log.info("Updated part: {}", savedPart.getName());
        return toDTO(savedPart);
    }

    public void deletePart(Long id) {
        if (!partRepository.existsById(id)) {
            throw new ResourceNotFoundException("Part not found: " + id);
        }
        partRepository.deleteById(id);
        log.info("Deleted part: {}", id);
    }

    public List<Part> getLowStockParts() {
        return partRepository.findLowStockParts();
    }

    public Page<PartDTO> searchParts(String keyword, Pageable pageable) {
        return partRepository.searchParts(keyword, pageable).map(this::toDTO);
    }

    private void mapDtoToEntity(PartDTO dto, Part part) {
        part.setSku(dto.getSku());
        part.setName(dto.getName());
        part.setDescription(dto.getDescription());
        part.setQuantity(dto.getQuantity());
        part.setMinStockLevel(dto.getMinStockLevel());
        part.setUnitPrice(dto.getUnitPrice());
        part.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        part.setCategory(dto.getCategory());
        if (dto.getReorderPoint() != null) part.setReorderPoint(dto.getReorderPoint());
        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + dto.getSupplierId()));
            part.setSupplier(supplier);
        }
    }

    public PartDTO toDTO(Part part) {
        PartDTO dto = new PartDTO();
        dto.setId(part.getId());
        dto.setSku(part.getSku());
        dto.setName(part.getName());
        dto.setDescription(part.getDescription());
        dto.setQuantity(part.getQuantity());
        dto.setMinStockLevel(part.getMinStockLevel());
        dto.setUnitPrice(part.getUnitPrice());
        dto.setTotalValue(part.getTotalValue());
        dto.setReorderPoint(part.getReorderPoint());
        dto.setCategory(part.getCategory());
        dto.setStatus(part.getStatus());
        if (part.getSupplier() != null) {
            dto.setSupplierId(part.getSupplier().getId());
            dto.setSupplierName(part.getSupplier().getName());
        }
        dto.setCreatedAt(part.getCreatedAt());
        dto.setUpdatedAt(part.getUpdatedAt());
        return dto;
    }
}
