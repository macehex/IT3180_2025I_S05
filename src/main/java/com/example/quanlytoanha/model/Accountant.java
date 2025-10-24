// Vị trí: src/main/java/com/example/quanlytoanha/model/Accountant.java
package com.example.quanlytoanha.model;

import java.sql.Timestamp;

public class Accountant extends User {

    public Accountant(int userId, String username, String email, String fullName, Role role, Timestamp createdAt, Timestamp lastLogin, String phoneNumber) {
        super(userId, username, email, fullName, role, createdAt, lastLogin, phoneNumber);
    }

    @Override
    public void displayDashboard() {
        System.out.println("--- Kế toán Dashboard ---");
        System.out.println("Xin chào, " + getFullName());
        System.out.println("1. Tạo hóa đơn");
        System.out.println("2. Xem lịch sử giao dịch");
        System.out.println("3. Thống kê công nợ");
    }

    // Phương thức riêng
    public void generateInvoices() {
        System.out.println("Đang tạo hóa đơn hàng loạt...");
    }
}
