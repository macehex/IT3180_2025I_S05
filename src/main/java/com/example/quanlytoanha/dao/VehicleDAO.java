package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VehicleDAO {
    // Đếm số lượng xe của một căn hộ
    public int countVehiclesByApartment(int apartmentId) throws SQLException {
        String sql = "SELECT COUNT(v.vehicle_id) FROM vehicles v " +
                "JOIN residents r ON v.resident_id = r.resident_id " +
                "WHERE r.apartment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, apartmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
