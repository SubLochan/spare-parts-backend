package com.spareparts.service.impl;

import com.spareparts.repository.OrderRepository;
import com.spareparts.repository.PartRepository;
import com.spareparts.repository.SupplierRepository;
import com.spareparts.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public Map<String, Object> getInventoryReport(LocalDateTime fromDate, LocalDateTime toDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("totalParts", partRepository.count());
        report.put("lowStockParts", partRepository.findLowStockParts().size());
        report.put("generatedAt", LocalDateTime.now());
        log.info("Inventory report generated");
        return report;
    }

    @Override
    public Map<String, Object> getSalesReport(LocalDateTime fromDate, LocalDateTime toDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("totalOrders", orderRepository.count());
        report.put("generatedAt", LocalDateTime.now());
        log.info("Sales report generated");
        return report;
    }

    @Override
    public Map<String, Object> getSupplierReport(LocalDateTime fromDate, LocalDateTime toDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("totalSuppliers", supplierRepository.count());
        report.put("activeSuppliers", supplierRepository.findByActive(true).size());
        report.put("generatedAt", LocalDateTime.now());
        log.info("Supplier report generated");
        return report;
    }

    @Override
    public byte[] exportReport(String type, String format) {
        String csv = "id,name,value\n1,Item1,100\n2,Item2,200\n";
        log.info("Report exported as {}", format);
        return csv.getBytes();
    }
}
