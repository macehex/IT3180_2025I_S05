package com.example.quanlytoanha.controller;

// Đây là ví dụ mã trong Lớp Controller/Frame Đăng nhập của bạn
// (Giả sử bạn có 2 ô text: txtUsername và txtPassword)

import com.example.quanlytoanha.service.AuthService;
import com.example.quanlytoanha.session.SessionManager;
import com.example.quanlytoanha.model.User;

public class LoginController {

    private AuthService authService;
    // (Giả sử có @FXML private TextField txtUsername;)
    // (Giả sử có @FXML private PasswordField txtPassword;)

    public LoginController() {
        this.authService = new AuthService(); // Khởi tạo AuthService
    }

    /**
     * Phương thức này được gọi khi người dùng nhấn nút "Đăng nhập".
     */
    private void handleLoginButtonAction() {
        String username = "user_nhap_vao"; // Lấy từ txtUsername.getText();
        String password = "pass_nhap_vao"; // Lấy từ txtPassword.getText();

        // 1. Gọi AuthService để xác thực
        User user = authService.login(username, password);

        // 2. Kiểm tra kết quả
        if (user != null) {
            // ĐĂNG NHẬP THÀNH CÔNG!

            // 3. Lưu user vào SessionManager
            SessionManager.getInstance().login(user);

            // 4. Chuyển sang cửa sổ chính (Dashboard/Main)
            openMainWindow();

            // 5. Đóng cửa sổ đăng nhập hiện tại
            closeLoginWindow();

        } else {
            // ĐĂNG NHẬP THẤT BẠI
            // Hiển thị thông báo lỗi
            showErrorDialog("Lỗi", "Tên đăng nhập hoặc mật khẩu không đúng.");
        }
    }

    // (Các phương thức giả định)
    private void openMainWindow() {
        // Viết mã để mở cửa sổ chính (MainWindow) của bạn ở đây
        System.out.println("Đang mở cửa sổ chính...");
    }

    private void closeLoginWindow() {
        // Viết mã để đóng cửa sổ đăng nhập (LoginWindow)
        System.out.println("Đang đóng cửa sổ đăng nhập...");
    }

    private void showErrorDialog(String title, String message) {
        // Viết mã để hiển thị Alert/Dialog lỗi (ví dụ: JavaFX Alert)
        System.out.println(title + ": " + message);
    }
}
