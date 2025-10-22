// Vị trí: src/main/java/com/example/quanlytoanha/dao/UserDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.*;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

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
                // user.setPassword(hashedPassword); // --> Bạn cần thêm
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
}
