// Vị trí: src/main/java/com/example/quanlytoanha/dao/ApartmentDAO.java
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

    public List<Apartment> getAllApartments() {
        List<Apartment> apartments = new ArrayList<>();
        // JOIN với users để lấy tên chủ hộ
        String sql = "SELECT a.apartment_id, a.area, a.owner_id, u.full_name AS owner_name " +
                     "FROM apartments a " +
                     "LEFT JOIN users u ON a.owner_id = u.user_id " +
                     "ORDER BY a.apartment_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Apartment apartment = new Apartment();
                apartment.setApartmentId(rs.getInt("apartment_id"));
                apartment.setArea(rs.getBigDecimal("area")); // Đọc diện tích
                
                // Xử lý owner_id có thể NULL
                int ownerId = rs.getInt("owner_id");
                if (rs.wasNull()) {
                    apartment.setOwnerId(0); // 0 để đại diện cho NULL
                } else {
                    apartment.setOwnerId(ownerId);
                }
                
                // Xử lý owner_name có thể NULL (căn hộ trống)
                String ownerName = rs.getString("owner_name");
                apartment.setOwnerName(ownerName != null ? ownerName : "");
                
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
                    // Xử lý owner_id có thể NULL
                    int ownerId = rs.getInt("owner_id");
                    if (rs.wasNull()) {
                        apartment.setOwnerId(0); // 0 để đại diện cho NULL
                    } else {
                        apartment.setOwnerId(ownerId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apartment; // Sẽ là null nếu không tìm thấy
    }

    /**
     * Thêm căn hộ mới vào database
     * @param apartment Đối tượng Apartment chứa thông tin căn hộ
     * @return true nếu thêm thành công, false nếu có lỗi
     */
    public boolean addApartment(Apartment apartment) throws SQLException {
        String sql;
        
        // Nếu có apartment_id thì chỉ định, không thì để tự động
        if (apartment.getApartmentId() > 0) {
            sql = "INSERT INTO apartments (apartment_id, area, owner_id) VALUES (?, ?, ?)";
        } else {
            sql = "INSERT INTO apartments (area, owner_id) VALUES (?, ?)";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (apartment.getApartmentId() > 0) {
                // Có chỉ định apartment_id
                stmt.setInt(1, apartment.getApartmentId());
                
                // Set area
                if (apartment.getArea() != null) {
                    stmt.setBigDecimal(2, apartment.getArea());
                } else {
                    stmt.setNull(2, java.sql.Types.DECIMAL);
                }
                
                // Set owner_id (có thể NULL)
                if (apartment.getOwnerId() > 0) {
                    stmt.setInt(3, apartment.getOwnerId());
                } else {
                    stmt.setNull(3, java.sql.Types.INTEGER);
                }
            } else {
                // Tự động tạo apartment_id
                // Set area
                if (apartment.getArea() != null) {
                    stmt.setBigDecimal(1, apartment.getArea());
                } else {
                    stmt.setNull(1, java.sql.Types.DECIMAL);
                }
                
                // Set owner_id (có thể NULL)
                if (apartment.getOwnerId() > 0) {
                    stmt.setInt(2, apartment.getOwnerId());
                } else {
                    stmt.setNull(2, java.sql.Types.INTEGER);
                }
            }
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Kiểm tra căn hộ có tồn tại không
     * @param apartmentId ID căn hộ cần kiểm tra
     * @return true nếu tồn tại, false nếu không
     */
    public boolean apartmentExists(int apartmentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM apartments WHERE apartment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, apartmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Lấy ID tiếp theo gợi ý cho căn hộ mới (MAX + 1)
     * @return ID gợi ý tiếp theo
     */
    public int getNextSuggestedApartmentId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(apartment_id), 0) + 1 FROM apartments";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 1; // Mặc định là 1 nếu không có căn hộ nào
    }

    /**
     * Cập nhật thông tin căn hộ (diện tích và chủ hộ)
     * @param apartment Đối tượng Apartment chứa thông tin mới
     * @return true nếu cập nhật thành công
     */
    public boolean updateApartment(Apartment apartment) throws SQLException {
        String sql = "UPDATE apartments SET area = ?, owner_id = ? WHERE apartment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set area
            if (apartment.getArea() != null) {
                stmt.setBigDecimal(1, apartment.getArea());
            } else {
                stmt.setNull(1, java.sql.Types.DECIMAL);
            }
            
            // Set owner_id (có thể NULL)
            if (apartment.getOwnerId() > 0) {
                stmt.setInt(2, apartment.getOwnerId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            
            stmt.setInt(3, apartment.getApartmentId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Xóa căn hộ
     * @param apartmentId ID căn hộ cần xóa
     * @return true nếu xóa thành công
     */
    public boolean deleteApartment(int apartmentId) throws SQLException {
        String sql = "DELETE FROM apartments WHERE apartment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, apartmentId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Bỏ gán chủ hộ cho tất cả căn hộ có owner_id = userId.
     * Dùng trước khi xóa tài khoản cư dân để tránh lỗi ràng buộc FK.
     * @param userId id người dùng (cư dân) cần gỡ khỏi các căn hộ
     * @return số bản ghi được cập nhật
     */
    public int clearOwnerByUserId(int userId) throws SQLException {
        String sql = "UPDATE apartments SET owner_id = NULL WHERE owner_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate();
        }
    }

    /**
     * Kiểm tra căn hộ có cư dân đang ở không (có owner_id hoặc có residents)
     * @param apartmentId ID căn hộ cần kiểm tra
     * @return true nếu căn hộ có người ở, false nếu không
     */
    public boolean hasResidents(int apartmentId) throws SQLException {
        // Kiểm tra có owner_id không
        Apartment apartment = getApartmentById(apartmentId);
        if (apartment != null && apartment.getOwnerId() > 0) {
            return true;
        }
        
        // Kiểm tra có residents trong căn hộ không
        String sql = "SELECT COUNT(*) FROM residents WHERE apartment_id = ? AND (status = 'RESIDING' OR status IS NULL)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, apartmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Chuyển chủ hộ: cập nhật quan hệ của chủ hộ cũ và thành viên mới
     * @param apartmentId ID căn hộ
     * @param oldOwnerId ID chủ hộ cũ
     * @param newOwnerId ID chủ hộ mới
     */
    public void transferApartmentOwner(int apartmentId, int oldOwnerId, int newOwnerId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Cập nhật quan hệ của chủ hộ cũ từ "Chủ hộ" thành "Thành viên"
            String updateOldOwnerSql = "UPDATE residents SET relationship = 'Thành viên' " +
                                      "WHERE apartment_id = ? AND user_id = ? AND relationship = 'Chủ hộ'";
            try (PreparedStatement stmt = conn.prepareStatement(updateOldOwnerSql)) {
                stmt.setInt(1, apartmentId);
                stmt.setInt(2, oldOwnerId);
                stmt.executeUpdate();
            }

            // 2. Cập nhật quan hệ của thành viên mới thành "Chủ hộ"
            String updateNewOwnerSql = "UPDATE residents SET relationship = 'Chủ hộ' " +
                                       "WHERE apartment_id = ? AND user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateNewOwnerSql)) {
                stmt.setInt(1, apartmentId);
                stmt.setInt(2, newOwnerId);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}