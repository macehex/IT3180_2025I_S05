package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager;
import io.github.palexdev.materialfx.controls.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ResidentDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label pageTitle;

    @FXML
    private Label debtAmountLabel;

    @FXML
    private Label paidAmountLabel;

    @FXML
    private Label notificationCountLabel;

    @FXML
    private VBox contentContainer;

    @FXML
    private VBox dashboardContent;

    // MaterialFX components
    @FXML
    private MFXButton homeButton;

    @FXML
    private MFXButton invoiceHistoryButton;

    @FXML
    private MFXButton loginHistoryButton;

    @FXML
    private MFXButton notificationButton;

    @FXML
    private MFXButton btnLogout;

    // Track current active button for visual feedback
    private MFXButton currentActiveButton = null;

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

        // Show dashboard content by default
        showDashboardContent();
    }

    private void setupButtonHoverEffects() {
        // Home Button hover effect
        homeButton.setOnMouseEntered(e -> {
            if (currentActiveButton != homeButton) {
                homeButton.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
            }
        });
        homeButton.setOnMouseExited(e -> {
            if (currentActiveButton != homeButton) {
                homeButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
            }
        });

        // Invoice History Button hover effect
        invoiceHistoryButton.setOnMouseEntered(e -> {
            if (currentActiveButton != invoiceHistoryButton) {
                invoiceHistoryButton.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
            }
        });
        invoiceHistoryButton.setOnMouseExited(e -> {
            if (currentActiveButton != invoiceHistoryButton) {
                invoiceHistoryButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
            }
        });

        // Login History Button hover effect
        loginHistoryButton.setOnMouseEntered(e -> {
            if (currentActiveButton != loginHistoryButton) {
                loginHistoryButton.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
            }
        });
        loginHistoryButton.setOnMouseExited(e -> {
            if (currentActiveButton != loginHistoryButton) {
                loginHistoryButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
            }
        });

        // Notification Button hover effect
        notificationButton.setOnMouseEntered(e -> {
            if (currentActiveButton != notificationButton) {
                notificationButton.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
            }
        });
        notificationButton.setOnMouseExited(e -> {
            if (currentActiveButton != notificationButton) {
                notificationButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
            }
        });

        // Logout Button hover effect
        btnLogout.setOnMouseEntered(e ->
                btnLogout.setStyle("-fx-background-color: rgba(244,67,54,1.0); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 600; -fx-cursor: hand; -fx-font-family: 'DejaVu Sans';")
        );
        btnLogout.setOnMouseExited(e ->
                btnLogout.setStyle("-fx-background-color: rgba(244,67,54,0.9); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 600; -fx-font-family: 'DejaVu Sans';")
        );
    }

    private void loadDashboardData() {
        // Example: Load debt amount, paid amount, notification count
        // Replace with your actual data loading logic
        debtAmountLabel.setText("0 VND");
        paidAmountLabel.setText("0 VND");
        notificationCountLabel.setText("0");
    }

    /**
     * Show the default dashboard content
     */
    private void showDashboardContent() {
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(dashboardContent);
        pageTitle.setText("Trang chủ");
        setActiveButton(homeButton);
    }

    /**
     * Load content from an FXML file and display it in the content container
     */
    private void loadContentFromFxml(String fxmlPath, String title, MFXButton activeButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(content);
            pageTitle.setText(title);
            setActiveButton(activeButton);
            
        } catch (IOException e) {
            e.printStackTrace();
            // On error, show dashboard content
            showDashboardContent();
        }
    }

    /**
     * Set visual feedback for active button
     */
    private void setActiveButton(MFXButton activeButton) {
        // Reset previous active button
        if (currentActiveButton != null) {
            currentActiveButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
        }

        // Set new active button
        currentActiveButton = activeButton;
        if (currentActiveButton != null) {
            currentActiveButton.setStyle("-fx-background-color: rgba(255,255,255,0.35); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 600; -fx-font-family: 'DejaVu Sans'; -fx-border-color: rgba(255,255,255,0.5); -fx-border-width: 2; -fx-border-radius: 12;");
        }
    }

    @FXML
    private void handleHomeButton() {
        showDashboardContent();
    }

    @FXML
    private void handleInvoiceHistoryButton() {
        loadContentFromFxml("/com/example/quanlytoanha/view/invoice_history_embedded.fxml", 
                           "Hóa đơn và Lịch sử giao dịch", 
                           invoiceHistoryButton);
    }

    @FXML
    private void handleLoginHistoryButton() {
        loadContentFromFxml("/com/example/quanlytoanha/view/login_management_embedded.fxml", 
                           "Quản lý đăng nhập", 
                           loginHistoryButton);
    }

    @FXML
    private void handleNotificationButton() {
        loadContentFromFxml("/com/example/quanlytoanha/view/notification_view_embedded.fxml", 
                           "Thông báo", 
                           notificationButton);
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