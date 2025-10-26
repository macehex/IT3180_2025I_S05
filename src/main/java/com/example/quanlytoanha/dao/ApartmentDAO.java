package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Apartment;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ApartmentDAO {

    public List<Apartment> getAllApartments() {
        List<Apartment> apartments = new ArrayList<>();
        String sql = "SELECT * FROM apartments"; // (Giả sử tên bảng là 'apartments')
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Apartment apartment = new Apartment();
                apartment.setApartmentId(rs.getInt("apartment_id"));
                apartment.setArea(rs.getBigDecimal("area")); // Đọc diện tích
                apartment.setOwnerId(rs.getInt("owner_id"));
                apartments.add(apartment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apartments;
    }

    public Apartment getApartmentById(int apartmentId) {
        Apartment apartment = null;
        String sql = "SELECT * FROM apartments WHERE apartment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, apartmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    apartment = new Apartment();
                    apartment.setApartmentId(rs.getInt("apartment_id"));
                    apartment.setArea(rs.getBigDecimal("area")); // Đọc diện tích
                    apartment.setOwnerId(rs.getInt("owner_id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apartment; // Sẽ là null nếu không tìm thấy
    }
}
