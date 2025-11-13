// Vị trí: src/main/java/com/example/quanlytoanha/dao/VehicleDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Vehicle;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    /**
     * HÀM TRA CỨU (AC3): Tìm Resident ID dựa trên Biển số xe.
     * @param licensePlate Biển số xe cần tra cứu.
     * @return Integer (ID Cư dân) nếu tìm thấy, ngược lại trả về null.
     */
    public Integer findResidentByLicensePlate(String licensePlate) throws SQLException {
        // Chuẩn hóa biển số (xóa dấu cách, gạch ngang, viết hoa)
        String standardizedPlate = licensePlate.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

        // SQL cũng chuẩn hóa biển số trong CSDL khi so sánh
        String sql = "SELECT resident_id FROM vehicles WHERE UPPER(REGEXP_REPLACE(license_plate, '[^a-zA-Z0-9]', '')) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, standardizedPlate);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("resident_id"); // Tìm thấy
                } else {
                    return null; // Không tìm thấy (Xe khách)
                }
            }
        }
    }

    /**
     * Lấy tất cả xe đã đăng ký của một Cư dân.
     */
    public List<Vehicle> getVehiclesByResident(int residentId) throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicles WHERE resident_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, residentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    vehicles.add(mapResultSetToVehicle(rs));
                }
            }
        }
        return vehicles;
    }

    /**
     * Thêm xe mới cho Cư dân (BQT dùng).
     */
    public boolean addVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles (resident_id, license_plate, vehicle_type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vehicle.getResidentId());
            pstmt.setString(2, vehicle.getLicensePlate());
            pstmt.setString(3, vehicle.getVehicleType());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Xóa xe của Cư dân.
     */
    public boolean deleteVehicle(int vehicleId) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vehicleId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // --- Hàm Ánh xạ ---
    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setVehicleId(rs.getInt("vehicle_id"));
        v.setResidentId(rs.getInt("resident_id"));
        v.setLicensePlate(rs.getString("license_plate"));
        v.setVehicleType(rs.getString("vehicle_type"));
        return v;
    }
}