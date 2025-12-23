package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Role;
import com.example.quanlytoanha.service.AuthService;
import com.example.quanlytoanha.session.SessionManager;
import com.example.quanlytoanha.model.User;

// --- Thêm các import của JavaFX ---
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtPasswordVisible;
    @FXML
    private Button btnShowPassword;
    @FXML
    private Button loginButton;
    @FXML
    private Text errorText;

    private AuthService authService;
    private boolean isPasswordVisible = false;

    public LoginController() {
        this.authService = new AuthService(); // Khởi tạo AuthService
    }

    /**
     * Phương thức này được tự động gọi sau khi FXML được tải.
     */
    @FXML
    public void initialize() {
        // Setup password visibility toggle
        if (btnShowPassword != null && txtPasswordVisible != null) {
            btnShowPassword.setOnAction(e -> togglePasswordVisibility());
            
            // Sync text between both fields
            txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {
                txtPasswordVisible.setText(newVal);
            });
            txtPasswordVisible.textProperty().addListener((obs, oldVal, newVal) -> {
                txtPassword.setText(newVal);
            });
        }
        
        // Có thể thêm logic khởi tạo ở đây (ví dụ: đặt giá trị mặc định)
        // txtUsername.setText("admin"); // (Để test cho nhanh)
        // txtPassword.setText("admin123"); // (Để test cho nhanh)
    }
    
    /**
     * Toggle password visibility
     */
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        
        if (isPasswordVisible) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPassword.setVisible(false);
            btnShowPassword.setText("🙈");
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPasswordVisible.setVisible(false);
            btnShowPassword.setText("👁️");
        }
    }

    /**
     * Phương thức này được gọi khi người dùng nhấn nút "Đăng nhập".
     * (Tên hàm phải khớp với onAction="#handleLoginButtonAction" trong FXML)
     */
    @FXML
    private void handleLoginButtonAction() {
        // Lấy dữ liệu từ GIAO DIỆN
        String username = txtUsername.getText();
        String password = isPasswordVisible ? txtPasswordVisible.getText() : txtPassword.getText();

        // 1. Kiểm tra rỗng
        if (username.isEmpty() || password.isEmpty()) {
            showError("Tên đăng nhập và mật khẩu không được để trống.");
            return;
        }

        // 2. Gọi AuthService để xác thực
        User user = authService.login(username, password);

        // 3. Kiểm tra kết quả
        if (user != null) {
            // ĐĂNG NHẬP THÀNH CÔNG!
            errorText.setText(""); // Xóa thông báo lỗi (nếu có)

            // 3. Lưu user vào SessionManager
            SessionManager.getInstance().login(user);

            // 4. Lấy cửa sổ (Stage) hiện tại
            Stage loginStage = (Stage) loginButton.getScene().getWindow();

            // 5. Mở cửa sổ chính (và đóng cửa sổ login)
            openMainWindow(user, loginStage);

        } else {
            // ĐĂNG NHẬP THẤT BẠI
            // Hiển thị thông báo lỗi
            showError("Tên đăng nhập hoặc mật khẩu không đúng.");
        }
    }

    /**
     * Mở cửa sổ Dashboard tương ứng với vai trò và đóng cửa sổ Login.
     */
    private void openMainWindow(User user, Stage loginStage) {
        try {
            // 1. Đóng cửa sổ login
            loginStage.close();

            // 2. Quyết định file FXML dựa trên vai trò (Role)
            String fxmlFile;
            if (user.getRole() == Role.ADMIN) {
                fxmlFile = "/com/example/quanlytoanha/view/admin_dashboard.fxml";
            } else if (user.getRole() == Role.RESIDENT) {
                fxmlFile = "/com/example/quanlytoanha/view/resident_dashboard.fxml";
            } else if (user.getRole() == Role.ACCOUNTANT) {
                fxmlFile = "/com/example/quanlytoanha/view/accountant_dashboard.fxml";
            }
            else if (user.getRole() == Role.POLICE) {
                fxmlFile = "/com/example/quanlytoanha/view/police_dashboard.fxml";
                // (Thêm CSS cho police nếu có)
            }
            else {
                // (Bạn có thể thêm case cho Kế toán, Công an...)
                // Fallback (dự phòng)
                fxmlFile = "/com/example/quanlytoanha/view/admin_dashboard.fxml";
            }

            // 3. Tải FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // 4. Load CSS stylesheet tương ứng với role
            Scene scene = new Scene(root, 800, 600);
            if (user.getRole() == Role.ADMIN) {
                scene.getStylesheets().add(getClass().getResource("/com/example/quanlytoanha/view/styles/admin-styles.css").toExternalForm());
            } else if (user.getRole() == Role.ACCOUNTANT) {
                scene.getStylesheets().add(getClass().getResource("/com/example/quanlytoanha/view/styles/accountant_styles.css").toExternalForm());
            }

            // 5. Hiển thị cửa sổ mới
            Stage mainStage = new Stage();
            mainStage.setTitle("Dashboard - " + user.getFullName());
            mainStage.setScene(scene); // Kích thước lớn hơn để hiển thị đầy đủ
            mainStage.setResizable(true); // Cho phép resize
            mainStage.setMaximized(true); // Set full screen
            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Không thể tải giao diện chính.");
        }
    }

    /**
     * Hiển thị thông báo lỗi trên giao diện.
     */
    private void showError(String message) {
        errorText.setText(message);
    }
}