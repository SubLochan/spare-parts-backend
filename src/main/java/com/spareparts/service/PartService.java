package com.spareparts.service;

import com.spareparts.dto.PartDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PartService {
    Page<PartDTO> getAllParts(Pageable pageable);
    PartDTO getPartById(Long id);
    PartDTO createPart(PartDTO partDTO);
    PartDTO updatePart(Long id, PartDTO partDTO);
    void deletePart(Long id);
    Page<PartDTO> getLowStockParts(Pageable pageable);
    Page<PartDTO> searchParts(String keyword, Pageable pageable);
}
