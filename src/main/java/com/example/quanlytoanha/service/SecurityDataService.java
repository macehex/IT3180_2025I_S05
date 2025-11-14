// Vị trí: src/main/java/com/example/quanlytoanha/service/SecurityDataService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.ResidentDAO;
import com.example.quanlytoanha.dao.VehicleAccessLogDAO;
import com.example.quanlytoanha.dao.VisitorLogDAO;
import com.example.quanlytoanha.model.Role;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.model.VehicleAccessLog;
import com.example.quanlytoanha.model.VisitorLog;
import com.example.quanlytoanha.session.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityDataService {

    private final ResidentDAO residentDAO = new ResidentDAO();
    private final VehicleAccessLogDAO vehicleLogDAO = new VehicleAccessLogDAO();
    private final VisitorLogDAO visitorLogDAO = new VisitorLogDAO();

    /**
     * Kiểm tra quyền: Chỉ Công an hoặc Admin mới được xem.
     */
    private void checkPermission() throws SecurityException {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || (currentUser.getRole() != Role.POLICE && currentUser.getRole() != Role.ADMIN)) {
            throw new SecurityException("Không có quyền truy cập dữ liệu an ninh.");
        }
    }

    /**
     * Lấy thống kê Dân cư (Chuyển vào/Chuyển đi)
     * SỬ DỤNG HÀM CÓ SẴN CỦA BẠN
     */
    public Map<String, Integer> getPopulationStats(LocalDate startDate, LocalDate endDate) throws SQLException, SecurityException {
        checkPermission();

        // Chuyển đổi LocalDate sang java.sql.Date
        java.sql.Date sqlStartDate = java.sql.Date.valueOf(startDate);
        java.sql.Date sqlEndDate = java.sql.Date.valueOf(endDate);

        // Gọi hàm getPopulationChangeStats bạn đã cung cấp
        return residentDAO.getPopulationChangeStats(sqlStartDate, sqlEndDate);
    }

    /**
     * Lấy lịch sử Xe Ra/Vào
     */
    public List<VehicleAccessLog> getVehicleLogs(LocalDate startDate, LocalDate endDate) throws SQLException, SecurityException {
        checkPermission();
        return vehicleLogDAO.findByDateRange(startDate, endDate);
    }

    /**
     * Lấy lịch sử Khách Ra/Vào
     */
    public List<VisitorLog> getVisitorLogs(LocalDate startDate, LocalDate endDate) throws SQLException, SecurityException {
        checkPermission();
        return visitorLogDAO.findByDateRange(startDate, endDate);
    }
}