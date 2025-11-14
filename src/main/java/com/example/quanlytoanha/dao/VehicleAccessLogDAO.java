// Vị trí: src/main/java/com/example/quanlytoanha/dao/VehicleAccessLogDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.VehicleAccessLog;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VehicleAccessLogDAO {

    public List<VehicleAccessLog> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<VehicleAccessLog> logs = new ArrayList<>();
        // Câu SQL JOIN phức tạp để lấy tên Bảo vệ và tên Cư dân
        String sql = """
            SELECT 
                log.log_id, log.license_plate, log.vehicle_type, log.resident_id, 
                log.access_type, log.access_time, log.guard_user_id, log.notes,
                guard.full_name AS guard_full_name,
                res.full_name AS resident_full_name
            FROM vehicle_access_logs log
            JOIN users guard ON log.guard_user_id = guard.user_id
            LEFT JOIN residents res ON log.resident_id = res.resident_id
            WHERE log.access_time >= ? AND log.access_time < ?
            ORDER BY log.access_time DESC;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay())); // Đến hết ngày cuối cùng

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
        }
        return logs;
    }

    private VehicleAccessLog mapResultSetToLog(ResultSet rs) throws SQLException {
        VehicleAccessLog log = new VehicleAccessLog();
        log.setLogId(rs.getInt("log_id"));
        log.setLicensePlate(rs.getString("license_plate"));
        log.setVehicleType(rs.getString("vehicle_type"));
        log.setResidentId(rs.getInt("resident_id")); // Sẽ là 0 nếu rs.getInt trả về 0 cho NULL
        log.setAccessType(rs.getString("access_type"));
        log.setAccessTime(rs.getTimestamp("access_time"));
        log.setGuardUserId(rs.getInt("guard_user_id"));
        log.setNotes(rs.getString("notes"));
        log.setGuardFullName(rs.getString("guard_full_name"));
        log.setResidentFullName(rs.getString("resident_full_name")); // Có thể NULL
        return log;
    }
}