package com.spareparts.service.impl;

import com.spareparts.dto.PartDTO;
import com.spareparts.entity.Part;
import com.spareparts.exception.DuplicateResourceException;
import com.spareparts.exception.ResourceNotFoundException;
import com.spareparts.repository.PartRepository;
import com.spareparts.repository.SupplierRepository;
import com.spareparts.service.PartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@Slf4j
public class PartServiceImpl implements PartService {

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public Page<PartDTO> getAllParts(Pageable pageable) {
        return partRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public PartDTO getPartById(Long id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + id));
        return convertToDTO(part);
    }

    @Override
    public PartDTO createPart(PartDTO partDTO) {
        if (partRepository.findBySku(partDTO.getSku()).isPresent()) {
            throw new DuplicateResourceException("Part with SKU " + partDTO.getSku() + " already exists");
        }

        Part part = new Part();
        part.setSku(partDTO.getSku());
        part.setName(partDTO.getName());
        part.setDescription(partDTO.getDescription());
        part.setQuantity(partDTO.getQuantity());
        part.setMinStockLevel(partDTO.getMinStockLevel());
        part.setUnitPrice(partDTO.getUnitPrice());
        part.setReorderPoint(partDTO.getReorderPoint());
        part.setCategory(partDTO.getCategory());
        part.setStatus(partDTO.getStatus() != null ? partDTO.getStatus() : "ACTIVE");

        if (partDTO.getSupplierId() != null) {
            part.setSupplier(supplierRepository.findById(partDTO.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found")));
        }

        Part savedPart = partRepository.save(part);
        log.info("Part created: {}", savedPart.getSku());
        return convertToDTO(savedPart);
    }

    @Override
    public PartDTO updatePart(Long id, PartDTO partDTO) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + id));

        part.setName(partDTO.getName());
        part.setDescription(partDTO.getDescription());
        part.setQuantity(partDTO.getQuantity());
        part.setMinStockLevel(partDTO.getMinStockLevel());
        part.setUnitPrice(partDTO.getUnitPrice());
        part.setStatus(partDTO.getStatus());

        Part updatedPart = partRepository.save(part);
        log.info("Part updated: {}", id);
        return convertToDTO(updatedPart);
    }

    @Override
    public void deletePart(Long id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part not found with id: " + id));
        partRepository.delete(part);
        log.info("Part deleted: {}", id);
    }

    @Override
    public Page<PartDTO> getLowStockParts(Pageable pageable) {
        return partRepository.findLowStockParts(pageable).map(this::convertToDTO);
    }

    @Override
    public Page<PartDTO> searchParts(String keyword, Pageable pageable) {
        return partRepository.searchParts(keyword, pageable).map(this::convertToDTO);
    }

    private PartDTO convertToDTO(Part part) {
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
