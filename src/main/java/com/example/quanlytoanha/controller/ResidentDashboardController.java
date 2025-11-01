package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager;
import io.github.palexdev.materialfx.controls.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ResidentDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label debtAmountLabel;

    @FXML
    private Label paidAmountLabel;

    @FXML
    private Label notificationCountLabel;

    // MaterialFX components
    @FXML
    private MFXButton invoiceHistoryButton;

    @FXML
    private MFXButton loginHistoryButton;

    @FXML
    private MFXButton notificationButton;

    @FXML
    private MFXButton btnLogout;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Xin chào, " + currentUser.getUsername());
        }

        // Setup hover effects for MaterialFX components
        setupButtonHoverEffects();

        // Initialize dashboard data (you can add your own logic here)
        loadDashboardData();
    }

    private void setupButtonHoverEffects() {
        // Invoice History Button hover effect
        invoiceHistoryButton.setOnMouseEntered(e ->
                invoiceHistoryButton.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 500;")
        );
        invoiceHistoryButton.setOnMouseExited(e ->
                invoiceHistoryButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 500;")
        );

        // Login History Button hover effect
        loginHistoryButton.setOnMouseEntered(e ->
                loginHistoryButton.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 500;")
        );
        loginHistoryButton.setOnMouseExited(e ->
                loginHistoryButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 500;")
        );

        // Notification Button hover effect
        notificationButton.setOnMouseEntered(e ->
                notificationButton.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 500;")
        );
        notificationButton.setOnMouseExited(e ->
                notificationButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 500;")
        );

        // Logout Button hover effect
        btnLogout.setOnMouseEntered(e ->
                btnLogout.setStyle("-fx-background-color: rgba(244,67,54,1.0); -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 600; -fx-cursor: hand;")
        );
        btnLogout.setOnMouseExited(e ->
                btnLogout.setStyle("-fx-background-color: rgba(244,67,54,0.9); -fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 600;")
        );
    }

    private void loadDashboardData() {
        // Example: Load debt amount, paid amount, notification count
        // Replace with your actual data loading logic
        debtAmountLabel.setText("0 VND");
        paidAmountLabel.setText("0 VND");
        notificationCountLabel.setText("0");
    }

    @FXML
    private void handleInvoiceHistoryButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/invoice_history.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Hóa đơn và Lịch sử giao dịch");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Optionally, show an error alert to the user
        }
    }

    @FXML
    private void handleLoginHistoryButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/login_management.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Quản lý đăng nhập");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Optionally, show an error alert to the user
        }
    }

    @FXML
    private void handleNotificationButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/notification_view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Thông Báo");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(notificationButton.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Xử lý sự kiện đăng xuất.
     */
    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage currentStage = (Stage) btnLogout.getScene().getWindow();
        currentStage.close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Quản lý Tòa nhà - Đăng nhập");
            loginStage.setScene(new Scene(root, 400, 300));
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}