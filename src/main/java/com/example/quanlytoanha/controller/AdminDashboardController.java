package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller cho Admin Dashboard
 */
public class AdminDashboardController {
    
    @FXML private Label lblWelcome;
    @FXML private Button btnManageResidents;
    @FXML private Button btnManageUsers;
    @FXML private Button btnManageApartments;
    @FXML private Button btnAnnouncements;
    @FXML private Button btnLogout;
    
    @FXML
    public void initialize() {
        // Cập nhật thông tin người dùng
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblWelcome.setText("Xin chào, " + currentUser.getFullName());
        }
        
        // Tự động mở giao diện quản lý cư dân
        handleManageResidents();
    }
    
    /**
     * Xử lý quản lý cư dân
     */
    @FXML
    private void handleManageResidents() {
        try {
            // Tải giao diện quản lý cư dân đơn giản
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/resident_management_simple.fxml"));
            Parent root = loader.load();
            
            // Tạo cửa sổ mới
            Stage stage = new Stage();
            stage.setTitle("Quản lý cư dân - Hệ thống quản lý tòa nhà");
            stage.setScene(new Scene(root, 1000, 600));
            stage.setResizable(true);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải giao diện quản lý cư dân!\nLỗi: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Xử lý quản lý người dùng (placeholder)
     */
    @FXML
    private void handleManageUsers() {
        showAlert("Thông báo", "Chức năng quản lý người dùng sẽ được phát triển trong phiên bản tiếp theo!", Alert.AlertType.INFORMATION);
    }
    
    /**
     * Xử lý quản lý căn hộ (placeholder)
     */
    @FXML
    private void handleManageApartments() {
        showAlert("Thông báo", "Chức năng quản lý căn hộ sẽ được phát triển trong phiên bản tiếp theo!", Alert.AlertType.INFORMATION);
    }
    
    /**
     * Xử lý quản lý thông báo (placeholder)
     */
    @FXML
    private void handleAnnouncements() {
        showAlert("Thông báo", "Chức năng quản lý thông báo sẽ được phát triển trong phiên bản tiếp theo!", Alert.AlertType.INFORMATION);
    }
    
    /**
     * Xử lý đăng xuất
     */
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            SessionManager.getInstance().logout();
            
            try {
                // Quay về màn hình đăng nhập
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/login.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) btnLogout.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Đăng nhập - Hệ thống quản lý tòa nhà");
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể quay về màn hình đăng nhập!", Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Hiển thị thông báo
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
