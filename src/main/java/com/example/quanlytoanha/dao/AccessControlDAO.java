// Vị trí: src/main/java/com/example/quanlytoanha/dao/AccessControlDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.VehicleAccessLog;
import com.example.quanlytoanha.model.VisitorLog;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date; // (Đảm bảo import java.util.Date)
import java.util.List;

public class AccessControlDAO {

    // ==========================================================
    // AC1: GHI NHẬN RA/VÀO
    // ==========================================================

    /**
     * Ghi nhận Khách đi bộ VÀO
     * (SỬA LỖI: Xử lý apartment_id có thể NULL)
     */
    public boolean checkInVisitor(VisitorLog log) throws SQLException {
        String sql = "INSERT INTO visitor_logs (visitor_name, id_card_number, contact_phone, reason, apartment_id, guard_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, log.getVisitorName());
            pstmt.setString(2, log.getIdCardNumber());
            pstmt.setString(3, log.getContactPhone());
            pstmt.setString(4, log.getReason());

            // --- SỬA LỖI: Xử lý Integer (có thể null) ---
            if (log.getApartmentId() != null && log.getApartmentId() > 0) {
                pstmt.setInt(5, log.getApartmentId());
            } else {
                pstmt.setNull(5, Types.INTEGER); // Gửi NULL nếu không chọn
            }
            // --- KẾT THÚC SỬA LỖI ---

            pstmt.setInt(6, log.getGuardUserId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Ghi nhận Khách đi bộ RA
     */
    public boolean checkOutVisitor(int logId) throws SQLException {
        String sql = "UPDATE visitor_logs SET check_out_time = CURRENT_TIMESTAMP WHERE log_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, logId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Ghi nhận Xe VÀO/RA
     */
    public boolean logVehicleAccess(VehicleAccessLog log) throws SQLException {
        String sql = "INSERT INTO vehicle_access_logs (license_plate, vehicle_type, resident_id, access_type, guard_user_id, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, log.getLicensePlate());
            pstmt.setString(2, log.getVehicleType());

            if (log.getResidentId() > 0) {
                pstmt.setInt(3, log.getResidentId());
            } else {
                pstmt.setNull(3, Types.INTEGER); // Xe khách
            }

            pstmt.setString(4, log.getAccessType()); // 'IN' hoặc 'OUT'
            pstmt.setInt(5, log.getGuardUserId());
            pstmt.setString(6, log.getNotes());

            return pstmt.executeUpdate() > 0;
        }
    }

    // ==========================================================
    // AC2: TRA CỨU LỊCH SỬ
    // (Đã sửa lỗi kiểu Date trong Service, DAO giữ nguyên java.sql.Date)
    // ==========================================================

    /**
     * Tra cứu lịch sử XE ra/vào theo khoảng thời gian
     */
    public List<VehicleAccessLog> searchVehicleLogs(java.sql.Date startDate, java.sql.Date endDate) throws SQLException {
        List<VehicleAccessLog> logs = new ArrayList<>();
        String sql = "SELECT v.*, u.full_name as guard_name, r.full_name as resident_name " +
                "FROM vehicle_access_logs v " +
                "JOIN users u ON v.guard_user_id = u.user_id " +
                "LEFT JOIN residents r ON v.resident_id = r.resident_id " +
                "WHERE v.access_time::date BETWEEN ? AND ? " + // Lọc theo Ngày
                "ORDER BY v.access_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToVehicleLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Tra cứu lịch sử KHÁCH ra/vào theo khoảng thời gian
     */
    public List<VisitorLog> searchVisitorLogs(java.sql.Date startDate, java.sql.Date endDate) throws SQLException {
        List<VisitorLog> logs = new ArrayList<>();
        String sql = "SELECT v.*, u.full_name as guard_name " +
                "FROM visitor_logs v " +
                "JOIN users u ON v.guard_user_id = u.user_id " +
                "WHERE v.check_in_time::date BETWEEN ? AND ? " + // Lọc theo Ngày
                "ORDER BY v.check_in_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToVisitorLog(rs));
                }
            }
        }
        return logs;
    }

    // --- HÀM ÁNH XẠ ---

    private VehicleAccessLog mapResultSetToVehicleLog(ResultSet rs) throws SQLException {
        VehicleAccessLog log = new VehicleAccessLog();
        log.setLogId(rs.getInt("log_id"));
        log.setLicensePlate(rs.getString("license_plate"));
        log.setVehicleType(rs.getString("vehicle_type"));
        log.setResidentId(rs.getInt("resident_id"));
        log.setAccessType(rs.getString("access_type"));
        log.setAccessTime(rs.getTimestamp("access_time"));
        log.setGuardUserId(rs.getInt("guard_user_id"));
        log.setNotes(rs.getString("notes"));

        log.setGuardFullName(rs.getString("guard_name"));
        log.setResidentFullName(rs.getString("resident_name"));

        return log;
    }

    private VisitorLog mapResultSetToVisitorLog(ResultSet rs) throws SQLException {
        VisitorLog log = new VisitorLog();
        log.setLogId(rs.getInt("log_id"));
        log.setVisitorName(rs.getString("visitor_name"));
        log.setIdCardNumber(rs.getString("id_card_number"));
        log.setContactPhone(rs.getString("contact_phone"));
        log.setReason(rs.getString("reason"));

        // --- SỬA LỖI: Đọc Integer (có thể null) ---
        int apartmentId = rs.getInt("apartment_id");
        if (!rs.wasNull()) {
            log.setApartmentId(apartmentId);
        } else {
            log.setApartmentId(null);
        }
        // --- KẾT THÚC SỬA LỖI ---

        log.setCheckInTime(rs.getTimestamp("check_in_time"));
        log.setCheckOutTime(rs.getTimestamp("check_out_time"));
        log.setGuardUserId(rs.getInt("guard_user_id"));

        log.setGuardFullName(rs.getString("guard_name"));

        return log;
    }
}