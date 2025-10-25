package com.example.quanlytoanha;

import com.example.quanlytoanha.dao.UserDAO;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.utils.PasswordUtil;

public class TestLogin {

    public static void main(String[] args) {
        try {
            UserDAO userDAO = new UserDAO();
            User admin = userDAO.findUserByUsername("admin");
            
            if (admin != null) {
                System.out.println("Tìm thấy user admin:");
                System.out.println("- Username: " + admin.getUsername());
                System.out.println("- Password hash: " + admin.getPassword());
                System.out.println("- Role: " + admin.getRole());
                
                // Test một số mật khẩu phổ biến
                String[] commonPasswords = {"admin", "123456", "password", "admin123", "123", "toanha", "quanly", "12345", "abc123"};
                
                System.out.println("\nKiểm tra các mật khẩu phổ biến:");
                for (String password : commonPasswords) {
                    boolean isMatch = PasswordUtil.checkPassword(password, admin.getPassword());
                    System.out.println("- '" + password + "': " + (isMatch ? "✓ ĐÚNG" : "✗ SAI"));
                    if (isMatch) {
                        System.out.println("*** MẬT KHẨU ĐÚNG LÀ: " + password + " ***");
                        break;
                    }
                }
            } else {
                System.out.println("Không tìm thấy user admin trong database!");
            }
            
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}