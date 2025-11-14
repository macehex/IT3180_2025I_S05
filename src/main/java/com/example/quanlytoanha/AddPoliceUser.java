// Vị trí: (Tạo file mới, ví dụ: com.example.quanlytoanha.utils.AddPoliceUser.java)
package com.example.quanlytoanha; // (Hoặc package của bạn)

import com.example.quanlytoanha.utils.DatabaseConnection; // (Giả sử bạn có file này)
import com.example.quanlytoanha.utils.PasswordUtil; // (File hash mật khẩu của bạn)

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class AddPoliceUser {

    public static void main(String[] args) {
        // --- CẤU HÌNH USER CÔNG AN ---
        String username = "congan"; // Tên đăng nhập
        String plainPassword = "congan123"; // Mật khẩu bạn sẽ dùng để đăng nhập
        String email = "congan@example.com"; // Email phải là DUY NHẤT
        String fullName = "Cán bộ Công an";
        int roleId = 3; // QUAN TRỌNG: role_id = 3 là "Công an"
        // ------------------------------------

        try {
            // 1. Băm mật khẩu
            String hashedPassword = PasswordUtil.hashPassword(plainPassword);
            System.out.println("Đã băm mật khẩu: " + plainPassword + " -> " + hashedPassword);

            // 2. Chuẩn bị câu lệnh SQL
            String sql = "INSERT INTO users (username, password, email, full_name, role_id, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)"; // (6 tham số)

            // 3. Kết nối và thực thi
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Gán 6 tham số
                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, email);
                pstmt.setString(4, fullName);
                pstmt.setInt(5, roleId);
                pstmt.setTimestamp(6, Timestamp.from(Instant.now()));

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("--- THÀNH CÔNG ---");
                    System.out.println("Đã thêm user Công an '" + username + "' vào database.");
                    System.out.println("Bạn có thể đăng nhập bằng mật khẩu: '" + plainPassword + "'");
                } else {
                    System.out.println("--- THẤT BẠI ---");
                    System.out.println("Không thêm được user.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Lỗi này thường xảy ra nếu username hoặc email đã tồn tại
            System.err.println("Lỗi khi thêm user: " + e.getMessage());
        }
    }
}