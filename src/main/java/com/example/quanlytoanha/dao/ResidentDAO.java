
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.ResidentPOJO;
import com.example.quanlytoanha.utils.DatabaseConnection; // Lớp tiện ích hiện có

import java.sql.*;

public class ResidentDAO {

    /**
     * Kiểm tra Số căn cước (idCardNumber) đã tồn tại chưa trong DB.
     * @param idCardNumber Số CCCD cần kiểm tra.
     * @return true nếu số CCCD này duy nhất (chưa tồn tại), false nếu đã tồn tại.
     */
    public boolean isIdCardUnique(String idCardNumber) throws SQLException {
        if (idCardNumber == null || idCardNumber.trim().isEmpty()) return true;

        String SQL = "SELECT resident_id FROM residents WHERE id_card_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setString(1, idCardNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                return !rs.next(); // Trả về TRUE nếu KHÔNG tìm thấy
            }
        }
    }

    /**
     * Kiểm tra ID Căn hộ có tồn tại không.
     * @param apartmentId ID Căn hộ.
     * @return true nếu căn hộ tồn tại.
     */
    public boolean isApartmentExist(int apartmentId) throws SQLException {
        // Giả định có bảng 'apartments'
        String SQL = "SELECT apartment_id FROM apartments WHERE apartment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, apartmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Thêm cư dân mới vào bảng 'residents'.
     * @param resident Đối tượng ResidentPOJO chứa dữ liệu cần lưu.
     * @return true nếu thêm thành công.
     */
    public boolean addResident(ResidentPOJO resident) throws SQLException {
        String SQL = "INSERT INTO residents (apartment_id, user_id, full_name, date_of_birth, id_card_number, relationship, status, move_in_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            // 1. apartmentId
            pstmt.setInt(1, resident.getApartmentId());

            // 2. userId (Có thể NULL)
            if (resident.getUserId() != null && resident.getUserId() > 0) {
                pstmt.setInt(2, resident.getUserId());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }

            // 3. fullName
            pstmt.setString(3, resident.getFullName());

            // 4. dateOfBirth (Có thể NULL)
            if (resident.getDateOfBirth() != null) {
                pstmt.setDate(4, new java.sql.Date(resident.getDateOfBirth().getTime()));
            } else {
                pstmt.setNull(4, java.sql.Types.DATE);
            }

            // 5. idCardNumber
            pstmt.setString(5, resident.getIdCardNumber());

            // 6. relationship
            pstmt.setString(6, resident.getRelationship());

            // 7. status
            pstmt.setString(7, resident.getStatus()); // Lấy giá trị mặc định RESIDING

            // 8. moveInDate
            pstmt.setDate(8, new java.sql.Date(resident.getMoveInDate().getTime()));

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        }
    }
}
