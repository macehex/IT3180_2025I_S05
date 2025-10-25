// Vị trí: src/main/java/com/example/quanlytoanha/Main.java
package com.example.quanlytoanha;

import com.example.quanlytoanha.model.Role;
import com.example.quanlytoanha.model.User;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Stage primaryStage; // Biến static để lưu trữ Stage chính

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage; // Lưu lại Stage chính
        showLoginScene(); // Bắt đầu bằng việc hiển thị màn hình Login
    }

    /**
     * Phương thức này hiển thị màn hình Đăng nhập.
     * Sẽ được gọi khi bắt đầu app và khi Đăng xuất.
     */
    public static void showLoginScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Quản lý Tòa nhà - Đăng nhập");
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();
    }

    /**
     * Phương thức này hiển thị màn hình Dashboard.
     * Sẽ được gọi khi Đăng nhập thành công.
     */
    public static void showDashboardScene(User user) throws IOException {
        // 1. Quyết định file FXML dựa trên vai trò
        String fxmlFile;
        if (user.getRole() == Role.ADMIN) {
            fxmlFile = "/com/example/quanlytoanha/view/admin_dashboard.fxml";
        } else if (user.getRole() == Role.RESIDENT) {
            fxmlFile = "/com/example/quanlytoanha/view/resident_dashboard.fxml";
        } else {
            // Fallback (dự phòng)
            fxmlFile = "/com/example/quanlytoanha/view/admin_dashboard.fxml";
        }

        // 2. Tải FXML
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlFile));
        Parent root = loader.load();

        // 3. Thay đổi Scene trên Stage chính
        primaryStage.setTitle("Dashboard - " + user.getFullName());
        primaryStage.setScene(new Scene(root)); // <-- Chỉ cần thay Scene!
        primaryStage.centerOnScreen(); // (Tùy chọn: căn giữa cửa sổ)
    }
}