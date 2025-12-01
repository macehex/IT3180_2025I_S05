// Vị trí: src/main/java/com/example/quanlytoanha/dao/PermissionDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Permission;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    // ==========================================================
    // QUẢN LÝ PHÂN QUYỀN - Các phương thức mới
    // ==========================================================

    /**
     * Thêm một quyền cho một vai trò.
     */
    public boolean addPermissionToRole(int roleId, int permissionId) throws SQLException {
        // Kiểm tra xem đã tồn tại chưa
        if (hasPermission(roleId, permissionId)) {
            return true; // Đã có rồi, không cần thêm
        }

        String sql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Xóa một quyền khỏi một vai trò.
     */
    public boolean removePermissionFromRole(int roleId, int permissionId) throws SQLException {
        String sql = "DELETE FROM role_permissions WHERE role_id = ? AND permission_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Kiểm tra xem một vai trò có một quyền cụ thể hay không.
     */
    public boolean hasPermission(int roleId, int permissionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM role_permissions WHERE role_id = ? AND permission_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            pstmt.setInt(2, permissionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Cập nhật danh sách quyền cho một vai trò (xóa tất cả và thêm lại).
     */
    public boolean updateRolePermissions(int roleId, List<Integer> permissionIds) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Xóa tất cả quyền hiện tại của role
            String deleteSql = "DELETE FROM role_permissions WHERE role_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, roleId);
                deleteStmt.executeUpdate();
            }

            // 2. Thêm lại các quyền mới
            if (permissionIds != null && !permissionIds.isEmpty()) {
                String insertSql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    for (Integer permissionId : permissionIds) {
                        insertStmt.setInt(1, roleId);
                        insertStmt.setInt(2, permissionId);
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}