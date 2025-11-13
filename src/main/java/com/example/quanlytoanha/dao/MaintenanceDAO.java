// Vị trí: src/main/java/com/example/quanlytoanha/dao/MaintenanceDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Maintenance;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// Sửa import: Không dùng java.sql.Date ở đây nữa
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDAO {

    /**
     * Lập lịch bảo trì mới (Status: PENDING)
     * (Đã sửa: Chấp nhận java.util.Date)
     */
    public boolean scheduleMaintenance(Maintenance maintenance) throws SQLException {
        String sql = "INSERT INTO maintenance_history (asset_id, status, scheduled_date, description, created_by_user_id) " +
                "VALUES (?, 'PENDING', ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, maintenance.getAssetId());
            // SỬA LỖI: Chuyển đổi util.Date sang sql.Date tại đây
            pstmt.setDate(2, new java.sql.Date(maintenance.getScheduledDate().getTime()));
            pstmt.setString(3, maintenance.getDescription());
            pstmt.setInt(4, maintenance.getCreatedByUserId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Hoàn thành/Ghi nhận một lịch bảo trì
     * (Đã sửa: Chấp nhận java.util.Date)
     */
    public boolean completeMaintenance(int maintenanceId, java.util.Date maintenanceDate, BigDecimal cost, String performedBy, String description) throws SQLException {
        String sql = "UPDATE maintenance_history SET status = 'COMPLETED', maintenance_date = ?, cost = ?, performed_by = ?, description = ? " +
                "WHERE maintenance_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // SỬA LỖI: Chuyển đổi util.Date sang sql.Date tại đây
            pstmt.setDate(1, new java.sql.Date(maintenanceDate.getTime()));
            pstmt.setBigDecimal(2, cost);
            pstmt.setString(3, performedBy);
            pstmt.setString(4, description);
            pstmt.setInt(5, maintenanceId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Lấy tất cả lịch sử/lịch hẹn bảo trì (JOIN với assets để lấy tên)
     */
    public List<Maintenance> getAllMaintenanceHistory() throws SQLException {
        List<Maintenance> historyList = new ArrayList<>();
        // JOIN với bảng assets để lấy asset_type (tên tài sản)
        String sql = "SELECT mh.*, a.asset_type " +
                "FROM maintenance_history mh " +
                "JOIN assets a ON mh.asset_id = a.asset_id " +
                "ORDER BY mh.scheduled_date DESC, mh.maintenance_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                historyList.add(mapResultSetToMaintenance(rs));
            }
        }
        return historyList;
    }

    // Hàm tiện ích
    private Maintenance mapResultSetToMaintenance(ResultSet rs) throws SQLException {
        Maintenance m = new Maintenance();
        m.setMaintenanceId(rs.getInt("maintenance_id"));
        m.setAssetId(rs.getInt("asset_id"));
        m.setStatus(rs.getString("status"));
        m.setScheduledDate(rs.getDate("scheduled_date")); // Đọc về java.sql.Date (Model chấp nhận)
        m.setMaintenanceDate(rs.getDate("maintenance_date")); // Đọc về java.sql.Date (Model chấp nhận)
        m.setDescription(rs.getString("description"));
        m.setCost(rs.getBigDecimal("cost"));
        m.setPerformedBy(rs.getString("performed_by"));
        m.setCreatedByUserId(rs.getInt("created_by_user_id"));

        // Lấy tên từ JOIN
        if (rs.getString("asset_type") != null) {
            m.setAssetName(rs.getString("asset_type"));
        }

        return m;
    }
}