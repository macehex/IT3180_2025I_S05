package com.example.quanlytoanha;

import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Script để khởi tạo các quyền (Permissions) cơ bản vào database.
 * Chạy file này một lần để tạo các quyền cần thiết cho hệ thống.
 */
public class InitializePermissions {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Đang khởi tạo quyền (Permissions)...");
        System.out.println("========================================");

        // Danh sách các quyền cơ bản
        String[] permissions = {
            // Quản lý Cư dân
            "CREATE_RESIDENT",           // Tạo cư dân mới
            "UPDATE_RESIDENT",           // Sửa thông tin cư dân
            "DELETE_RESIDENT",           // Xóa cư dân
            "VIEW_RESIDENT",             // Xem thông tin cư dân
            
            // Quản lý Hóa đơn
            "CREATE_INVOICE",            // Tạo hóa đơn
            "UPDATE_INVOICE",            // Sửa hóa đơn
            "DELETE_INVOICE",            // Xóa hóa đơn
            "VIEW_INVOICE",              // Xem hóa đơn
            
            // Quản lý Tài khoản
            "CREATE_USER",               // Tạo tài khoản người dùng
            "UPDATE_USER",               // Sửa tài khoản người dùng
            "DELETE_USER",               // Xóa tài khoản người dùng
            "VIEW_USER",                 // Xem thông tin tài khoản
            "RESET_PASSWORD",            // Reset mật khẩu
            
            // Quản lý Phân quyền
            "MANAGE_PERMISSIONS",        // Quản lý phân quyền
            
            // Quản lý Tài sản
            "CREATE_ASSET",              // Tạo tài sản mới
            "UPDATE_ASSET",              // Sửa thông tin tài sản
            "DELETE_ASSET",              // Xóa tài sản
            "VIEW_ASSET",                // Xem thông tin tài sản
            
            // Quản lý Bảo trì
            "CREATE_MAINTENANCE",        // Tạo lịch sử bảo trì
            "UPDATE_MAINTENANCE",        // Sửa lịch sử bảo trì
            "VIEW_MAINTENANCE",          // Xem lịch sử bảo trì
            
            // Quản lý Thông báo
            "CREATE_ANNOUNCEMENT",       // Tạo thông báo
            "VIEW_ANNOUNCEMENT",         // Xem thông báo
            
            // Quản lý Yêu cầu Dịch vụ
            "CREATE_SERVICE_REQUEST",    // Tạo yêu cầu dịch vụ
            "UPDATE_SERVICE_REQUEST",    // Cập nhật yêu cầu dịch vụ
            "VIEW_SERVICE_REQUEST",      // Xem yêu cầu dịch vụ
            
            // Báo cáo
            "VIEW_REPORT",               // Xem báo cáo
            "EXPORT_REPORT",             // Xuất báo cáo
            
            // Thanh toán
            "MAKE_PAYMENT",              // Thanh toán hóa đơn
            "VIEW_PAYMENT",              // Xem lịch sử thanh toán
            
            // Quyền cơ bản
            "READ_PROFILE",              // Đọc thông tin cá nhân
            "UPDATE_PROFILE",            // Cập nhật thông tin cá nhân
            "VIEW_DASHBOARD"             // Xem dashboard
        };

        try (Connection conn = DatabaseConnection.getConnection()) {
            int successCount = 0;
            int skipCount = 0;

            for (String permissionName : permissions) {
                if (permissionExists(conn, permissionName)) {
                    System.out.println("⏭️  Bỏ qua: " + permissionName + " (đã tồn tại)");
                    skipCount++;
                } else {
                    if (insertPermission(conn, permissionName)) {
                        System.out.println("✅ Đã thêm: " + permissionName);
                        successCount++;
                    } else {
                        System.out.println("❌ Lỗi khi thêm: " + permissionName);
                    }
                }
            }

            System.out.println("========================================");
            System.out.println("✅ HOÀN TẤT!");
            System.out.println("========================================");
            System.out.println("Đã thêm: " + successCount + " quyền mới");
            System.out.println("Đã bỏ qua: " + skipCount + " quyền (đã tồn tại)");
            System.out.println("Tổng cộng: " + permissions.length + " quyền");
            System.out.println("========================================");
            System.out.println("Bây giờ bạn có thể sử dụng tab 'Quản lý Phân quyền' để gán quyền cho các vai trò.");

        } catch (SQLException e) {
            System.err.println("========================================");
            System.err.println("❌ LỖI!");
            System.err.println("========================================");
            System.err.println("Đã xảy ra lỗi khi khởi tạo quyền:");
            e.printStackTrace();
            System.err.println("========================================");
        }
    }

    /**
     * Kiểm tra xem quyền đã tồn tại chưa.
     */
    private static boolean permissionExists(Connection conn, String permissionName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM permission WHERE permission_name = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, permissionName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Thêm một quyền vào database.
     */
    private static boolean insertPermission(Connection conn, String permissionName) throws SQLException {
        String sql = "INSERT INTO permission (permission_name) VALUES (?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, permissionName);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}

