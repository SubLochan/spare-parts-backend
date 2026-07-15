package com.spareparts.controller;

import com.spareparts.entity.Order;
import com.spareparts.entity.Part;
import com.spareparts.entity.Supplier;
import com.spareparts.repository.OrderRepository;
import com.spareparts.repository.PartRepository;
import com.spareparts.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired private PartRepository partRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private SupplierRepository supplierRepository;

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        List<Part> allParts = partRepository.findAll();
        List<Order> allOrders = orderRepository.findAll();
        List<Supplier> allSuppliers = supplierRepository.findAll();

        long totalParts = allParts.size();
        long lowStockCount = allParts.stream().filter(Part::isLowStock).count();
        long outOfStockCount = allParts.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() == 0).count();
        long inStockCount = totalParts - lowStockCount - outOfStockCount;
        long activeSuppliers = allSuppliers.stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive())).count();
        long pendingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PENDING).count();
        BigDecimal totalInventoryValue = allParts.stream()
                .map(p -> p.getTotalValue() != null ? p.getTotalValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Monthly orders data for bar chart (last 6 months)
        List<Map<String, Object>> monthlyOrdersData = buildMonthlyOrdersData(allOrders);

        // Stock distribution for pie chart
        List<Map<String, Object>> stockDistribution = new ArrayList<>();
        stockDistribution.add(Map.of("name", "In Stock", "value", inStockCount));
        stockDistribution.add(Map.of("name", "Low Stock", "value", lowStockCount));
        stockDistribution.add(Map.of("name", "Out of Stock", "value", outOfStockCount));

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalParts", totalParts);
        metrics.put("lowStockParts", lowStockCount);
        metrics.put("totalOrders", allOrders.size());
        metrics.put("pendingOrders", pendingOrders);
        metrics.put("totalSuppliers", activeSuppliers);
        metrics.put("totalInventoryValue", totalInventoryValue);
        metrics.put("monthlyOrdersData", monthlyOrdersData);
        metrics.put("stockDistribution", stockDistribution);
        return metrics;
    }

    private List<Map<String, Object>> buildMonthlyOrdersData(List<Order> allOrders) {
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDateTime month = now.minusMonths(i);
            int year = month.getYear();
            Month m = month.getMonth();

            long count = allOrders.stream()
                    .filter(o -> o.getOrderDate() != null
                            && o.getOrderDate().getYear() == year
                            && o.getOrderDate().getMonth() == m)
                    .count();

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("month", m.getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            entry.put("orders", count);
            result.add(entry);
        }
        return result;
    }

    @GetMapping("/stock-alerts")
    public List<Part> getStockAlerts() {
        return partRepository.findLowStockParts();
    }

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

    @GetMapping("/sales")
    public Map<String, Object> getSalesReport() {
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

    @GetMapping("/suppliers")
    public Map<String, Object> getSupplierReport() {
        List<Supplier> suppliers = supplierRepository.findAll();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("generatedAt", LocalDateTime.now());
        report.put("totalSuppliers", suppliers.size());
        report.put("activeSuppliers", suppliers.stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive())).count());
        report.put("suppliers", suppliers);
        return report;
    }

    @GetMapping("/orders")
    public Map<String, Object> getOrderReport(@RequestParam(required = false) String status) {
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
                                str(p.getCategory()), str(p.getQuantity()),
                                str(p.getMinStockLevel()), str(p.getUnitPrice()),
                                str(p.getTotalValue()), str(p.getStatus())
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
                                str(o.getTotalAmount()), str(o.getStatus()), str(o.getOrderDate())
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
