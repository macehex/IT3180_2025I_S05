// Vị trí: src/main/java/com/example/quanlytoanha/dao/VisitorLogDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.VisitorLog;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VisitorLogDAO {

    public List<VisitorLog> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<VisitorLog> logs = new ArrayList<>();
        String sql = """
            SELECT 
                log.log_id, log.visitor_name, log.id_card_number, log.contact_phone, 
                log.reason, log.apartment_id, log.check_in_time, log.check_out_time, 
                log.guard_user_id, guard.full_name AS guard_full_name
            FROM visitor_logs log
            JOIN users guard ON log.guard_user_id = guard.user_id
            WHERE log.check_in_time >= ? AND log.check_in_time < ?
            ORDER BY log.check_in_time DESC;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
        }
        return logs;
    }

    private VisitorLog mapResultSetToLog(ResultSet rs) throws SQLException {
        VisitorLog log = new VisitorLog();
        log.setLogId(rs.getInt("log_id"));
        log.setVisitorName(rs.getString("visitor_name"));
        log.setIdCardNumber(rs.getString("id_card_number"));
        log.setContactPhone(rs.getString("contact_phone"));
        log.setReason(rs.getString("reason"));
        log.setApartmentId((Integer) rs.getObject("apartment_id")); // Lấy Integer (cho phép NULL)
        log.setCheckInTime(rs.getTimestamp("check_in_time"));
        log.setCheckOutTime(rs.getTimestamp("check_out_time"));
        log.setGuardUserId(rs.getInt("guard_user_id"));
        log.setGuardFullName(rs.getString("guard_full_name"));
        return log;
    }
}