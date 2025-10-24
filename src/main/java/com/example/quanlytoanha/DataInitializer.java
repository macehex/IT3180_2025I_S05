package com.example.quanlytoanha;

// Vị trí: src/main/java/com/example/quanlytoanha/DataInitializer.java
import com.example.quanlytoanha.utils.DatabaseConnection;
import com.example.quanlytoanha.utils.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant; // Dùng để lấy thời gian hiện tại

/**
 * Lớp tiện ích để thêm dữ liệu mẫu vào database.
 * Chỉ chạy 1 LẦN.
 */
public class DataInitializer {

    public static void main(String[] args) {
        // --- CẤU HÌNH USER BẠN MUỐN TẠO ---
        int userId = 1; // <-- SỬA ĐỔI: Thêm user_id
        String username = "admin";
        String plainPassword = "admin123"; // Mật khẩu bạn sẽ dùng để đăng nhập
        String email = "admin@example.com";
        String fullName = "Quản Trị Viên";
        int roleId = 1; // QUAN TRỌNG: Đảm bảo role_id = 1 là "ADMIN" trong bảng 'roles'
        // ------------------------------------

        try {
            // 1. Băm mật khẩu
            String hashedPassword = PasswordUtil.hashPassword(plainPassword);
            System.out.println("Đã băm mật khẩu: " + plainPassword + " -> " + hashedPassword);

            // 2. Chuẩn bị câu lệnh SQL
            // <-- SỬA ĐỔI: Thêm "user_id" vào câu lệnh SQL
            String sql = "INSERT INTO users (user_id, username, password, email, full_name, role_id, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)"; // <-- SỬA ĐỔI: Thêm 1 dấu ? (tổng 7)

            // 3. Kết nối và thực thi
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // <-- SỬA ĐỔI: Gán các tham số, bắt đầu bằng user_id
                pstmt.setInt(1, userId); // (Tham số 1)
                pstmt.setString(2, username); // (Tham số 2)
                pstmt.setString(3, hashedPassword); // (Tham số 3)
                pstmt.setString(4, email); // (Tham số 4)
                pstmt.setString(5, fullName); // (Tham số 5)
                pstmt.setInt(6, roleId); // (Tham số 6)
                pstmt.setTimestamp(7, Timestamp.from(Instant.now())); // (Tham số 7)

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("--- THÀNH CÔNG ---");
                    System.out.println("Đã thêm user '" + username + "' (ID=" + userId + ") vào database.");
                    System.out.println("Bạn có thể đăng nhập bằng mật khẩu: '" + plainPassword + "'");
                } else {
                    System.out.println("--- THẤT BẠI ---");
                    System.out.println("Không thêm được user.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi thêm user: " + e.getMessage());
        }
    }
}
