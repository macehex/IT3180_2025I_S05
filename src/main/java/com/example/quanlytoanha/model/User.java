// Vị trí: src/main/java/com/example/quanlytoanha/model/User.java
package com.example.quanlytoanha.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lớp cơ sở (abstract) cho tất cả người dùng trong hệ thống.
 * Chứa các thông tin chung từ bảng 'users'.
 */
public abstract class User {

    // --- Fields ---
    private int userId;
    private String username;
    private String password; // Mật khẩu đã băm (hashed)
    private String email;
    private String phoneNumber;
    private String fullName;
    private Role role; // Sử dụng Enum
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private Set<String> permissions; // Danh sách quyền

    public User() {
    }

    // --- Constructor ---
    public User(int userId, String username, String email, String fullName, Role role, Timestamp createdAt, Timestamp lastLogin, String phoneNumber) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.phoneNumber = phoneNumber;
        // Lưu ý: 'password' và 'permissions' sẽ được nạp sau khi đối tượng được tạo
        // (thông qua setters) vì chúng ta không muốn truyền mật khẩu trong constructor.
    }

    // --- Phương thức chung ---
    public void viewProfile() {
        System.out.println("Viewing profile for: " + this.fullName);
        System.out.println("Role: " + this.role.getRoleName());
    }

    /*
    public void updateProfile(String newEmail, String newPhone) {
        this.email = newEmail;
        this.phoneNumber = newPhone;
        System.out.println("Profile updated.");
        // (Sau đó bạn sẽ cần gọi một lớp Service/DAO để lưu vào DB)
    }
    */

    // --- Phương thức trừu tượng ---
    /**
     * Mỗi vai trò (Role) sẽ có một trang tổng quan (dashboard) hoặc menu khác nhau.
     * Lớp con BẮT BUỘC phải định nghĩa (implement) phương thức này.
     */
    public abstract void displayDashboard();

    // --- Quản lý Quyền (Permissions) ---
    /**
     * Nạp quyền cho User (thường gọi sau khi login thành công).
     */
    public void setPermissions(List<Permission> permissionList) {
        if (permissionList != null) {
            this.permissions = permissionList.stream()
                    .map(Permission::getPermissionName)
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Kiểm tra xem User có một quyền cụ thể hay không.
     * @param permissionName Tên quyền cần kiểm tra (ví dụ: "CREATE_INVOICE")
     * @return true nếu có quyền, false nếu không
     */
    public boolean hasPermission(String permissionName) {
        if (this.permissions == null) {
            return false; // Chưa nạp quyền
        }
        return this.permissions.contains(permissionName);
    }

    // --- Getters and Setters (Đầy đủ) ---

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Lấy mật khẩu đã băm (dùng để so sánh khi login).
     */
    public String getPassword() {
        return password;
    }

    /**
     * Nạp mật khẩu đã băm từ DB vào đối tượng User.
     * (Sẽ được gọi trong UserDAO).
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * Lấy tập hợp (Set) các tên quyền của người dùng.
     */
    public Set<String> getPermissions() {
        return permissions;
    }

    // Phương thức setPermissions(List<Permission> permissionList) đã được định nghĩa ở trên.
}