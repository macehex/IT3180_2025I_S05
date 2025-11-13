// Vị trí: src/main/java/com/example/quanlytoanha/dao/ResidentHistoryDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.ResidentHistory;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResidentHistoryDAO {

    /**
     * HÀM GHI: Thêm một bản ghi lịch sử vào CSDL.
     * Hàm này được gọi BÊN TRONG Transaction khi cập nhật Resident.
     * @param conn Connection đang mở (từ Transaction)
     * @param residentId ID cư dân bị thay đổi
     * @param changedByUserId ID của BQT
     * @param oldDataJson Dữ liệu cũ (dạng String JSON)
     * @param newDataJson Dữ liệu mới (dạng String JSON)
     */
    public void addHistory(Connection conn, int residentId, int changedByUserId, String oldDataJson, String newDataJson) throws SQLException {
        String sql = "INSERT INTO resident_history (resident_id, changed_by_user_id, old_data, new_data) " +
                "VALUES (?, ?, ?::jsonb, ?::jsonb)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, residentId);
            pstmt.setInt(2, changedByUserId);
            pstmt.setString(3, oldDataJson);
            pstmt.setString(4, newDataJson);
            pstmt.executeUpdate();
        }
    }

    /**
     * HÀM ĐỌC: Lấy lịch sử thay đổi cho MỘT cư dân.
     * (Để hiển thị trên Form US1_1_1.4)
     */
    public List<ResidentHistory> getHistoryForResident(int residentId) throws SQLException {
        List<ResidentHistory> historyList = new ArrayList<>();
        // JOIN với bảng users (để lấy tên BQT) và residents (lấy tên Cư dân)
        String sql = "SELECT rh.*, u.full_name as admin_name, r.full_name as resident_name " +
                "FROM resident_history rh " +
                "JOIN users u ON rh.changed_by_user_id = u.user_id " +
                "JOIN residents r ON rh.resident_id = r.resident_id " +
                "WHERE rh.resident_id = ? " +
                "ORDER BY rh.changed_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, residentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ResidentHistory h = new ResidentHistory();
                    h.setHistoryId(rs.getInt("history_id"));
                    h.setResidentId(rs.getInt("resident_id"));
                    h.setChangedByUserId(rs.getInt("changed_by_user_id"));
                    h.setChangedAt(rs.getTimestamp("changed_at"));
                    h.setOldData(rs.getString("old_data"));
                    h.setNewData(rs.getString("new_data"));

                    // Lấy tên từ JOIN
                    h.setChangedByUserFullName(rs.getString("admin_name"));
                    h.setResidentFullName(rs.getString("resident_name"));

                    historyList.add(h);
                }
            }
        }
        return historyList;
    }
}