// Vị trí: src/main/java/com/example/quanlytoanha/dao/FeeTypeDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.FeeType;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FeeTypeDAO {

    /**
     * Lấy tất cả các loại phí đang được áp dụng
     */
    public List<FeeType> getAllActiveFees() throws SQLException {
        List<FeeType> feeTypes = new ArrayList<>();
        String sql = "SELECT * FROM fee_types WHERE is_active = TRUE ORDER BY fee_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                feeTypes.add(mapResultSetToFeeType(rs));
            }
        }
        return feeTypes;
    }

    /**
     * Thêm một loại phí mới
     */
    public boolean addFee(FeeType fee) throws SQLException {
        String sql = "INSERT INTO fee_types (fee_name, unit_price, unit, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fee.getFeeName());
            stmt.setBigDecimal(2, fee.getUnitPrice());
            stmt.setString(3, fee.getUnit());
            stmt.setString(4, fee.getDescription());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật một loại phí
     */
    public boolean updateFee(FeeType fee) throws SQLException {
        String sql = "UPDATE fee_types SET fee_name = ?, unit_price = ?, unit = ?, description = ? WHERE fee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fee.getFeeName());
            stmt.setBigDecimal(2, fee.getUnitPrice());
            stmt.setString(3, fee.getUnit());
            stmt.setString(4, fee.getDescription());
            stmt.setInt(5, fee.getFeeId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Hủy (Vô hiệu hóa) một loại phí
     */
    public boolean deactivateFee(int feeId) throws SQLException {
        String sql = "UPDATE fee_types SET is_active = FALSE WHERE fee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, feeId);
            return stmt.executeUpdate() > 0;
        }
    }

    private FeeType mapResultSetToFeeType(ResultSet rs) throws SQLException {
        FeeType fee = new FeeType();
        fee.setFeeId(rs.getInt("fee_id"));
        fee.setFeeName(rs.getString("fee_name"));
        fee.setUnitPrice(rs.getBigDecimal("unit_price"));
        fee.setUnit(rs.getString("unit"));
        fee.setDescription(rs.getString("description"));
        fee.setActive(rs.getBoolean("is_active"));
        return fee;
    }
}