
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.utils.DatabaseConnection; // Lớp tiện ích hiện có
import com.example.quanlytoanha.model.Role;
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
            // (Lưu ý: Bạn nên dùng UserDAO với Transaction để đảm bảo userId được tạo)
            if (resident.getUserId() > 0) { // SỬA: Bỏ kiểm tra != null vì getUserId() là int
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

            // 7. status <-- BỊ XÓA (XÓA CẢ DÒNG pstmt.setString(7, ...))

            // 8. moveInDate <-- BỊ XÓA (XÓA CẢ DÒNG pstmt.setDate(8, ...))

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        }
    }

    /**
     * Lấy tất cả thông tin Resident (bao gồm cả thông tin Users liên quan).
     * @return List<Resident>
     */
    public java.util.List<Resident> getAllResidents() throws SQLException {
        java.util.List<Resident> residents = new java.util.ArrayList<>();

        // Sử dụng LEFT JOIN để lấy cả cư dân có và không có tài khoản user
        String SQL = "SELECT u.user_id, u.username, u.email, u.full_name, u.role_id, u.created_at, u.last_login, u.phone_number, " +
                "r.resident_id, r.apartment_id, r.date_of_birth, r.id_card_number, r.relationship, r.status, r.move_in_date " +
                "FROM residents r LEFT JOIN users u ON r.user_id = u.user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL);
             ResultSet rs = pstmt.executeQuery()) {

            // Lặp qua kết quả và tạo đối tượng Resident
            while (rs.next()) {
                Resident resident = mapResultSetToResident(rs);
                residents.add(resident);
            }
        }
        return residents;
    }

    /**
     * Tìm kiếm cư dân theo tên, căn hộ và trạng thái
     * @param name Tên cư dân (có thể null)
     * @param apartmentId ID căn hộ (có thể null)
     * @param status Trạng thái cư dân (có thể null)
     * @return List<Resident> danh sách cư dân phù hợp
     */
    public java.util.List<Resident> searchResidents(String name, Integer apartmentId, String status) throws SQLException {
        java.util.List<Resident> residents = new java.util.ArrayList<>();
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.user_id, u.username, u.email, u.full_name, u.role_id, u.created_at, u.last_login, u.phone_number, ");
        sql.append("r.resident_id, r.apartment_id, r.date_of_birth, r.id_card_number, r.relationship, r.status, r.move_in_date ");
        sql.append("FROM residents r LEFT JOIN users u ON r.user_id = u.user_id WHERE 1=1");
        
        java.util.List<Object> parameters = new java.util.ArrayList<>();
        
        if (name != null && !name.trim().isEmpty()) {
            sql.append(" AND (u.full_name LIKE ? OR r.full_name LIKE ?)");
            parameters.add("%" + name.trim() + "%");
            parameters.add("%" + name.trim() + "%");
        }
        
        if (apartmentId != null) {
            sql.append(" AND r.apartment_id = ?");
            parameters.add(apartmentId);
        }
        
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND r.status = ?");
            parameters.add(status);
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Resident resident = mapResultSetToResident(rs);
                    residents.add(resident);
                }
            }
        }
        
        return residents;
    }

    // Phương thức giả định để ánh xạ dữ liệu (Bạn cần tự triển khai chi tiết)
    private Resident mapResultSetToResident(ResultSet rs) throws SQLException {
        // Tạo đối tượng Resident bằng constructor rỗng
        Resident resident = new Resident();
        
        // Lấy thông tin User (có thể NULL do LEFT JOIN)
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            resident.setUserId(userId);
            resident.setUsername(rs.getString("username"));
            resident.setEmail(rs.getString("email"));
            resident.setPhoneNumber(rs.getString("phone_number"));
            
            // Xử lý Role
            int roleId = rs.getInt("role_id");
            if (!rs.wasNull() && roleId > 0) {
                try {
                    resident.setRole(Role.fromId(roleId));
                } catch (IllegalArgumentException e) {
                    // Nếu roleId không hợp lệ, đặt mặc định là RESIDENT
                    resident.setRole(Role.RESIDENT);
                }
            } else {
                // Nếu không có roleId, đặt mặc định là RESIDENT
                resident.setRole(Role.RESIDENT);
            }
            
            // Xử lý timestamps
            java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                resident.setCreatedAt(createdAt);
            }
            
            java.sql.Timestamp lastLogin = rs.getTimestamp("last_login");
            if (lastLogin != null) {
                resident.setLastLogin(lastLogin);
            }
        }

        // Lấy thông tin Resident (luôn có)
        resident.setResidentId(rs.getInt("resident_id"));
        resident.setApartmentId(rs.getInt("apartment_id"));
        resident.setIdCardNumber(rs.getString("id_card_number"));
        resident.setRelationship(rs.getString("relationship"));
        resident.setStatus(rs.getString("status"));
        
        // Xử lý fullName - ưu tiên từ bảng users, nếu không có thì dùng từ residents
        String fullName = rs.getString("full_name");
        if (fullName == null || fullName.trim().isEmpty()) {
            // Nếu không có fullName từ users, có thể lấy từ bảng residents nếu có
            // Hoặc có thể để trống
            fullName = "Cư dân #" + resident.getResidentId();
        }
        resident.setFullName(fullName);
        
        // Xử lý DateOfBirth
        java.util.Date dateOfBirth = rs.getDate("date_of_birth");
        if (dateOfBirth != null) {
            resident.setDateOfBirth(dateOfBirth);
        }
        
        // Xử lý MoveInDate
        java.util.Date moveInDate = rs.getDate("move_in_date");
        if (moveInDate != null) {
            resident.setMoveInDate(moveInDate);
        }

        return resident;
    }

    /**
     * Xóa cư dân khỏi hệ thống.
     * Phương thức này sẽ xóa cư dân và xử lý các ràng buộc liên quan.
     * @param residentId ID của cư dân cần xóa.
     * @return true nếu xóa thành công, false nếu không tìm thấy cư dân.
     * @throws SQLException nếu có lỗi trong quá trình xóa.
     */
    public boolean removeResident(int residentId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction
            
            // Kiểm tra xem cư dân có tồn tại không
            String checkSQL = "SELECT resident_id FROM residents WHERE resident_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
                checkStmt.setInt(1, residentId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        // Cư dân không tồn tại
                        conn.rollback();
                        return false;
                    }
                }
            }
            
            // Xóa các phương tiện liên quan trước (do foreign key constraint)
            String deleteVehiclesSQL = "DELETE FROM vehicles WHERE resident_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteVehiclesSQL)) {
                pstmt.setInt(1, residentId);
                pstmt.executeUpdate();
            }
            
            // Xóa cư dân
            String deleteResidentSQL = "DELETE FROM residents WHERE resident_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteResidentSQL)) {
                pstmt.setInt(1, residentId);
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    conn.commit(); // Xác nhận transaction
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    // Log rollback exception if needed
                    System.err.println("Lỗi khi rollback transaction: " + rollbackEx.getMessage());
                }
            }
            throw e; // Ném lại exception để caller xử lý
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Khôi phục auto-commit
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Lỗi khi đóng connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Xóa cư dân theo user ID.
     * Phương thức này tìm resident_id dựa trên user_id và sau đó xóa cư dân.
     * @param userId ID của người dùng.
     * @return true nếu xóa thành công, false nếu không tìm thấy cư dân.
     * @throws SQLException nếu có lỗi trong quá trình xóa.
     */
    public boolean removeResidentByUserId(int userId) throws SQLException {
        // Tìm resident_id từ user_id
        String findResidentSQL = "SELECT resident_id FROM residents WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(findResidentSQL)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int residentId = rs.getInt("resident_id");
                    return removeResident(residentId);
                } else {
                    return false; // Không tìm thấy cư dân với user_id này
                }
            }
        }
    }

    /**
     * Phương thức mới: Lấy Resident đầy đủ từ DB để nạp vào form Edit.
     * Thực hiện JOIN giữa bảng users và residents để có đủ thông tin.
     * @param userId ID của người dùng.
     * @return Đối tượng Resident hoàn chỉnh hoặc null nếu không tìm thấy.
     */
    public Resident getResidentByUserId(int userId) throws SQLException {
        Resident resident = null;

        // Truy vấn JOIN: Lấy tất cả cột từ users (u) và residents (r)
        String SQL = "SELECT u.*, r.* FROM users u " +
                "JOIN residents r ON u.user_id = r.user_id " +
                "WHERE u.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("DAO DEBUG: Đã tìm thấy bản ghi cho User ID: " + userId);

                    // Khởi tạo đối tượng bằng constructor rỗng
                    resident = new Resident();

                    // --- 1. ÁNH XẠ DỮ LIỆU USER (TỪ BẢNG 'users') ---
                    try {
                        resident.setUserId(rs.getInt("user_id"));
                        resident.setUsername(rs.getString("username"));
                        resident.setEmail(rs.getString("email"));
                        resident.setFullName(rs.getString("full_name"));

                        // Ánh xạ Role
                        int roleId = rs.getInt("role_id");
                        resident.setRole(Role.fromId(roleId));

                        // CÁC TRƯỜNG THỜI GIAN/THAM SỐ CÓ THỂ NULL (RẤT DỄ GÂY LỖI NẾU KHÔNG XỬ LÝ)
                        resident.setCreatedAt(rs.getTimestamp("created_at"));
                        resident.setLastLogin(rs.getTimestamp("last_login"));
                        resident.setPhoneNumber(rs.getString("phone_number"));

                        // --- 2. ÁNH XẠ DỮ LIỆU RESIDENT (TỪ BẢNG 'residents') ---
                        resident.setResidentId(rs.getInt("resident_id"));
                        resident.setApartmentId(rs.getInt("apartment_id"));

                        // Xử lý DateOfBirth (DÙNG rs.getDate() trả về java.util.Date)
                        resident.setDateOfBirth(rs.getDate("date_of_birth"));

                        resident.setIdCardNumber(rs.getString("id_card_number"));
                        resident.setRelationship(rs.getString("relationship"));

                    } catch (Exception e) {
                        // Nếu có lỗi, in ra ngoại lệ thực sự và ném lại SQL Exception
                        System.err.println("LỖI ÁNH XẠ DỮ LIỆU RESIDENT:");
                        e.printStackTrace();
                        // Ném lại ngoại lệ để tầng Controller xử lý và hiển thị lỗi
                        throw new SQLException("Lỗi ánh xạ dữ liệu: " + e.getMessage(), e);
                    }

                    // Không cần dòng "TẠO ĐỐI TƯỢNG RESIDENT BẰNG CONSTRUCTOR" cũ nữa
                }
                return resident;
            }
        }
    }
}