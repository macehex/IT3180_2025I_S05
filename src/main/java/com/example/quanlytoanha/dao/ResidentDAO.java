
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.ResidentPOJO;
import com.example.quanlytoanha.utils.DatabaseConnection; // Lớp tiện ích hiện có

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Lấy danh sách tất cả cư dân với thông tin đầy đủ từ database.
     * Bao gồm thông tin từ bảng residents và users.
     * @return Danh sách ResidentPOJO với thông tin đầy đủ
     */
    public List<ResidentPOJO> getAllResidents() throws SQLException {
        List<ResidentPOJO> residents = new ArrayList<>();
        
        String SQL = """
            SELECT r.resident_id, r.apartment_id, r.user_id, r.full_name, 
                   r.date_of_birth, r.id_card_number, r.relationship, r.status, r.move_in_date,
                   u.phone_number, u.email
            FROM residents r
            LEFT JOIN users u ON r.user_id = u.user_id
            ORDER BY r.resident_id
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                ResidentPOJO resident = new ResidentPOJO();
                resident.setResidentId(rs.getInt("resident_id"));
                resident.setApartmentId(rs.getInt("apartment_id"));
                
                int userId = rs.getInt("user_id");
                if (!rs.wasNull()) {
                    resident.setUserId(userId);
                }
                
                resident.setFullName(rs.getString("full_name"));
                
                Date dateOfBirth = rs.getDate("date_of_birth");
                if (dateOfBirth != null) {
                    resident.setDateOfBirth(dateOfBirth);
                }
                
                resident.setIdCardNumber(rs.getString("id_card_number"));
                resident.setRelationship(rs.getString("relationship"));
                resident.setStatus(rs.getString("status"));
                
                Date moveInDate = rs.getDate("move_in_date");
                if (moveInDate != null) {
                    resident.setMoveInDate(moveInDate);
                }
                
                // Thêm thông tin từ bảng users
                String phoneNumber = rs.getString("phone_number");
                String email = rs.getString("email");
                
                // Tạo một lớp mở rộng để chứa thông tin bổ sung
                ExtendedResidentPOJO extendedResident = new ExtendedResidentPOJO(resident);
                extendedResident.setPhoneNumber(phoneNumber);
                extendedResident.setEmail(email);
                
                residents.add(extendedResident);
            }
        }
        
        return residents;
    }

    /**
     * Tìm kiếm cư dân theo các tiêu chí.
     * @param name Tên cư dân (có thể null)
     * @param apartmentId ID căn hộ (có thể null)
     * @param status Trạng thái (có thể null)
     * @return Danh sách cư dân phù hợp
     */
    public List<ResidentPOJO> searchResidents(String name, Integer apartmentId, String status) throws SQLException {
        List<ResidentPOJO> residents = new ArrayList<>();
        
        StringBuilder SQL = new StringBuilder("""
            SELECT r.resident_id, r.apartment_id, r.user_id, r.full_name, 
                   r.date_of_birth, r.id_card_number, r.relationship, r.status, r.move_in_date,
                   u.phone_number, u.email
            FROM residents r
            LEFT JOIN users u ON r.user_id = u.user_id
            WHERE 1=1
            """);
        
        List<Object> parameters = new ArrayList<>();
        
        if (name != null && !name.trim().isEmpty()) {
            SQL.append(" AND LOWER(r.full_name) LIKE LOWER(?)");
            parameters.add("%" + name.trim() + "%");
        }
        
        if (apartmentId != null) {
            SQL.append(" AND r.apartment_id = ?");
            parameters.add(apartmentId);
        }
        
        if (status != null && !status.trim().isEmpty()) {
            SQL.append(" AND r.status = ?");
            parameters.add(status.trim());
        }
        
        SQL.append(" ORDER BY r.resident_id");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL.toString())) {
            
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ResidentPOJO resident = new ResidentPOJO();
                    resident.setResidentId(rs.getInt("resident_id"));
                    resident.setApartmentId(rs.getInt("apartment_id"));
                    
                    int userId = rs.getInt("user_id");
                    if (!rs.wasNull()) {
                        resident.setUserId(userId);
                    }
                    
                    resident.setFullName(rs.getString("full_name"));
                    
                    Date dateOfBirth = rs.getDate("date_of_birth");
                    if (dateOfBirth != null) {
                        resident.setDateOfBirth(dateOfBirth);
                    }
                    
                    resident.setIdCardNumber(rs.getString("id_card_number"));
                    resident.setRelationship(rs.getString("relationship"));
                    resident.setStatus(rs.getString("status"));
                    
                    Date moveInDate = rs.getDate("move_in_date");
                    if (moveInDate != null) {
                        resident.setMoveInDate(moveInDate);
                    }
                    
                    // Thêm thông tin từ bảng users
                    String phoneNumber = rs.getString("phone_number");
                    String email = rs.getString("email");
                    
                    // Tạo một lớp mở rộng để chứa thông tin bổ sung
                    ExtendedResidentPOJO extendedResident = new ExtendedResidentPOJO(resident);
                    extendedResident.setPhoneNumber(phoneNumber);
                    extendedResident.setEmail(email);
                    
                    residents.add(extendedResident);
                }
            }
        }
        
        return residents;
    }

    /**
     * Lớp mở rộng ResidentPOJO để chứa thông tin từ bảng users
     */
    public static class ExtendedResidentPOJO extends ResidentPOJO {
        private String phoneNumber;
        private String email;
        
        public ExtendedResidentPOJO(ResidentPOJO resident) {
            this.setResidentId(resident.getResidentId());
            this.setApartmentId(resident.getApartmentId());
            this.setUserId(resident.getUserId());
            this.setFullName(resident.getFullName());
            this.setDateOfBirth(resident.getDateOfBirth());
            this.setIdCardNumber(resident.getIdCardNumber());
            this.setRelationship(resident.getRelationship());
            this.setStatus(resident.getStatus());
            this.setMoveInDate(resident.getMoveInDate());
        }
        
        public String getPhoneNumber() {
            return phoneNumber;
        }
        
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
}
