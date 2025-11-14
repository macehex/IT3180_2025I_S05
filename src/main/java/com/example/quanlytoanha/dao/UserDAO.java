// Vị trí: src/main/java/com/example/quanlytoanha/dao/UserDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.*;
import com.example.quanlytoanha.utils.DatabaseConnection;
import com.example.quanlytoanha.utils.PasswordUtil;
import com.google.gson.Gson; // <-- BỔ SUNG: Cho chuyển đổi JSON (Cần thư viện GSON)
import java.time.Instant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Statement;
import java.sql.SQLException;

// (Giả sử bạn có một lớp DBConnection để kết nối)

public class UserDAO {

    // Khởi tạo DAO cho lịch sử
    private final ResidentHistoryDAO historyDAO = new ResidentHistoryDAO();

    // --- Các hàm đã có (getUserById, findUserByUsername, updateUserProfile, addResident) GIỮ NGUYÊN ---

    /**
     * Lấy một User từ DB bằng ID.
     */
    public User getUserById(int userId) {
        User user = null;
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int roleId = rs.getInt("role_id");
                Role userRole = Role.fromId(roleId);

                String username = rs.getString("username");
                String email = rs.getString("email");
                String fullName = rs.getString("full_name");
                String phone = rs.getString("phone_number");
                Timestamp createdAt = rs.getTimestamp("created_at");
                Timestamp lastLogin = rs.getTimestamp("last_login");

                switch (userRole) {
                    case ADMIN:
                        user = new Admin(userId, username, email, fullName, userRole, createdAt, lastLogin, phone);
                        break;
                    case ACCOUNTANT:
                        user = new Accountant(userId, username, email, fullName, userRole, createdAt, lastLogin, phone);
                        break;
                    case POLICE:
                        user = new Police(userId, username, email, fullName, userRole, createdAt, lastLogin, phone);
                        break;
                    case RESIDENT:
                        user = new Resident(userId, username, email, fullName, userRole, createdAt, lastLogin, phone,
                                0, 0, null, "", "");
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
     */
    public User findUserByUsername(String username) {
        User user = null;
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                int roleId = rs.getInt("role_id");
                Role userRole = Role.fromId(roleId);

                String email = rs.getString("email");
                String fullName = rs.getString("full_name");
                String phone = rs.getString("phone_number");
                Timestamp createdAt = rs.getTimestamp("created_at");
                Timestamp lastLogin = rs.getTimestamp("last_login");
                String hashedPassword = rs.getString("password");

                switch (userRole) {
                    case ADMIN:
                        user = new Admin(userId, username, email, fullName, userRole, createdAt, lastLogin, phone);
                        break;
                    case ACCOUNTANT:
                        user = new Accountant(userId, username, email, fullName, userRole, createdAt, lastLogin, phone);
                        break;
                    case POLICE:
                        user = new Police(userId, username, email, fullName, userRole, createdAt, lastLogin, phone);
                        break;
                    case RESIDENT:
                        user = new Resident(userId, username, email, fullName, userRole, createdAt, lastLogin, phone,
                                0, 0, null, "", "");
                        break;
                    default:
                        throw new IllegalStateException("Vai trò không xác định: " + userRole);
                }
                user.setPassword(hashedPassword);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * Cập nhật profile của User (không phải Resident).
     */
    public void updateUserProfile(User user) {
        String sql = "UPDATE users SET email = ?, phone_number = ?, role_id = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPhoneNumber());
            int roleIdToSave = user.getRole().getRoleId();

            pstmt.setInt(3, roleIdToSave);
            pstmt.setInt(4, user.getUserId());

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Thêm một hồ sơ cư dân mới (Giữ nguyên).
     */
    public boolean addResident(Resident resident) throws SQLException {
        String userSql = "INSERT INTO users (username, password, full_name, role_id, created_at, phone_number, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String residentSql = "INSERT INTO residents (user_id, apartment_id, date_of_birth, id_card_number, relationship) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        int generatedUserId = -1;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // --- PHẦN 1: INSERT VÀO BẢNG USERS ---
            try (PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                String hashedPassword = PasswordUtil.hashPassword(resident.getPassword());

                userStmt.setString(1, resident.getUsername());
                userStmt.setString(2, hashedPassword);
                userStmt.setString(3, resident.getFullName());
                userStmt.setInt(4, resident.getRole().getRoleId());
                userStmt.setTimestamp(5, Timestamp.from(Instant.now()));
                userStmt.setString(6, resident.getPhoneNumber());
                userStmt.setString(7, resident.getEmail());

                userStmt.executeUpdate();

                ResultSet rs = userStmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedUserId = rs.getInt(1);
                } else {
                    throw new SQLException("Không thể lấy user ID tự tăng sau khi INSERT users.");
                }

                resident.setUserId(generatedUserId);
            }

            // --- PHẦN 2: INSERT VÀO BẢNG RESIDENTS ---
            if (generatedUserId != -1) {
                try (PreparedStatement residentStmt = conn.prepareStatement(residentSql)) {
                    java.sql.Date sqlDateOfBirth = (resident.getDateOfBirth() != null)
                            ? new java.sql.Date(resident.getDateOfBirth().getTime())
                            : null;

                    residentStmt.setInt(1, generatedUserId);
                    residentStmt.setInt(2, resident.getApartmentId());
                    residentStmt.setDate(3, sqlDateOfBirth);
                    residentStmt.setString(4, resident.getIdCardNumber());
                    residentStmt.setString(5, resident.getRelationship());

                    residentStmt.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                conn.rollback();
            }
            throw new SQLException("Thêm cư dân thất bại: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // --- BỔ SUNG: HÀM UPDATE VÀ GHI LOG (US1_1_1.4) ---

    /**
     * Cập nhật thông tin của một Resident và ghi lại lịch sử thay đổi (Audit Log).
     * @param resident Đối tượng Resident chứa thông tin mới.
     * @param changedByUserId ID của Ban Quản trị thực hiện thay đổi.
     * @param oldDataJson Dữ liệu Cũ của Resident (JSON String).
     * @param newDataJson Dữ liệu Mới của Resident (JSON String).
     * @return true nếu cập nhật thành công.
     */
    public boolean updateResidentAndLog(Resident resident, int changedByUserId, String oldDataJson, String newDataJson) throws SQLException {

        // Cập nhật bảng users (fullName, phoneNumber, email)
        String userSql = "UPDATE users SET full_name = ?, phone_number = ?, email = ? WHERE user_id = ?";

        // Cập nhật bảng residents
        String residentSql = "UPDATE residents SET date_of_birth = ?, id_card_number = ?, relationship = ?, apartment_id = ? WHERE resident_id = ?";

        Connection conn = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Cập nhật bảng users
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, resident.getFullName());
                userStmt.setString(2, resident.getPhoneNumber());
                userStmt.setString(3, resident.getEmail());
                userStmt.setInt(4, resident.getUserId());
                userStmt.executeUpdate();
            }

            // 2. Cập nhật bảng residents
            try (PreparedStatement residentStmt = conn.prepareStatement(residentSql)) {
                java.sql.Date sqlDateOfBirth = (resident.getDateOfBirth() != null)
                        ? new java.sql.Date(resident.getDateOfBirth().getTime())
                        : null;

                residentStmt.setDate(1, sqlDateOfBirth);
                residentStmt.setString(2, resident.getIdCardNumber());
                residentStmt.setString(3, resident.getRelationship());
                residentStmt.setInt(4, resident.getApartmentId());
                residentStmt.setInt(5, resident.getResidentId());

                residentStmt.executeUpdate();
            }

            // --- BƯỚC 3: GHI LỊCH SỬ THAY ĐỔI (AUDIT LOG) ---
            // Kiểm tra: Chỉ ghi log nếu có sự khác biệt về nội dung JSON
            if (oldDataJson != null && newDataJson != null && !oldDataJson.equals(newDataJson)) {
                historyDAO.addHistory(
                        conn,
                        resident.getResidentId(),
                        changedByUserId,
                        oldDataJson,
                        newDataJson
                );
            }

            conn.commit();
            success = true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                conn.rollback();
            }
            throw new SQLException("Cập nhật hồ sơ và ghi log thất bại: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
        return success;
    }

    /**
     * (Hàm cũ: updateResident, ĐÃ BỊ LOẠI BỎ vì nó không ghi log.
     * Hàm updateResidentAndLog sẽ thay thế nó.)
     * * Nếu bạn vẫn cần hàm không ghi log, hãy sao chép logic của updateResidentAndLog
     * và bỏ qua phần gọi historyDAO.addHistory().
     * * Ví dụ:
     * public boolean updateResident(Resident resident) throws SQLException {
     * // ... logic cập nhật users/residents ...
     * return true;
     * }
     */

    // --- BỔ SUNG: HÀM HỖ TRỢ LẤY DỮ LIỆU CŨ TỪ RESIDENT ID (Dùng trong Service) ---

    public Resident getResidentByResidentId(int residentId) throws SQLException {
        Resident resident = null;
        // Sử dụng logic của ResidentDAO.getResidentByUserId nhưng dựa trên resident_id
        String SQL = "SELECT u.*, r.* FROM residents r LEFT JOIN users u ON r.user_id = u.user_id WHERE r.resident_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, residentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Cần ResidentDAO để ánh xạ kết quả
                    ResidentDAO residentDAO = new ResidentDAO();
                    // Gọi hàm ánh xạ dữ liệu phức tạp
                    resident = residentDAO.mapResultSetToResident(rs);
                }
            }
        }
        return resident;
    }

    /**
     * Cập nhật mật khẩu của người dùng. (Giữ nguyên)
     */
    public static boolean updateUserPassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật thời gian đăng nhập cuối cùng của người dùng. (Giữ nguyên)
     */
    public static boolean updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.from(Instant.now()));
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}