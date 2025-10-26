package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.DashboardDAO;
import com.example.quanlytoanha.model.Role; // Enum Role của bạn
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DashboardService {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    /**
     * Lấy tất cả các số liệu thống kê cho Dashboard Admin.
     * @return Map chứa các số liệu (key: tên số liệu, value: giá trị)
     * @throws SecurityException Nếu user không có quyền xem.
     * @throws SQLException Nếu có lỗi database.
     */
    public Map<String, Object> getAdminDashboardStats() throws SecurityException, SQLException {
        // Kiểm tra quyền (chỉ Admin mới được xem)
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            throw new SecurityException("Chỉ Ban Quản Trị mới có quyền xem thống kê này.");
        }

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalResidents", dashboardDAO.getTotalActiveResidents());
        stats.put("totalApartments", dashboardDAO.getTotalApartments());
        stats.put("totalDebt", dashboardDAO.getTotalUnpaidDebt());
        stats.put("totalUnpaidInvoices", dashboardDAO.getTotalUnpaidInvoices());

        return stats;
    }
}