package com.spareparts.controller;

import com.spareparts.dto.OrderDTO;
import com.spareparts.entity.Order;
import com.spareparts.entity.Part;
import com.spareparts.entity.Supplier;
import com.spareparts.exception.ResourceNotFoundException;
import com.spareparts.repository.OrderRepository;
import com.spareparts.repository.PartRepository;
import com.spareparts.repository.SupplierRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private PartRepository partRepository;
    @Autowired private SupplierRepository supplierRepository;

    @GetMapping
    public Page<OrderDTO> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long partId,
            @RequestParam(defaultValue = "orderDate,desc") String sort) {

        String[] s = sort.split(",");
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(s.length > 1 ? s[1] : "desc"), s[0]));

        Page<Order> orders;
        if (status != null && !status.isBlank()) {
            orders = orderRepository.findByStatus(Order.OrderStatus.valueOf(status), pageable);
        } else if (supplierId != null) {
            orders = orderRepository.findBySupplier_Id(supplierId, pageable);
        } else if (partId != null) {
            orders = orderRepository.findByPart_Id(partId, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.map(this::toDTO);
    }

    @GetMapping("/{id}")
    public OrderDTO getById(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return toDTO(order);
    }

    @PostMapping
    public OrderDTO create(@Valid @RequestBody OrderDTO dto) {
        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        mapDtoToEntity(dto, order);
        return toDTO(orderRepository.save(order));
    }

    @PutMapping("/{id}")
    public OrderDTO update(@PathVariable Long id, @Valid @RequestBody OrderDTO dto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        mapDtoToEntity(dto, order);
        return toDTO(orderRepository.save(order));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found: " + id);
        }
        orderRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public OrderDTO updateStatus(@PathVariable Long id,
                                 @RequestParam String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        if (order.getStatus() == Order.OrderStatus.DELIVERED && order.getDeliveryDate() == null) {
            order.setDeliveryDate(LocalDateTime.now());
        }
        return toDTO(orderRepository.save(order));
    }

    private void mapDtoToEntity(OrderDTO dto, Order order) {
        if (dto.getPartId() != null) {
            Part part = partRepository.findById(dto.getPartId())
                    .orElseThrow(() -> new ResourceNotFoundException("Part not found: " + dto.getPartId()));
            order.setPart(part);
        }
        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + dto.getSupplierId()));
            order.setSupplier(supplier);
        }
        order.setQuantity(dto.getQuantity());
        order.setUnitPrice(dto.getUnitPrice());
        order.setRemarks(dto.getRemarks());
        if (dto.getStatus() != null) {
            order.setStatus(Order.OrderStatus.valueOf(dto.getStatus().toUpperCase()));
        }
        // deliveryDate is now String in DTO — parse to LocalDateTime
        if (dto.getDeliveryDate() != null && !dto.getDeliveryDate().isBlank()) {
            try {
                if (dto.getDeliveryDate().contains("T")) {
                    order.setDeliveryDate(LocalDateTime.parse(dto.getDeliveryDate()));
                } else {
                    order.setDeliveryDate(LocalDate.parse(dto.getDeliveryDate()).atStartOfDay());
                }
            } catch (Exception e) {
                // skip invalid date
            }
        }
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setPartId(order.getPart() != null ? order.getPart().getId() : null);
        dto.setPartName(order.getPart() != null ? order.getPart().getName() : null);
        dto.setSupplierId(order.getSupplier() != null ? order.getSupplier().getId() : null);
        dto.setSupplierName(order.getSupplier() != null ? order.getSupplier().getName() : null);
        dto.setQuantity(order.getQuantity());
        dto.setUnitPrice(order.getUnitPrice());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setRemarks(order.getRemarks());
        // deliveryDate is String in DTO — convert from LocalDateTime
        dto.setDeliveryDate(order.getDeliveryDate() != null
                ? order.getDeliveryDate().toLocalDate().toString() : null);
        dto.setOrderDate(order.getOrderDate());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }
}