package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.PermissionDAO;
import com.example.quanlytoanha.dao.UserDAO;
import com.example.quanlytoanha.model.Permission;
import com.example.quanlytoanha.model.Role;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Service xử lý business logic cho quản lý tài khoản người dùng và phân quyền.
 */
public class UserAccountService {

    private final UserDAO userDAO;
    private final PermissionDAO permissionDAO;

    public UserAccountService() {
        this.userDAO = new UserDAO();
        this.permissionDAO = new PermissionDAO();
    }

    /**
     * Kiểm tra quyền: Chỉ Admin mới được quản lý tài khoản.
     */
    private void checkPermission() throws SecurityException {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            throw new SecurityException("Chỉ Quản trị viên mới có quyền thực hiện thao tác này.");
        }
    }

    /**
     * Lấy tất cả người dùng trong hệ thống.
     */
    public List<User> getAllUsers() throws SQLException, SecurityException {
        checkPermission();
        return userDAO.getAllUsers();
    }

    /**
     * Tạo tài khoản người dùng mới.
     */
    public int createUser(String username, String plainPassword, String email, String fullName,
                          String phoneNumber, Role role) throws SQLException, SecurityException, IllegalArgumentException {
        checkPermission();

        // Validation
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống.");
        }
        if (role == Role.RESIDENT) {
            throw new IllegalArgumentException("Không thể tạo tài khoản cư dân từ đây. Vui lòng sử dụng chức năng quản lý cư dân.");
        }

        // Kiểm tra username đã tồn tại chưa
        if (userDAO.isUsernameExists(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        }

        // Kiểm tra email đã tồn tại chưa (nếu email không null)
        if (email != null && !email.trim().isEmpty() && userDAO.isEmailExists(email)) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        return userDAO.createUser(username, plainPassword, email, fullName, phoneNumber, role);
    }

    /**
     * Cập nhật thông tin tài khoản người dùng.
     */
    public boolean updateUser(int userId, String email, String fullName, String phoneNumber, Role role)
            throws SQLException, SecurityException, IllegalArgumentException {
        checkPermission();

        // Validation
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống.");
        }

        // Kiểm tra email đã tồn tại chưa (trừ chính user đó)
        User existingUser = userDAO.getUserById(userId);
        if (existingUser == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng.");
        }
        if (!existingUser.getEmail().equals(email) && userDAO.isEmailExists(email)) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        return userDAO.updateUser(userId, email, fullName, phoneNumber, role);
    }

    /**
     * Reset mật khẩu của người dùng.
     */
    public boolean resetUserPassword(int userId, String newPlainPassword)
            throws SQLException, SecurityException, IllegalArgumentException {
        checkPermission();

        if (newPlainPassword == null || newPlainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu mới không được để trống.");
        }

        return userDAO.updateUserPasswordFromPlain(userId, newPlainPassword);
    }

    /**
     * Xóa tài khoản người dùng.
     */
    public boolean deleteUser(int userId) throws SQLException, SecurityException {
        checkPermission();

        // Không cho phép xóa chính mình
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUserId() == userId) {
            throw new SecurityException("Bạn không thể xóa chính tài khoản của mình.");
        }

        return userDAO.deleteUser(userId);
    }

    /**
     * Xóa tài khoản cư dân sau khi đã xóa hồ sơ cư dân.
     */
    public boolean deleteUserForce(int userId) throws SQLException, SecurityException {
        checkPermission();

        // Không cho phép xóa chính mình
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUserId() == userId) {
            throw new SecurityException("Bạn không thể xóa chính tài khoản của mình.");
        }

        return userDAO.deleteUserForce(userId);
    }

    /**
     * Lấy tất cả quyền trong hệ thống.
     */
    public List<Permission> getAllPermissions() throws SQLException, SecurityException {
        checkPermission();
        return permissionDAO.getAllPermissions();
    }

    /**
     * Lấy danh sách quyền của một vai trò.
     */
    public List<Permission> getPermissionsByRole(Role role) throws SQLException, SecurityException {
        checkPermission();
        return permissionDAO.getPermissionsByRoleId(role.getRoleId());
    }

    /**
     * Cập nhật danh sách quyền cho một vai trò.
     */
    public boolean updateRolePermissions(Role role, List<Integer> permissionIds)
            throws SQLException, SecurityException {
        checkPermission();
        return permissionDAO.updateRolePermissions(role.getRoleId(), permissionIds);
    }

    /**
     * Thêm quyền cho một vai trò.
     */
    public boolean addPermissionToRole(Role role, int permissionId) throws SQLException, SecurityException {
        checkPermission();
        return permissionDAO.addPermissionToRole(role.getRoleId(), permissionId);
    }

    /**
     * Xóa quyền khỏi một vai trò.
     */
    public boolean removePermissionFromRole(Role role, int permissionId) throws SQLException, SecurityException {
        checkPermission();
        return permissionDAO.removePermissionFromRole(role.getRoleId(), permissionId);
    }

    // Trong UserAccountService.java
    public List<User> getUsersByPage(int limit, int offset) throws SQLException {
        return userDAO.getUsersByPage(limit, offset);
    }

    public int countTotalUsers() throws SQLException {
        return userDAO.countTotalUsers();
    }
}

