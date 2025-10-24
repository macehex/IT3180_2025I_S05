// Vị trí: src/main/java/com/example/quanlytoanha/session/SessionManager.java
package com.example.quanlytoanha.session;

import com.example.quanlytoanha.model.User;

/**
 * Lớp Singleton để quản lý phiên đăng nhập của người dùng.
 * Lớp này sẽ giữ thông tin người dùng đang đăng nhập
 * và có thể được truy cập từ bất kỳ đâu trong ứng dụng.
 */
public class SessionManager {

    // 1. Thể hiện (instance) tĩnh và duy nhất của chính nó
    private static SessionManager instance;

    // 2. Thông tin người dùng đang đăng nhập
    private User currentUser;

    // 3. Constructor là private để ngăn chặn việc tạo đối tượng từ bên ngoài
    private SessionManager() {
        // Empty
    }

    /**
     * Phương thức tĩnh để lấy thể hiện duy nhất của SessionManager.
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Dùng để "đăng nhập" bằng cách lưu thông tin user.
     * Được gọi bởi lớp Controller/Window đăng nhập.
     * @param user Đối tượng User đã được xác thực
     */
    public void login(User user) {
        this.currentUser = user;
    }

    /**
     * Dùng để "đăng xuất" bằng cách xóa thông tin user.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Lấy thông tin của người dùng đang đăng nhập.
     * @return Đối tượng User, hoặc null nếu chưa đăng nhập.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Kiểm tra nhanh xem đã có ai đăng nhập hay chưa.
     * @return true nếu đã đăng nhập, false nếu chưa.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}