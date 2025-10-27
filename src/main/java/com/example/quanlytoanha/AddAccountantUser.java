// Vị trí: (Có thể chạy ở bất cứ đâu, ví dụ: com.example.quanlytoanha.utils.AddAccountantUser.java)
package com.example.quanlytoanha; // (Hoặc package của bạn)

import com.example.quanlytoanha.utils.DatabaseConnection; // (Giả sử bạn có file này)
import com.example.quanlytoanha.utils.PasswordUtil; // (File này ở ngay dưới)

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class AddAccountantUser {

    public static void main(String[] args) {
        // --- CẤU HÌNH USER KẾ TOÁN ---
        String username = "ketoan";
        String plainPassword = "ketoan123"; // Mật khẩu bạn sẽ dùng để đăng nhập
        String email = "ketoan@example.com";
        String fullName = "Nhân Viên Kế Toán";
        int roleId = 2; // QUAN TRỌNG: role_id = 2 là "Kế toán"
        // ------------------------------------

        try {
            // 1. Băm mật khẩu
            String hashedPassword = PasswordUtil.hashPassword(plainPassword);
            System.out.println("Đã băm mật khẩu: " + plainPassword + " -> " + hashedPassword);

            // 2. Chuẩn bị câu lệnh SQL (ĐÃ XÓA user_id)
            String sql = "INSERT INTO users (username, password, email, full_name, role_id, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)"; // (Chỉ còn 6 tham số)

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
                    System.out.println("Đã thêm user Kế toán '" + username + "' vào database.");
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
