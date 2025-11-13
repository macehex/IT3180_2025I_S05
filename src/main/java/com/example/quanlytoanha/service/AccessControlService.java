// Vị trí: src/main/java/com/example/quanlytoanha/service/AccessControlService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.AccessControlDAO;
import com.example.quanlytoanha.dao.VehicleDAO; // <-- BỔ SUNG: Import DAO mới
import com.example.quanlytoanha.model.VehicleAccessLog;
import com.example.quanlytoanha.model.VisitorLog;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class AccessControlService {

    private final AccessControlDAO accessControlDAO = new AccessControlDAO();

    // --- BỔ SUNG: Khởi tạo VehicleDAO ---
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    // ==========================================================
    // AC1: GHI NHẬN RA/VÀO (Giữ nguyên)
    // ==========================================================

    public boolean checkInVisitor(VisitorLog log) throws SQLException {
        if (log.getVisitorName() == null || log.getVisitorName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách không được để trống.");
        }
        return accessControlDAO.checkInVisitor(log);
    }

    public boolean checkOutVisitor(int logId) throws SQLException {
        return accessControlDAO.checkOutVisitor(logId);
    }

    // --- SỬA LỖI (US8_1_1 - AC3): Tích hợp logic Xác thực Xe ---
    public boolean logVehicleAccess(VehicleAccessLog log) throws SQLException {
        if (log.getLicensePlate() == null || log.getLicensePlate().trim().isEmpty()) {
            throw new IllegalArgumentException("Biển số xe không được để trống.");
        }

        // --- Logic AC3 (Xác thực): ---
        // 1. Tra cứu biển số xe trong bảng 'vehicles' của cư dân.
        Integer residentId = vehicleDAO.findResidentByLicensePlate(log.getLicensePlate());

        // 2. Gán ID Cư dân (nếu tìm thấy)
        if (residentId != null) {
            log.setResidentId(residentId); // Đánh dấu đây là xe của cư dân
            log.setNotes(log.getNotes() + " (Xe Cư dân)"); // (Tùy chọn)
        } else {
            log.setResidentId(0); // Xe khách
        }
        // --- Kết thúc AC3 ---

        return accessControlDAO.logVehicleAccess(log);
    }

    // ==========================================================
    // AC2: TRA CỨU LỊCH SỬ (Giữ nguyên)
    // ==========================================================

    public List<VehicleAccessLog> searchVehicleLogs(LocalDate startDate, LocalDate endDate) throws SQLException {
        Date utilStartDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date utilEndDate = Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        java.sql.Date sqlStartDate = new java.sql.Date(utilStartDate.getTime());
        java.sql.Date sqlEndDate = new java.sql.Date(utilEndDate.getTime());

        return accessControlDAO.searchVehicleLogs(sqlStartDate, sqlEndDate);
    }

    public List<VisitorLog> searchVisitorLogs(LocalDate startDate, LocalDate endDate) throws SQLException {
        Date utilStartDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date utilEndDate = Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        java.sql.Date sqlStartDate = new java.sql.Date(utilStartDate.getTime());
        java.sql.Date sqlEndDate = new java.sql.Date(utilEndDate.getTime());

        return accessControlDAO.searchVisitorLogs(sqlStartDate, sqlEndDate);
    }
}