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

    @FXML private Label welcomeLabel;
    @FXML private Label pageTitle;
    @FXML private Label debtAmountLabel;
    @FXML private Label paidAmountLabel;
    @FXML private Label notificationCountLabel;
    @FXML private VBox contentContainer;
    @FXML private VBox dashboardContent;

    // --- CÁC NÚT MENU (MaterialFX) ---
    @FXML private MFXButton homeButton;
    @FXML private MFXButton invoiceHistoryButton;
    @FXML private MFXButton loginHistoryButton;
    @FXML private MFXButton notificationButton;
    @FXML private MFXButton viewMyRequestsButton;

    // MỚI: Thêm khai báo cho nút Tạo Phản Ánh (Lỗi cũ do thiếu dòng này)
    @FXML private MFXButton createRequestButton;

    @FXML private MFXButton btnLogout;

    // Biến theo dõi nút đang được chọn
    private MFXButton currentActiveButton = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Xin chào, " + currentUser.getUsername());
        }

        // Thiết lập hiệu ứng hover cho tất cả các nút
        setupButtonHoverEffects();

        loadDashboardData();
        showDashboardContent();
    }

    /**
     * CẬP NHẬT: Hàm này giờ gọn hơn, gọi hàm chung cho từng nút
     */
    private void setupButtonHoverEffects() {
        setupSingleButtonHover(homeButton);
        setupSingleButtonHover(invoiceHistoryButton);
        setupSingleButtonHover(loginHistoryButton);
        setupSingleButtonHover(notificationButton);
        setupSingleButtonHover(viewMyRequestsButton);

        // MỚI: Đăng ký hiệu ứng cho nút Tạo Phản Ánh
        setupSingleButtonHover(createRequestButton);

        // Nút Đăng xuất (Logout) có màu riêng nên xử lý riêng
        if (btnLogout != null) {
            btnLogout.setOnMouseEntered(e ->
                    btnLogout.setStyle("-fx-background-color: rgba(244,67,54,1.0); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 600; -fx-cursor: hand; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER;")
            );
            btnLogout.setOnMouseExited(e ->
                    btnLogout.setStyle("-fx-background-color: rgba(244,67,54,0.9); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 600; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER;")
            );
        }
    }

    /**
     * MỚI: Hàm dùng chung để xử lý hiệu ứng Hover cho các nút menu
     * Giúp code không bị lặp lại và dễ sửa đổi style
     */
    private void setupSingleButtonHover(MFXButton button) {
        if (button == null) return; // Tránh lỗi NullPointerException

        // Khi chuột đi vào: Sáng hơn + Căn giữa (CENTER)
        button.setOnMouseEntered(e -> {
            if (currentActiveButton != button) {
                button.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER;");
            }
        });

        // Khi chuột đi ra: Tối hơn + Căn trái (CENTER_LEFT)
        button.setOnMouseExited(e -> {
            if (currentActiveButton != button) {
                button.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER_LEFT;");
            }
        });
    }

    private void loadDashboardData() {
        debtAmountLabel.setText("0 VND");
        paidAmountLabel.setText("0 VND");
        notificationCountLabel.setText("0");
    }

    private void showDashboardContent() {
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(dashboardContent);
        pageTitle.setText("Trang chủ");
        setActiveButton(homeButton);
    }

    private void loadContentFromFxml(String fXMLPath, String title, MFXButton activeButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fXMLPath));
            Node content = loader.load();

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(content);
            pageTitle.setText(title);
            setActiveButton(activeButton);

        } catch (IOException e) {
            e.printStackTrace();
            showDashboardContent();
        }
    }

    /**
     * CẬP NHẬT: Đảm bảo nút Active cũng được căn giữa và có viền
     */
    private void setActiveButton(MFXButton activeButton) {
        // Reset nút cũ (trả về lề trái)
        if (currentActiveButton != null) {
            currentActiveButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER_LEFT;");
        }

        // Set nút mới (Căn giữa + Viền sáng)
        currentActiveButton = activeButton;
        if (currentActiveButton != null) {
            currentActiveButton.setStyle("-fx-background-color: rgba(255,255,255,0.35); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 600; -fx-font-family: 'DejaVu Sans'; -fx-border-color: rgba(255,255,255,0.5); -fx-border-width: 2; -fx-border-radius: 12; -fx-alignment: CENTER;");
        }
    }

    @FXML
    private void handleHomeButton() {
        showDashboardContent();
    }

    @FXML
    private void handleInvoiceHistoryButton() {
        loadContentFromFxml("/com/example/quanlytoanha/view/invoice_history_embedded.fxml", "Hóa đơn và Lịch sử giao dịch", invoiceHistoryButton);
    }

    @FXML
    private void handleLoginHistoryButton() {
        loadContentFromFxml("/com/example/quanlytoanha/view/login_management_embedded.fxml", "Quản lý đăng nhập", loginHistoryButton);
    }

    @FXML
    private void handleNotificationButton() {
        loadContentFromFxml("/com/example/quanlytoanha/view/notification_view_embedded.fxml", "Thông báo", notificationButton);
    }

    @FXML
    private void handleCreateRequestButton() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/CreateServiceRequest.fxml"));
            Parent root = loader.load();
            CreateServiceRequestController controller = loader.getController();
            controller.setCurrentResidentId(currentUser.getUserId());
            Stage stage = new Stage();
            stage.setTitle("Tạo Yêu Cầu Mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewMyRequestsButton() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/MyRequestsList.fxml"));
            Node content = loader.load();
            MyRequestsListController controller = loader.getController();
            controller.loadDataForResident(currentUser.getUserId());
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(content);
            pageTitle.setText("Yêu Cầu Của Tôi");
            setActiveButton(viewMyRequestsButton);
        } catch (IOException e) {
            e.printStackTrace();
            showDashboardContent();
        }
    }

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