package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Apartment;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApartmentDAO {
    // ... (các hàm khác)

    // Lấy thông tin cơ bản của tất cả căn hộ để tính phí
    public List<Apartment> getAllApartmentsForBilling() throws SQLException {
        List<Apartment> apartments = new ArrayList<>();
        String sql = "SELECT apartment_id, area, owner_id FROM apartments";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Apartment apt = new Apartment(); // Giả sử có model Apartment
                apt.setApartmentId(rs.getInt("apartment_id"));
                apt.setArea(rs.getDouble("area"));
                apt.setOwnerId(rs.getInt("owner_id"));
                apartments.add(apt);
            }
        }
        return apartments;
    }
}