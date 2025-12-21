package com.example.quanlytoanha;

import com.example.quanlytoanha.utils.DatabaseConnection;
import com.example.quanlytoanha.utils.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Script để reset mật khẩu cho một user cụ thể.
 * Chạy file này để reset mật khẩu nếu bạn quên hoặc mật khẩu bị lỗi.
 */
public class ResetPassword {

    public static void main(String[] args) {
        // ============================================
        // CẤU HÌNH: Sửa các thông tin dưới đây
        // ============================================
        String username = "admin"; // <-- SỬA: Tên đăng nhập của bạn
        String newPassword = "admin123"; // <-- SỬA: Mật khẩu mới bạn muốn đặt
        
        // ============================================
        // KHÔNG CẦN SỬA PHẦN DƯỚI
        // ============================================

        try {
            // 1. Băm mật khẩu mới
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            System.out.println("Đã băm mật khẩu mới.");

            // 2. Cập nhật mật khẩu trong database
            String sql = "UPDATE users SET password = ? WHERE username = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, hashedPassword);
                pstmt.setString(2, username);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("========================================");
                    System.out.println("✅ THÀNH CÔNG!");
                    System.out.println("========================================");
                    System.out.println("Đã reset mật khẩu cho user: " + username);
                    System.out.println("Mật khẩu mới: " + newPassword);
                    System.out.println("Bạn có thể đăng nhập với mật khẩu này ngay bây giờ.");
                    System.out.println("========================================");
                } else {
                    System.out.println("========================================");
                    System.out.println("❌ THẤT BẠI!");
                    System.out.println("========================================");
                    System.out.println("Không tìm thấy user với username: " + username);
                    System.out.println("Vui lòng kiểm tra lại username.");
                    System.out.println("========================================");
                }
            }

        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ LỖI!");
            System.err.println("========================================");
            System.err.println("Đã xảy ra lỗi khi reset mật khẩu:");
            e.printStackTrace();
            System.err.println("========================================");
        }
    }
}

