package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Vehicle;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    /**
     * HÀM TRA CỨU (AC3): Tìm Resident ID dựa trên Biển số xe.
     * @param licensePlate Biển số xe cần tra cứu.
     * @return Integer (ID Cư dân) nếu tìm thấy, ngược lại trả về null.
     */
    public Integer findResidentByLicensePlate(String licensePlate) throws SQLException {
        // Kiểm tra null hoặc rỗng
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            return null;
        }
        
        // Chuẩn hóa biển số: trim() trước, sau đó xóa tất cả ký tự không phải chữ/số, viết hoa
        String standardizedPlate = licensePlate.trim().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        
        // Nếu sau khi chuẩn hóa thành chuỗi rỗng, trả về null
        if (standardizedPlate.isEmpty()) {
            return null;
        }

        // SQL cũng chuẩn hóa biển số trong CSDL khi so sánh
        // Sử dụng TRIM() và REGEXP_REPLACE để xử lý khoảng trắng và ký tự đặc biệt
        // PostgreSQL REGEXP_REPLACE mặc định replace tất cả các occurrence
        String sql = "SELECT resident_id FROM vehicles WHERE UPPER(REGEXP_REPLACE(TRIM(license_plate), '[^a-zA-Z0-9]', '')) = ?";

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

    /**
     * NGHIỆP VỤ MỚI: Đếm số lượng xe đang hoạt động của một căn hộ theo loại xe.
     * Phục vụ cho việc tính phí hàng tháng tự động.
     */
    public int countActiveVehiclesByType(int apartmentId, String vehicleType) {
        String sql = "SELECT COUNT(*) FROM vehicles WHERE apartment_id = ? AND vehicle_type = ? AND is_active = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, apartmentId);
            pstmt.setString(2, vehicleType.toUpperCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy danh sách xe của một căn hộ (để hiển thị trên thông tin hộ gia đình).
     */
    public List<Vehicle> getVehiclesByApartment(int apartmentId) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT v.*, u.full_name FROM vehicles v " +
                "LEFT JOIN users u ON v.resident_id = u.user_id " +
                "WHERE v.apartment_id = ? AND v.is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, apartmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Vehicle v = mapResultSetToVehicle(rs);
                    v.setResidentFullName(rs.getString("full_name"));
                    vehicles.add(v);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    public boolean addVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles (resident_id, apartment_id, license_plate, vehicle_type, registration_date, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vehicle.getResidentId());
            pstmt.setInt(2, vehicle.getApartmentId());
            pstmt.setString(3, vehicle.getLicensePlate());
            pstmt.setString(4, vehicle.getVehicleType().toUpperCase());
            pstmt.setDate(5, Date.valueOf(vehicle.getRegistrationDate()));
            pstmt.setBoolean(6, vehicle.isActive());

            return pstmt.executeUpdate() > 0;
        }
    }

    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setVehicleId(rs.getInt("vehicle_id"));
        v.setResidentId(rs.getInt("resident_id"));
        v.setApartmentId(rs.getInt("apartment_id"));
        v.setLicensePlate(rs.getString("license_plate"));
        v.setVehicleType(rs.getString("vehicle_type"));
        v.setRegistrationDate(rs.getDate("registration_date").toLocalDate());
        v.setActive(rs.getBoolean("is_active"));
        return v;
    }
}