package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.service.DashboardService;
import com.example.quanlytoanha.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

public class AdminDashboardController {

    @FXML private Button btnThemCuDan;
    @FXML private Label lblWelcome;
    @FXML private Button btnQuanLyTaiKhoan;
    @FXML private Button btnQuanLyHoaDon;
    @FXML private Button btnTaoThongBao;
    @FXML private Button btnXemYeuCauDichVu;
    @FXML private Button btnXemDanhSachCuDan;
    @FXML private Button btnLogout;

    @FXML private Label lblTotalResidents;
    @FXML private Label lblTotalApartments;
    @FXML private Label lblTotalDebt;
    @FXML private Label lblTotalUnpaidInvoices;

    // --- THÊM: Khai báo Service ---
    private DashboardService dashboardService;

    @FXML
    public void initialize() {
        this.dashboardService = new DashboardService();
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            lblWelcome.setText("Xin chào, " + currentUser.getFullName() + " (Ban Quản Trị)");

            loadDashboardStats();

            if (btnThemCuDan != null) {
                boolean hasPermission = currentUser.hasPermission("CREATE_RESIDENT");
                btnThemCuDan.setVisible(hasPermission);
                btnThemCuDan.setManaged(hasPermission);

                if (hasPermission) {
                    btnThemCuDan.setOnAction(event -> handleOpenAddResidentForm());
                }
            }

            if (btnQuanLyTaiKhoan != null)
                btnQuanLyTaiKhoan.setOnAction(event -> handleQuanLyTaiKhoan());
            if (btnQuanLyHoaDon != null)
                btnQuanLyHoaDon.setOnAction(event -> handleQuanLyHoaDon());
            if (btnTaoThongBao != null)
                btnTaoThongBao.setOnAction(event -> handleTaoThongBao());
            if (btnXemYeuCauDichVu != null)
                btnXemYeuCauDichVu.setOnAction(event -> handleXemYeuCauDichVu());
            if (btnXemDanhSachCuDan != null)
                btnXemDanhSachCuDan.setOnAction(event -> handleOpenResidentList());
        }
    }

    private void loadDashboardStats() {
        try {
            Map<String, Object> stats = dashboardService.getAdminDashboardStats();

            // Lấy giá trị từ Map và cập nhật Label
            // Dùng getOrDefault để tránh lỗi nếu key không tồn tại
            lblTotalResidents.setText(String.valueOf(stats.getOrDefault("totalResidents", 0)));
            lblTotalApartments.setText(String.valueOf(stats.getOrDefault("totalApartments", 0)));

            // Định dạng tiền tệ cho Công nợ
            BigDecimal totalDebt = (BigDecimal) stats.getOrDefault("totalDebt", BigDecimal.ZERO);
            lblTotalDebt.setText(String.format("%,.0f", totalDebt)); // Ví dụ: 1,250,000

            lblTotalUnpaidInvoices.setText(String.valueOf(stats.getOrDefault("totalUnpaidInvoices", 0)));

        } catch (SecurityException e) {
            // Không hiển thị lỗi quyền ở đây vì nếu vào được màn hình này thì đã có quyền
            System.err.println("Lỗi phân quyền khi tải thống kê: " + e.getMessage());
        } catch (Exception e) { // Bắt SQLException hoặc lỗi khác
            showAlert(Alert.AlertType.WARNING, "Lỗi Tải Dữ Liệu", "Không thể tải số liệu thống kê.");
            e.printStackTrace();
            // Có thể đặt giá trị lỗi cho các Label
            lblTotalResidents.setText("Lỗi");
            lblTotalApartments.setText("Lỗi");
            lblTotalDebt.setText("Lỗi");
            lblTotalUnpaidInvoices.setText("Lỗi");
        }
    }

    private void handleOpenAddResidentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/add_resident_form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Tạo Hồ Sơ Cư Dân Mới");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnThemCuDan.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải form thêm cư dân.");
        }
    }

    private void handleQuanLyTaiKhoan() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/UserManagement.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Quản lý tài khoản người dùng");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnQuanLyTaiKhoan.getScene().getWindow());
            stage.setScene(new Scene(root, 900, 600));
            stage.setResizable(true);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải form quản lý tài khoản.");
        }
    }

    private void handleQuanLyHoaDon() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Quản lý hóa đơn chưa được triển khai.");
    }

    private void handleTaoThongBao() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Tạo thông báo chưa được triển khai.");
    }

    private void handleXemYeuCauDichVu() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Xem yêu cầu dịch vụ chưa được triển khai.");
    }

    @FXML
    private void handleOpenResidentList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/resident_list.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Danh sách cư dân");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(btnXemDanhSachCuDan.getScene().getWindow());
            stage.setScene(new Scene(root, 1000, 700));
            stage.setResizable(true);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách cư dân.");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.getInstance().logout();
            Stage currentStage = (Stage) btnLogout.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Quản lý Tòa nhà - Đăng nhập");
            loginStage.setScene(new Scene(root, 450, 500));
            loginStage.setResizable(true);
            loginStage.setMinWidth(450);
            loginStage.setMinHeight(500);
            loginStage.setMaximized(true); // Set full screen
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể quay lại màn hình đăng nhập.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
