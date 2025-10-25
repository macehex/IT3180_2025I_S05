// Vị trí: src/main/java/com/example/quanlytoanha/dao/ResidentDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.model.Role;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO để truy vấn dữ liệu cư dân từ database
 * (ĐÃ GỘP TÍNH NĂNG TỪ 2 BRANCH)
 */
public class ResidentDAO {

    // ====================================================================
    // --- CÁC PHƯƠNG THỨC TỪ 'topic/login-logout' (Create/Validate) ---
    // ====================================================================

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
     * @param resident Đối tượng Resident chứa dữ liệu cần lưu.
     * @return true nếu thêm thành công.
     */
    public boolean addResident(Resident resident) throws SQLException {
        // SỬA SQL: Loại bỏ status và move_in_date để khớp với Resident.java
        String SQL = "INSERT INTO residents (apartment_id, user_id, full_name, date_of_birth, id_card_number, relationship) " +
                "VALUES (?, ?, ?, ?, ?, ?)"; // CHỈ CÒN 6 THAM SỐ

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            // 1. apartmentId
            pstmt.setInt(1, resident.getApartmentId());

            // 2. userId (Có thể NULL)
            if (resident.getUserId() > 0) {
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

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }


    // ====================================================================
    // --- CÁC PHƯƠNG THỨC TỪ 'feature/view-filter' (Read/Search/Stats) ---
    // --- Đây là phiên bản được chọn để giữ lại ---
    // ====================================================================

    /**
     * Lấy danh sách tất cả cư dân trong hệ thống
     * (GIỮ PHIÊN BẢN CỦA 'feature/view-filter' VÌ CHI TIẾT HƠN)
     * @return Danh sách tất cả cư dân
     */
    public List<Resident> getAllResidents() {
        List<Resident> residents = new ArrayList<>();
        String sql = """
            SELECT r.resident_id, r.apartment_id, r.user_id, r.full_name, r.date_of_birth, 
                   r.id_card_number, r.relationship, r.status, r.move_in_date, r.move_out_date,
                   u.username, u.email, u.phone_number, u.created_at, u.last_login, u.role_id,
                   a.area, owner.full_name as owner_name
            FROM residents r
            JOIN users u ON r.user_id = u.user_id
            LEFT JOIN apartments a ON r.apartment_id = a.apartment_id
            LEFT JOIN users owner ON a.owner_id = owner.user_id
            ORDER BY r.full_name
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Resident resident = mapResultSetToResident(rs);
                residents.add(resident);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách cư dân: " + e.getMessage());
            e.printStackTrace();
        }
        
        return residents;
    }
    
    /**
     * Tìm kiếm cư dân theo các tiêu chí đơn giản
     * @param fullName Tên cư dân (có thể null)
     * @param apartmentId ID căn hộ (có thể null)
     * @param status Trạng thái (có thể null)
     * @return Danh sách cư dân thỏa mãn tiêu chí
     */
    public List<Resident> searchResidents(String fullName, Integer apartmentId, String status) {
        List<Resident> residents = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT r.resident_id, r.apartment_id, r.user_id, r.full_name, r.date_of_birth, 
                   r.id_card_number, r.relationship, r.status, r.move_in_date, r.move_out_date,
                   u.username, u.email, u.phone_number, u.created_at, u.last_login, u.role_id,
                   a.area, owner.full_name as owner_name
            FROM residents r
            JOIN users u ON r.user_id = u.user_id
            LEFT JOIN apartments a ON r.apartment_id = a.apartment_id
            LEFT JOIN users owner ON a.owner_id = owner.user_id
            WHERE 1=1
            """);
        
        List<Object> parameters = new ArrayList<>();
        
        // Thêm các điều kiện tìm kiếm
        if (fullName != null && !fullName.trim().isEmpty()) {
            sql.append(" AND r.full_name LIKE ?");
            parameters.add("%" + fullName.trim() + "%");
        }
        
        if (apartmentId != null) {
            sql.append(" AND r.apartment_id = ?");
            parameters.add(apartmentId);
        }
        
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND r.status = ?");
            parameters.add(status.trim());
        }
        
        sql.append(" ORDER BY r.full_name");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Resident resident = mapResultSetToResident(rs);
                    residents.add(resident);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm cư dân: " + e.getMessage());
            e.printStackTrace();
        }
        
        return residents;
    }
    
    /**
     * Lấy thông tin chi tiết của một cư dân theo ID
     * @param residentId ID của cư dân
     * @return Thông tin cư dân hoặc null nếu không tìm thấy
     */
    public Resident getResidentById(int residentId) {
        String sql = """
            SELECT r.resident_id, r.apartment_id, r.user_id, r.full_name, r.date_of_birth, 
                   r.id_card_number, r.relationship, r.status, r.move_in_date, r.move_out_date,
                   u.username, u.email, u.phone_number, u.created_at, u.last_login, u.role_id,
                   a.area, owner.full_name as owner_name
            FROM residents r
            JOIN users u ON r.user_id = u.user_id
            LEFT JOIN apartments a ON r.apartment_id = a.apartment_id
            LEFT JOIN users owner ON a.owner_id = owner.user_id
            WHERE r.resident_id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, residentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToResident(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy thông tin cư dân: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Đếm tổng số cư dân trong hệ thống
     * @return Tổng số cư dân
     */
    public int getTotalResidentCount() {
        String sql = "SELECT COUNT(*) FROM residents";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi đếm số cư dân: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Đếm số cư dân theo trạng thái
     * @param status Trạng thái cần đếm
     * @return Số cư dân có trạng thái đó
     */
    public int getResidentCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM residents WHERE status = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi đếm cư dân theo trạng thái: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Chuyển đổi ResultSet thành đối tượng Resident
     * (GIỮ PHIÊN BẢN CỦA 'feature/view-filter' VÌ CHI TIẾT HƠN)
     */
    private Resident mapResultSetToResident(ResultSet rs) throws SQLException {
        // Lấy thông tin từ bảng users
        int userId = rs.getInt("user_id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String phoneNumber = rs.getString("phone_number");
        String fullName = rs.getString("full_name");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp lastLogin = rs.getTimestamp("last_login");
        int roleId = rs.getInt("role_id");
        
        // Lấy thông tin từ bảng residents
        int residentId = rs.getInt("resident_id");
        int apartmentId = rs.getInt("apartment_id");
        Date dateOfBirth = rs.getDate("date_of_birth");
        String idCardNumber = rs.getString("id_card_number");
        String relationship = rs.getString("relationship");
        String status = rs.getString("status");
        Date moveInDate = rs.getDate("move_in_date");
        Date moveOutDate = rs.getDate("move_out_date");
        
        // Tạo đối tượng Resident
        Role role = Role.fromId(roleId);
        Resident resident = new Resident(userId, username, email, fullName, role, 
                                        createdAt, lastLogin, phoneNumber,
                                        residentId, apartmentId, dateOfBirth, 
                                        idCardNumber, relationship, status, moveInDate, moveOutDate);
        
        // Thêm các thuộc tính JOIN (từ 'feature/view-filter')
        // (Giả sử bạn đã thêm setter cho các trường này trong model 'Resident')
        // resident.setApartmentArea(rs.getDouble("area"));
        // resident.setOwnerName(rs.getString("owner_name"));

        return resident;
    }
}