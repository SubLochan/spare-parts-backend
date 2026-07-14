package com.spareparts.service;

import java.time.LocalDateTime;
import java.util.Map;

public interface ReportService {
    Map<String, Object> getInventoryReport(LocalDateTime fromDate, LocalDateTime toDate);
    Map<String, Object> getSalesReport(LocalDateTime fromDate, LocalDateTime toDate);
    Map<String, Object> getSupplierReport(LocalDateTime fromDate, LocalDateTime toDate);
    byte[] exportReport(String type, String format);
}
