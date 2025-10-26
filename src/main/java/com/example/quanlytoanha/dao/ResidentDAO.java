
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.utils.DatabaseConnection; // Lớp tiện ích hiện có
import com.example.quanlytoanha.model.Role;
import java.sql.*;
import java.util.Date; // Cần thiết cho DateOfBirth

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

        // SỬ DỤNG LỆNH JOIN để lấy thông tin từ cả users và residents
        String SQL = "SELECT u.user_id, u.username, u.email, u.full_name, u.role_id, u.created_at, u.last_login, u.phone_number, " +
                "r.resident_id, r.apartment_id, r.date_of_birth, r.id_card_number, r.relationship " +
                "FROM users u JOIN residents r ON u.user_id = r.user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL);
             ResultSet rs = pstmt.executeQuery()) {

            // Lặp qua kết quả và tạo đối tượng Resident
            while (rs.next()) {
                // Bạn cần một phương thức utility để ánh xạ ResultSet sang Resident
                Resident resident = mapResultSetToResident(rs);
                residents.add(resident);
            }
        }
        return residents;
    }

    // Phương thức giả định để ánh xạ dữ liệu (Bạn cần tự triển khai chi tiết)
    private Resident mapResultSetToResident(ResultSet rs) throws SQLException {
        // Lấy thông tin User
        int userId = rs.getInt("user_id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String fullName = rs.getString("full_name");
        // Giả định bạn có Role.getRoleById(int)
        // Role role = Role.getRoleById(rs.getInt("role_id"));

        // Lấy thông tin Resident
        int residentId = rs.getInt("resident_id");
        int apartmentId = rs.getInt("apartment_id");
        java.util.Date dateOfBirth = rs.getDate("date_of_birth");
        String idCardNumber = rs.getString("id_card_number");
        String relationship = rs.getString("relationship");

        // TẠO OBJECT RESIDENT
        // Resident resident = new Resident(userId, username, email, fullName, role, ...);
        // Do constructor của bạn có nhiều tham số, cách dễ nhất là dùng setter (yêu cầu constructor rỗng)
        Resident resident = new Resident();
        resident.setUserId(userId);
        resident.setUsername(username);
        resident.setEmail(email);
        resident.setFullName(fullName);
        // resident.setRole(role);
        resident.setPhoneNumber(rs.getString("phone_number"));

        resident.setResidentId(residentId);
        resident.setApartmentId(apartmentId);
        resident.setDateOfBirth(dateOfBirth);
        resident.setIdCardNumber(idCardNumber);
        resident.setRelationship(relationship);

        return resident;
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