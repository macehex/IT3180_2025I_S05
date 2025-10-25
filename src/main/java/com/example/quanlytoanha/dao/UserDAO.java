// Vị trí: src/main/java/com/example/quanlytoanha/dao/UserDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.*;
import com.example.quanlytoanha.utils.DatabaseConnection;
import com.example.quanlytoanha.utils.PasswordUtil; // 1. KHẮC PHỤC LỖI PasswordUtil
import java.time.Instant; // 3. KHẮC PHỤC LỖI Instant (Dùng cho Timestamp.from(Instant.now()))
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Statement; // <-- THÊM DÒNG NÀY
import java.sql.SQLException; // <-- Đảm bảo dòng này có


// (Giả sử bạn có một lớp DBConnection để kết nối)

public class UserDAO {

    /**
     * Lấy một User từ DB bằng ID.
     * Đây là nơi phép "phiên dịch" diễn ra.
     */
    public User getUserById(int userId) {
        User user = null;
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // 1. Lấy role_id từ DB (dưới dạng INT)
                int roleId = rs.getInt("role_id");

                // 2. PHIÊN DỊCH: Chuyển INT thành ENUM
                // Chúng ta dùng phương thức Role.fromId() đã tạo ở bước trước
                Role userRole = Role.fromId(roleId);

                // Lấy các thông tin chung khác
                String username = rs.getString("username");
                String email = rs.getString("email");
                String fullName = rs.getString("full_name");
                String phone = rs.getString("phone_number");
                Timestamp createdAt = rs.getTimestamp("created_at");
                Timestamp lastLogin = rs.getTimestamp("last_login");

                // 3. QUAN TRỌNG: Dùng Factory Pattern để tạo đúng đối tượng
                // Dựa vào Enum Role, ta quyết định tạo Admin, Resident, hay Accountant
                switch (userRole) {
                    case ADMIN:
                        user = new Admin(userId, username, email, fullName, userRole, createdAt, lastLogin, phone);
                        break;
                    case ACCOUNTANT:
                        user = new Accountant(userId, username, email, fullName, userRole, createdAt, lastLogin, phone);
                        break;
                    case POLICE:
                        // (Tương tự, tạo new Police(...))
                        break;
                    case RESIDENT:
                        // Đối với Resident, bạn cần JOIN thêm bảng 'residents'
                        // (Đây là ví dụ đơn giản, thực tế bạn cần query thêm)
                        // int residentId = ... (lấy từ bảng residents)
                        // user = new Resident(...);
                        break;
                    default:
                        throw new IllegalStateException("Vai trò không xác định: " + userRole);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * Tìm một người dùng bằng username (dùng cho việc login).
     *
     * @param username Tên đăng nhập
     * @return Đối tượng User (Admin, Resident,...) nếu tìm thấy, ngược lại trả về null
     */
    public User findUserByUsername(String username) {
        User user = null;
        // Lấy tất cả thông tin user, bao gồm cả password (đã băm)
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection(); // Giả sử bạn có lớp DBConnection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                int roleId = rs.getInt("role_id");

                // "Phiên dịch" int sang Enum Role
                Role userRole = Role.fromId(roleId);

                // Lấy các thông tin chung
                String email = rs.getString("email");
                String fullName = rs.getString("full_name");
                String phone = rs.getString("phone_number");
                Timestamp createdAt = rs.getTimestamp("created_at");
                Timestamp lastLogin = rs.getTimestamp("last_login");

                // !! QUAN TRỌNG: Lấy mật khẩu đã băm từ DB
                String hashedPassword = rs.getString("password");

                // Dùng "Factory" để tạo đúng đối tượng User
                switch (userRole) {
                    case ADMIN:
                        user = new Admin(userId, username, email, fullName, userRole, createdAt, lastLogin, phone);
                        break;
                    case ACCOUNTANT:
                        user = new Accountant(userId, username, email, fullName, userRole, createdAt, lastLogin, phone);
                        break;
                    // case POLICE:
                    //     user = new Police(...);
                    //     break;
                    case RESIDENT:
                        // !! Với Resident, bạn cần query (JOIN) thêm thông tin từ bảng 'residents'
                        // (Để đơn giản, tạm thời ta tạo Resident với thông tin cơ bản)
                        // (Sau này bạn sẽ cải tiến query này để lấy đủ thông tin Resident)
                        user = new Resident(userId, username, email, fullName, userRole, createdAt, lastLogin, phone,
                                0, 0, null, "", ""); // ID, ApartmentId, DOB,... tạm
                        break;
                    default:
                        throw new IllegalStateException("Vai trò không xác định: " + userRole);
                }

                // !! NẠP mật khẩu băm vào đối tượng User
                // (Bạn cần thêm trường `password` và setter/getter cho nó trong Abstract User)
                user.setPassword(hashedPassword); // --> Bạn cần thêm
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user; // Trả về null nếu không tìm thấy user
    }

    // Trong lớp UserDAO
    public void updateUserProfile(User user) {
        // Chúng ta muốn cập nhật email, phone, và roleId
        String sql = "UPDATE users SET email = ?, phone_number = ?, role_id = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPhoneNumber()); // Giả sử bạn đã thêm getter/setter này

            // 3. PHIÊN DỊCH NGƯỢC: Chuyển ENUM thành INT
            // Chúng ta dùng getter của Enum để lấy ra số INT
            int roleIdToSave = user.getRole().getRoleId();

            pstmt.setInt(3, roleIdToSave);
            pstmt.setInt(4, user.getUserId());

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Thêm một hồ sơ cư dân mới. Thực hiện INSERT vào bảng users và residents
     * trong một Database Transaction.
     *
     * @param resident Đối tượng Resident chứa thông tin cần lưu.
     * @return true nếu thêm thành công.
     */
    public boolean addResident(Resident resident) throws SQLException {
        // SQL cho bảng users (Lấy khóa tự tăng - user_id)
        String userSql = "INSERT INTO users (username, password, full_name, role_id, created_at, phone_number, email) VALUES (?, ?, ?, ?, ?, ?, ?)";

        // SQL cho bảng residents
        // Lưu ý: SQL này đã được tối giản (loại bỏ status, move_in_date) để khớp với Resident.java hiện tại
        String residentSql = "INSERT INTO residents (user_id, apartment_id, date_of_birth, id_card_number, relationship) VALUES (?, ?, ?, ?, ?)"; // 5 THAM SỐ

        Connection conn = null;
        int generatedUserId = -1;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // --- PHẦN 1: INSERT VÀO BẢNG USERS ---
            try (PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {

                // Giả định ResidentService đã gán Username và Mật khẩu tạm thời
                String hashedPassword = PasswordUtil.hashPassword(resident.getPassword());

                userStmt.setString(1, resident.getUsername());
                userStmt.setString(2, hashedPassword);
                userStmt.setString(3, resident.getFullName());
                // Giả định Resident đã được gán Role.RESIDENT
                userStmt.setInt(4, resident.getRole().getRoleId());
                userStmt.setTimestamp(5, Timestamp.from(Instant.now()));
                userStmt.setString(6, resident.getPhoneNumber()); // Cần có getter trong User
                userStmt.setString(7, resident.getEmail());     // Cần có getter trong User

                userStmt.executeUpdate();

                // Lấy ID tự tăng (user_id)
                ResultSet rs = userStmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedUserId = rs.getInt(1);
                } else {
                    throw new SQLException("Không thể lấy user ID tự tăng sau khi INSERT users.");
                }

                // Cập nhật lại userId cho đối tượng Resident
                resident.setUserId(generatedUserId);
            }

            // --- PHẦN 2: INSERT VÀO BẢNG RESIDENTS ---
            if (generatedUserId != -1) {
                try (PreparedStatement residentStmt = conn.prepareStatement(residentSql)) {

                    // Chuyển java.util.Date sang java.sql.Date
                    java.sql.Date sqlDateOfBirth = (resident.getDateOfBirth() != null)
                            ? new java.sql.Date(resident.getDateOfBirth().getTime())
                            : null;

                    residentStmt.setInt(1, generatedUserId); // SỬ DỤNG USER ID VỪA TẠO
                    // getApartmentId() của bạn trả về int
                    residentStmt.setInt(2, resident.getApartmentId());
                    residentStmt.setDate(3, sqlDateOfBirth);
                    residentStmt.setString(4, resident.getIdCardNumber());
                    residentStmt.setString(5, resident.getRelationship());

                    residentStmt.executeUpdate();
                }
            }

            conn.commit(); // Ghi nhận thay đổi nếu cả 2 INSERT thành công
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                conn.rollback(); // Hoàn tác nếu có lỗi
            }
            throw new SQLException("Thêm cư dân thất bại: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    /**
     * Cập nhật thông tin của một Resident (Cư dân) trên cả 2 bảng (users và residents).
     * @param resident Đối tượng Resident chứa thông tin mới.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean updateResident(Resident resident) throws SQLException {
        // Cập nhật bảng users (fullName, phoneNumber, email)
        String userSql = "UPDATE users SET full_name = ?, phone_number = ?, email = ? WHERE user_id = ?";

        // Cập nhật bảng residents (dateOfBirth, idCardNumber, relationship, apartment_id)
        // Lưu ý: SQL này đã được tối giản để khớp với Resident.java hiện tại
        String residentSql = "UPDATE residents SET date_of_birth = ?, id_card_number = ?, relationship = ?, apartment_id = ? WHERE resident_id = ?";

        Connection conn = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Cập nhật bảng users
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, resident.getFullName());
                userStmt.setString(2, resident.getPhoneNumber()); // Giả định có getter trong User
                userStmt.setString(3, resident.getEmail());      // Giả định có getter trong User
                userStmt.setInt(4, resident.getUserId());
                userStmt.executeUpdate();
            }

            // 2. Cập nhật bảng residents
            try (PreparedStatement residentStmt = conn.prepareStatement(residentSql)) {

                // Chuyển java.util.Date sang java.sql.Date
                java.sql.Date sqlDateOfBirth = (resident.getDateOfBirth() != null)
                        ? new java.sql.Date(resident.getDateOfBirth().getTime())
                        : null;

                residentStmt.setDate(1, sqlDateOfBirth);
                residentStmt.setString(2, resident.getIdCardNumber());
                residentStmt.setString(3, resident.getRelationship());
                residentStmt.setInt(4, resident.getApartmentId()); // Thêm cập nhật ApartmentId
                residentStmt.setInt(5, resident.getResidentId()); // Điều kiện WHERE

                residentStmt.executeUpdate();
            }

            conn.commit(); // Ghi nhận thay đổi nếu cả 2 thành công
            success = true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                conn.rollback(); // Hoàn tác nếu có lỗi
            }
            // Ném lại lỗi để tầng Service xử lý
            throw new SQLException("Cập nhật hồ sơ thất bại: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
        return success;
    }
}
