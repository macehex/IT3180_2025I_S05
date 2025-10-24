// Vị trí: src/main/java/com/example/quanlytoanha/dao/PermissionDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Permission;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO (Data Access Object) cho đối tượng Permission.
 * Chịu trách nhiệm truy vấn và thao tác với bảng 'permission'
 * và bảng 'role_permissions'.
 */
public class PermissionDAO {
    /**
     * Lấy tất cả các quyền (Permissions) mà một vai trò (Role) sở hữu.
     * Đây là phương thức cốt lõi được gọi khi người dùng đăng nhập.
     * * @param roleId ID của vai trò (ví dụ: 1 cho ADMIN, 4 cho RESIDENT)
     * @return một Danh sách (List) các đối tượng Permission
     */
    public List<Permission> getPermissionsByRoleId(int roleId) {
        List<Permission> permissions = new ArrayList<>();

        // Câu lệnh SQL này JOIN 3 bảng:
        // 1. Lấy permission_id từ 'role_permissions' dựa trên role_id
        // 2. Lấy thông tin (tên) của permission từ bảng 'permission'
        String sql = "SELECT p.permission_id, p.permission_name " +
                "FROM permission p " +
                "JOIN role_permissions rp ON p.permission_id = rp.permission_id " +
                "WHERE rp.role_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Gán roleId vào câu lệnh SQL (dấu ? thứ 1)
            pstmt.setInt(1, roleId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Lặp qua tất cả các dòng kết quả
                while (rs.next()) {
                    int permissionId = rs.getInt("permission_id");
                    String permissionName = rs.getString("permission_name");

                    // Tạo đối tượng Permission và thêm vào danh sách
                    permissions.add(new Permission(permissionId, permissionName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Xử lý lỗi (ví dụ: log lỗi)
        }

        return permissions;
    }

    /**
     * Lấy tất cả các quyền có trong hệ thống.
     * (Hữu ích cho các chức năng admin, ví dụ: "gán quyền cho vai trò")
     * * @return một Danh sách (List) tất cả các đối tượng Permission
     */
    public List<Permission> getAllPermissions() {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT * FROM permission";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int permissionId = rs.getInt("permission_id");
                String permissionName = rs.getString("permission_name");
                permissions.add(new Permission(permissionId, permissionName));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return permissions;
    }

    // Bạn có thể thêm các phương thức khác ở đây nếu cần, ví dụ:
    // - createPermission(Permission permission)
    // - deletePermission(int permissionId)
    // - addPermissionToRole(int roleId, int permissionId)
    // - removePermissionFromRole(int roleId, int permissionId)
}