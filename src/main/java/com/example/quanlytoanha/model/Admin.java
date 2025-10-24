// Vị trí: src/main/java/com/example/quanlytoanha/model/Admin.java
package com.example.quanlytoanha.model;

import java.sql.Timestamp;

/**
 * Đại diện cho Ban quản trị.
 */
public class Admin extends User {

    // Constructor chỉ cần gọi constructor của lớp cha
    public Admin(int userId, String username, String email, String fullName, Role role, Timestamp createdAt, Timestamp lastLogin, String phoneNumber) {
        super(userId, username, email, fullName, role, createdAt, lastLogin, phoneNumber);
    }

    /**
     * Định nghĩa phương thức abstract từ lớp cha
     */
    @Override
    public void displayDashboard() {
        System.out.println("--- Admin Dashboard (Ban quản trị) ---");
        System.out.println("Xin chào, " + getFullName());
        System.out.println("1. Quản lý tài khoản người dùng");
        System.out.println("2. Tạo và gửi thông báo");
        System.out.println("3. Xem tất cả yêu cầu dịch vụ");
        System.out.println("4. Quản lý tài sản");
    }

    // Các phương thức riêng của Admin
    public void manageUserAccounts() {
        System.out.println("Đang quản lý tài khoản...");
    }

    public void createAnnouncement() {
        System.out.println("Đang tạo thông báo mới...");
    }
}
