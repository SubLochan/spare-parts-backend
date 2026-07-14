package com.spareparts.controller;

import com.spareparts.entity.Order;
import com.spareparts.entity.Part;
import com.spareparts.entity.Supplier;
import com.spareparts.repository.OrderRepository;
import com.spareparts.repository.PartRepository;
import com.spareparts.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired private PartRepository partRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private SupplierRepository supplierRepository;

    /** Dashboard metrics — hit on home/dashboard page load */
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        List<Part> allParts = partRepository.findAll();
        List<Order> allOrders = orderRepository.findAll();
        List<Supplier> allSuppliers = supplierRepository.findAll();

        long totalParts = allParts.size();
        long lowStockCount = allParts.stream().filter(Part::isLowStock).count();
        long activeSuppliers = allSuppliers.stream().filter(s -> Boolean.TRUE.equals(s.getActive())).count();
        long pendingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PENDING).count();
        BigDecimal totalInventoryValue = allParts.stream()
                .map(p -> p.getTotalValue() != null ? p.getTotalValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalParts", totalParts);
        metrics.put("lowStockCount", lowStockCount);
        metrics.put("activeSuppliers", activeSuppliers);
        metrics.put("pendingOrders", pendingOrders);
        metrics.put("totalInventoryValue", totalInventoryValue);
        metrics.put("totalOrders", allOrders.size());
        return metrics;
    }

    /** Low-stock alerts */
    @GetMapping("/stock-alerts")
    public List<Part> getStockAlerts() {
        return partRepository.findLowStockParts();
    }

    /** Inventory report */
    @GetMapping("/inventory")
    public Map<String, Object> getInventoryReport(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {

        List<Part> parts = partRepository.findAll();

        if (category != null && !category.isBlank()) {
            parts = parts.stream().filter(p -> category.equalsIgnoreCase(p.getCategory())).toList();
        }
        if (status != null && !status.isBlank()) {
            parts = parts.stream().filter(p -> status.equalsIgnoreCase(p.getStatus())).toList();
        }

        BigDecimal totalValue = parts.stream()
                .map(p -> p.getTotalValue() != null ? p.getTotalValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("generatedAt", LocalDateTime.now());
        report.put("totalParts", parts.size());
        report.put("totalInventoryValue", totalValue);
        report.put("lowStockItems", parts.stream().filter(Part::isLowStock).count());
        report.put("parts", parts);
        return report;
    }

    /** Sales/order report */
    @GetMapping("/sales")
    public Map<String, Object> getSalesReport(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {

        List<Order> orders = orderRepository.findAll();

        BigDecimal totalRevenue = orders.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("generatedAt", LocalDateTime.now());
        report.put("totalOrders", orders.size());
        report.put("totalRevenue", totalRevenue);
        report.put("orders", orders);
        return report;
    }

    /** Supplier report */
    @GetMapping("/suppliers")
    public Map<String, Object> getSupplierReport() {
        List<Supplier> suppliers = supplierRepository.findAll();

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("generatedAt", LocalDateTime.now());
        report.put("totalSuppliers", suppliers.size());
        report.put("activeSuppliers", suppliers.stream().filter(s -> Boolean.TRUE.equals(s.getActive())).count());
        report.put("suppliers", suppliers);
        return report;
    }

    /** Order report */
    @GetMapping("/orders")
    public Map<String, Object> getOrderReport(
            @RequestParam(required = false) String status) {

        List<Order> orders = orderRepository.findAll();
        if (status != null && !status.isBlank()) {
            orders = orders.stream()
                    .filter(o -> o.getStatus().name().equalsIgnoreCase(status)).toList();
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("generatedAt", LocalDateTime.now());
        report.put("totalOrders", orders.size());
        report.put("orders", orders);
        return report;
    }

    /** CSV export */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam String reportType,
            @RequestParam(defaultValue = "csv") String format) {

        StringBuilder csv = new StringBuilder();

        switch (reportType.toUpperCase()) {
            case "INVENTORY" -> {
                csv.append("ID,SKU,Name,Category,Quantity,MinStockLevel,UnitPrice,TotalValue,Status\n");
                partRepository.findAll().forEach(p ->
                        csv.append(String.join(",",
                                str(p.getId()), str(p.getSku()), str(p.getName()),
                                str(p.getCategory()), str(p.getQuantity()), str(p.getMinStockLevel()),
                                str(p.getUnitPrice()), str(p.getTotalValue()), str(p.getStatus())
                        )).append("\n"));
            }
            case "ORDERS", "SALES" -> {
                csv.append("ID,OrderNumber,Part,Supplier,Quantity,UnitPrice,TotalAmount,Status,OrderDate\n");
                orderRepository.findAll().forEach(o ->
                        csv.append(String.join(",",
                                str(o.getId()), str(o.getOrderNumber()),
                                o.getPart() != null ? o.getPart().getName() : "",
                                o.getSupplier() != null ? o.getSupplier().getName() : "",
                                str(o.getQuantity()), str(o.getUnitPrice()),
                                str(o.getTotalAmount()), str(o.getStatus()),
                                str(o.getOrderDate())
                        )).append("\n"));
            }
            case "SUPPLIER" -> {
                csv.append("ID,Name,ContactPerson,Email,Phone,City,Country,Active\n");
                supplierRepository.findAll().forEach(s ->
                        csv.append(String.join(",",
                                str(s.getId()), str(s.getName()), str(s.getContactPerson()),
                                str(s.getEmail()), str(s.getPhone()),
                                str(s.getCity()), str(s.getCountry()), str(s.getActive())
                        )).append("\n"));
            }
        }

        byte[] data = csv.toString().getBytes();
        String filename = reportType.toLowerCase() + "_report." + format;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    private String str(Object val) {
        return val != null ? val.toString().replace(",", ";") : "";
    }
}
