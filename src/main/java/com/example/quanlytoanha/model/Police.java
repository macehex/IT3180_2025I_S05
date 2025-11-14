package com.example.quanlytoanha.model;

import java.sql.Timestamp;

/**
 * Đại diện cho người dùng có vai trò Công an (Role.POLICE).
 */
public class Police extends User {

    // Constructor để UserDAO có thể tạo đối tượng
    public Police(int userId, String username, String email, String fullName, Role role, Timestamp createdAt, Timestamp lastLogin, String phoneNumber) {
        // Gọi constructor của lớp cha (User)
        super(userId, username, email, fullName, role, createdAt, lastLogin, phoneNumber);
    }

    /**
     * Định nghĩa phương thức abstract từ lớp cha
     * (Hiển thị các chức năng của Công an)
     */
    @Override
    public void displayDashboard() {
        System.out.println("--- Cổng Truy cập Dữ liệu An ninh ---");
        System.out.println("Xin chào, " + getFullName());
        System.out.println("1. Truy xuất Báo cáo Dân cư");
        System.out.println("2. Truy xuất Lịch sử Ra/Vào");
    }

    // (Trong tương lai, bạn có thể thêm các phương thức nghiệp vụ riêng cho Công an ở đây)
    public void exportSecurityReport() {
        System.out.println("Đang truy xuất và xuất báo cáo an ninh...");
    }
}