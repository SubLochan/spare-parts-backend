package com.spareparts.service.impl;

import com.spareparts.dto.OrderDTO;
import com.spareparts.entity.Order;
import com.spareparts.entity.Order.OrderStatus;
import com.spareparts.exception.ResourceNotFoundException;
import com.spareparts.repository.OrderRepository;
import com.spareparts.repository.PartRepository;
import com.spareparts.repository.SupplierRepository;
import com.spareparts.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return convertToDTO(order);
    }

    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setPart(partRepository.findById(orderDTO.getPartId())
                .orElseThrow(() -> new ResourceNotFoundException("Part not found")));
        order.setSupplier(supplierRepository.findById(orderDTO.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found")));
        order.setQuantity(orderDTO.getQuantity());
        order.setUnitPrice(orderDTO.getUnitPrice());
        order.setStatus(OrderStatus.PENDING);
        order.setRemarks(orderDTO.getRemarks());

        Order savedOrder = orderRepository.save(order);
        log.info("Order created: {}", savedOrder.getOrderNumber());
        return convertToDTO(savedOrder);
    }

    @Override
    public OrderDTO updateOrder(Long id, OrderDTO orderDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setQuantity(orderDTO.getQuantity());
        order.setUnitPrice(orderDTO.getUnitPrice());
        order.setRemarks(orderDTO.getRemarks());

        Order updatedOrder = orderRepository.save(order);
        log.info("Order updated: {}", id);
        return convertToDTO(updatedOrder);
    }

    @Override
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        orderRepository.delete(order);
        log.info("Order deleted: {}", id);
    }

    @Override
    public OrderDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setStatus(OrderStatus.valueOf(status.toUpperCase()));

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated: {} -> {}", id, status);
        return convertToDTO(updatedOrder);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setPartId(order.getPart().getId());
        dto.setPartName(order.getPart().getName());
        dto.setSupplierId(order.getSupplier().getId());
        dto.setSupplierName(order.getSupplier().getName());
        dto.setQuantity(order.getQuantity());
        dto.setUnitPrice(order.getUnitPrice());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().getValue());
        dto.setRemarks(order.getRemarks());
        dto.setDeliveryDate(order.getDeliveryDate());
        dto.setOrderDate(order.getOrderDate());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }
}
