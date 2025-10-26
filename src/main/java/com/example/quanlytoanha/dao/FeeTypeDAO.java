package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.FeeType;
import com.example.quanlytoanha.utils.DatabaseConnection; // Lớp kết nối của bạn

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FeeTypeDAO {

    /**
     * NGHIỆP VỤ 1: Load dữ liệu
     */
    public List<FeeType> getAllActiveFeeTypes() {
        List<FeeType> feeTypes = new ArrayList<>();
        String sql = "SELECT * FROM fee_types WHERE is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                FeeType fee = new FeeType(
                        rs.getInt("fee_id"),
                        rs.getString("fee_name"),
                        rs.getBigDecimal("unit_price"),
                        rs.getString("unit"),
                        rs.getString("description"),
                        rs.getBoolean("is_active")
                );
                feeTypes.add(fee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feeTypes;
    }

    /**
     * NGHIỆP VỤ 2: Thêm phí mới
     */
    public boolean addFee(FeeType fee) {
        String sql = "INSERT INTO fee_types (fee_name, unit_price, unit, description, is_active) VALUES (?, ?, ?, ?, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fee.getFeeName());
            stmt.setBigDecimal(2, fee.getUnitPrice());
            stmt.setString(3, fee.getUnit());
            stmt.setString(4, fee.getDescription());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NGHIỆP VỤ 3: Chỉnh sửa phí
     */
    public boolean updateFee(FeeType fee) {
        String sql = "UPDATE fee_types SET fee_name = ?, unit_price = ?, unit = ?, description = ? WHERE fee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fee.getFeeName());
            stmt.setBigDecimal(2, fee.getUnitPrice());
            stmt.setString(3, fee.getUnit());
            stmt.setString(4, fee.getDescription());
            stmt.setInt(5, fee.getId());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NGHIỆP VỤ 4: Xóa/Hủy phí
     */
    public boolean deactivateFee(int feeId) {
        String sql = "UPDATE fee_types SET is_active = FALSE WHERE fee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, feeId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}