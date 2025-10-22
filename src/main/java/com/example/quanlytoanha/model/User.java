package com.example.quanlytoanha.model;
import java.sql.Timestamp;
import java.util.List; // Import
import java.util.Set;
import java.util.stream.Collectors;

public abstract class User {
    private int userId;
    private String username;
    private String password; // Thường thì không nên lưu password ở đây, nhưng để map với DB
    private String email;
    private String phoneNumber;
    private String fullName;
    private Role role; // Sử dụng Enum thay vì int
    private Timestamp createdAt;
    private Timestamp lastLogin;

    // THÊM TRƯỜNG MỚI:
    // Dùng Set<String> để lưu tên các quyền (ví dụ: "CREATE_INVOICE", "VIEW_ASSETS")
    private Set<String> permissions;

    public User(int userId, String username, String email, String fullName, Role role, Timestamp createdAt, Timestamp lastLogin, String phoneNumber) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.phoneNumber = phoneNumber;
    }

    // Mọi người dùng đều có thể xem thông tin cá nhân
    public void viewProfile() {
        System.out.println("Viewing profile for: " + this.fullName);
        System.out.println("Role: " + this.role.getRoleName());

    }

    // Mọi người dùng đều có thể cập nhật thông tin (ví dụ)

    /*public void updateProfile(String newEmail, String newPhone) {
        this.email = newEmail;
        this.phoneNumber = newPhone;
        System.out.println("Profile updated.");
        // (Sau đó bạn sẽ cần gọi một lớp Service/DAO để lưu vào DB)
    }
    */

    // --- PHƯƠNG THỨC TRỪU TƯỢNG (ABSTRACT) ---
    /**
     * Mỗi vai trò (Role) sẽ có một trang tổng quan (dashboard) hoặc menu khác nhau.
     * Lớp con BẮT BUỘC phải định nghĩa (implement) phương thức này.
     */
    public abstract void displayDashboard();

    // --- QUẢN LÝ QUYỀN ---

    /**
     * Dùng để "nạp" quyền cho User sau khi lấy từ DB (bảng role_permissions)
     * Giả sử Permission là một class bạn tạo ra có getPermissionName()
     */
    public void setPermissions(List<Permission> permissionList) {
        if (permissionList != null) {
            this.permissions = permissionList.stream()
                    .map(Permission::getPermissionName)
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Phương thức cực kỳ hữu ích: Kiểm tra xem User này có quyền làm gì đó không.
     * @param permissionName Tên quyền cần kiểm tra (ví dụ: "CREATE_INVOICE")
     * @return true nếu có quyền, false nếu không
     */
    public boolean hasPermission(String permissionName) {
        if (this.permissions == null) {
            return false;
        }
        return this.permissions.contains(permissionName);
    }

    // --- Getters and Setters ---
    // (Thêm các getters/setters cần thiết cho các trường private)
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public String getPhoneNumber() {return phoneNumber;}
    // ... thêm các getters/setters khác
}