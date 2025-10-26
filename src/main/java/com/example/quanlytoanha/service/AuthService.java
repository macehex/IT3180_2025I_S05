// Vị trí: src/main/java/com/example/quanlytoanha/service/AuthService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.UserDAO;
import com.example.quanlytoanha.dao.PermissionDAO;
import com.example.quanlytoanha.model.Permission;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.utils.PasswordUtil; // Lớp tiện ích ta vừa tạo

import java.util.List;

/**
 * Lớp dịch vụ xử lý logic Đăng nhập, Đăng xuất và Quyền hạn.
 */
public class AuthService {

    private UserDAO userDAO;
    private PermissionDAO permissionDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
        this.permissionDAO = new PermissionDAO(); // Khởi tạo DAO
    }

    /**
     * Xử lý logic đăng nhập.
     * @param username Tên đăng nhập
     * @param plainPassword Mật khẩu thô (người dùng nhập)
     * @return Đối tượng User đầy đủ thông tin (bao gồm cả Quyền) nếu đăng nhập thành công,
     * ngược lại trả về null.
     */
    public User login(String username, String plainPassword) {
        // 1. Tìm user trong DB bằng username
        User user = userDAO.findUserByUsername(username);

        // 2. Nếu không tìm thấy user -> trả về null (lỗi sai tên đăng nhập)
        if (user == null) {
            System.out.println("Login thất bại: Không tìm thấy user " + username);
            return null;
        }

        // 3. Lấy mật khẩu đã băm từ đối tượng user (phải lấy từ DB)
        // (Giả sử đã thêm getPassword() vào lớp User)
        String hashedPasswordFromDB = user.getPassword(); // <-- Cần sửa User.java để có hàm này

        // 4. Kiểm tra mật khẩu
        if (PasswordUtil.checkPassword(plainPassword, hashedPasswordFromDB)) {
            // Mật khẩu ĐÚNG!
            System.out.println("Login thành công: " + user.getUsername());

            // 5. Tải quyền (permissions) cho user
            List<Permission> permissions = permissionDAO.getPermissionsByRoleId(user.getRole().getRoleId());
            user.setPermissions(permissions); // (Hàm này ta đã định nghĩa ở User.java)

            // 6. Cập nhật thời gian đăng nhập cuối cùng
            boolean lastLoginUpdated = UserDAO.updateLastLogin(user.getUserId());
            if (lastLoginUpdated) {
                // Cập nhật thời gian trong đối tượng user hiện tại
                user.setLastLogin(java.sql.Timestamp.from(java.time.Instant.now()));
                System.out.println("DEBUG: Đã cập nhật last_login cho user " + user.getUsername());
            } else {
                System.out.println("WARNING: Không thể cập nhật last_login cho user " + user.getUsername());
            }


            // === START DEBUG TẠM THỜI ===
            System.out.println("DEBUG: Role ID đang được nạp: " + user.getRole().getRoleId());
            System.out.println("DEBUG: Số lượng quyền được nạp: " + permissions.size());
            System.out.println("DEBUG: Danh sách quyền nạp: " + user.getPermissions()); // <-- Xem tập hợp (Set) được lưu
            System.out.println("DEBUG: Kiểm tra quyền 'CREATE_RESIDENT': " + user.hasPermission("CREATE_RESIDENT"));
            // === END DEBUG TẠM THỜI ===


            // 6. Trả về đối tượng User đầy đủ thông tin
            return user;
        } else {
            // Mật khẩu SAI!
            System.out.println("Login thất bại: Sai mật khẩu cho user " + username);
            return null;
        }
    }
}