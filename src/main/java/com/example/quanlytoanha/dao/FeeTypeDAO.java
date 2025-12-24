// Vị trí: src/main/java/com/example/quanlytoanha/dao/FeeTypeDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.FeeType;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FeeTypeDAO {

    /**
     * NGHIỆP VỤ 1: Load dữ liệu (Đã cập nhật)
     */
    public List<FeeType> getAllActiveFeeTypes() {
        List<FeeType> feeTypes = new ArrayList<>();
        // Lấy tất cả các cột, bao gồm cả cột mới
        String sql = "SELECT * FROM fee_types WHERE is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Sử dụng constructor mới (đã sửa lỗi 'fee_id')
                FeeType fee = new FeeType(
                        rs.getInt("fee_id"), // Đã sửa từ 'id'
                        rs.getString("fee_name"),
                        rs.getBigDecimal("unit_price"),
                        rs.getString("unit"),
                        rs.getString("description"),
                        rs.getBoolean("is_active"),
                        rs.getBoolean("is_default"), // <-- Đọc cột mới
                        rs.getString("pricing_model") // <-- Đọc cột mới
                );
                feeTypes.add(fee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feeTypes;
    }

    /**
     * NGHIỆP VỤ 2: Thêm phí mới (Đã cập nhật)
     */
    public boolean addFee(FeeType fee) {
        // Cập nhật câu SQL để thêm 2 cột mới
        String sql = "INSERT INTO fee_types (fee_name, unit_price, unit, description, is_active, is_default, pricing_model) VALUES (?, ?, ?, ?, TRUE, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fee.getFeeName());
            stmt.setBigDecimal(2, fee.getUnitPrice());
            stmt.setString(3, fee.getUnit());
            stmt.setString(4, fee.getDescription());
            stmt.setBoolean(5, fee.isDefault()); // <-- Thêm tham số mới
            stmt.setString(6, fee.getPricingModel()); // <-- Thêm tham số mới

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NGHIỆP VỤ 3: Chỉnh sửa phí (Đã cập nhật)
     */
    public boolean updateFee(FeeType fee) {
        // Cập nhật câu SQL để sửa 2 cột mới
        String sql = "UPDATE fee_types SET fee_name = ?, unit_price = ?, unit = ?, description = ?, is_default = ?, pricing_model = ? WHERE fee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fee.getFeeName());
            stmt.setBigDecimal(2, fee.getUnitPrice());
            stmt.setString(3, fee.getUnit());
            stmt.setString(4, fee.getDescription());
            stmt.setBoolean(5, fee.isDefault()); // <-- Thêm tham số mới
            stmt.setString(6, fee.getPricingModel()); // <-- Thêm tham số mới
            stmt.setInt(7, fee.getFeeId()); // Đã sửa thành getFeeId()

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NGHIỆP VỤ 4: Xóa/Hủy phí (Đã sửa lỗi 'fee_id')
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

    /**
     * HÀM MỚI: Lấy tất cả các phí mặc định (is_default = TRUE)
     */
    public List<FeeType> getAllDefaultFees() {
        List<FeeType> feeTypes = new ArrayList<>();
        // Lấy tất cả các phí đang hoạt động VÀ là phí mặc định
        String sql = "SELECT * FROM fee_types WHERE is_active = TRUE AND is_default = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Sử dụng constructor 8 tham số mà chúng ta đã sửa
                FeeType fee = new FeeType(
                        rs.getInt("fee_id"),
                        rs.getString("fee_name"),
                        rs.getBigDecimal("unit_price"),
                        rs.getString("unit"),
                        rs.getString("description"),
                        rs.getBoolean("is_active"),
                        rs.getBoolean("is_default"),
                        rs.getString("pricing_model")
                );
                feeTypes.add(fee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feeTypes;
    }

    /**
     * HÀM MỚI: Lấy tất cả các phí tùy chọn mà một căn hộ đã đăng ký
     */
    public List<FeeType> getOptionalFeesForApartment(int apartmentId) {
        List<FeeType> feeTypes = new ArrayList<>();
        // JOIN 2 bảng fee_types và service_registrations
        String sql = """
            SELECT ft.* FROM fee_types ft
            JOIN service_registrations sr ON ft.fee_id = sr.fee_id
            WHERE sr.apartment_id = ? 
              AND sr.status = TRUE 
              AND ft.is_active = TRUE
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, apartmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FeeType fee = new FeeType(
                            rs.getInt("fee_id"),
                            rs.getString("fee_name"),
                            rs.getBigDecimal("unit_price"),
                            rs.getString("unit"),
                            rs.getString("description"),
                            rs.getBoolean("is_active"),
                            rs.getBoolean("is_default"),
                            rs.getString("pricing_model")
                    );
                    feeTypes.add(fee);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feeTypes;
    }

    /**
     * HÀM MỚI: Tìm id của phí đóng góp đang hoạt động
     */
    public Integer getActiveVoluntaryFeeId() {
        String sql = "SELECT fee_id FROM fee_types WHERE pricing_model = 'VOLUNTARY' AND is_active = TRUE LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("fee_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * HÀM MỚI: Lấy loại phí vừa được tạo gần đây nhất (có fee_id lớn nhất)
     * Dùng để lấy ID ngay sau khi thêm mới.
     */
    public FeeType getLatestFee() {
        // Sắp xếp ID giảm dần và lấy cái đầu tiên -> Chính là cái mới nhất
        String sql = "SELECT * FROM fee_types ORDER BY fee_id DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return new FeeType(
                        rs.getInt("fee_id"),
                        rs.getString("fee_name"),
                        rs.getBigDecimal("unit_price"),
                        rs.getString("unit"),
                        rs.getString("description"),
                        rs.getBoolean("is_active"),
                        rs.getBoolean("is_default"),
                        rs.getString("pricing_model")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * HÀM MỚI: Tìm thông tin loại phí dựa theo tên
     * Dùng để lấy ID sau khi vừa tạo phí mới.
     */
    public FeeType getFeeByName(String feeName) {
        String sql = "SELECT * FROM fee_types WHERE fee_name = ? AND is_active = TRUE LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, feeName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new FeeType(
                            rs.getInt("fee_id"),
                            rs.getString("fee_name"),
                            rs.getBigDecimal("unit_price"),
                            rs.getString("unit"),
                            rs.getString("description"),
                            rs.getBoolean("is_active"),
                            rs.getBoolean("is_default"),
                            rs.getString("pricing_model")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}