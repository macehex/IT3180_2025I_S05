package com.example.quanlytoanha.controller;

// --- Import các lớp cần thiết ---
import com.example.quanlytoanha.model.ApartmentDebt;
import com.example.quanlytoanha.model.DebtReport;
import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.service.FinancialService;
import com.example.quanlytoanha.service.NotificationService; // Service mới
import com.example.quanlytoanha.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*; // Import Alert, Button, Label, TableView, CheckBox, TextArea, TextField
import javafx.stage.Modality; // Import Modality
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AccountantDashboardController {

    // --- FXML Fields cho Tab Báo cáo ---
    @FXML private Label lblWelcome; // Thêm label welcome
    @FXML private Label lblTotalDebt;
    @FXML private Label lblTotalOverdue;
    @FXML private Label lblUnpaidInvoices;
    @FXML private TableView<ApartmentDebt> debtTable;

    // --- FXML Fields cho Tab Tự động hóa ---
    @FXML private CheckBox chkAutoSendNewInvoice;
    @FXML private CheckBox chkAutoSendReminder;
    @FXML private TextField txtDaysBefore;
    @FXML private CheckBox chkAutoSendOverdue;
    @FXML private Button btnSendManualReminder;
    @FXML private TextArea txtAutomationLog;

    // --- Nút chung ---
    @FXML private Button btnLogout;

    // --- Services ---
    private FinancialService financialService;
    private NotificationService notificationService; // Thêm NotificationService

    @FXML
    public void initialize() {
        // Khởi tạo các services
        this.financialService = new FinancialService();
        this.notificationService = new NotificationService(); // Khởi tạo service mới

        // Cập nhật lời chào
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblWelcome.setText("Xin chào, " + currentUser.getFullName());
        }

        // Tải dữ liệu cho Tab Báo cáo
        loadDashboardData();

        // Thêm sự kiện click đúp cho bảng công nợ
        debtTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ApartmentDebt selectedDebt = debtTable.getSelectionModel().getSelectedItem();
                if (selectedDebt != null) {
                    handleViewDetails(selectedDebt);
                }
            }
        });

        // Gán sự kiện cho nút Gửi thủ công trong Tab Tự động hóa
        if (btnSendManualReminder != null) { // Kiểm tra null để an toàn
            btnSendManualReminder.setOnAction(event -> handleSendManualReminders());
        }

        // (Tùy chọn) Tạm thời vô hiệu hóa các checkbox cấu hình tự động
        if (chkAutoSendNewInvoice != null) chkAutoSendNewInvoice.setDisable(true);
        if (chkAutoSendReminder != null) chkAutoSendReminder.setDisable(true);
        if (txtDaysBefore != null) txtDaysBefore.setDisable(true);
        if (chkAutoSendOverdue != null) chkAutoSendOverdue.setDisable(true);
    }

    /**
     * Tải dữ liệu cho Tab Báo cáo Công nợ.
     */
    @FXML
    private void loadDashboardData() {
        try {
            // 1. Tải thống kê
            DebtReport report = financialService.generateDebtReport();
            lblTotalDebt.setText(String.format("%,.0f VNĐ", report.getTotalDebtAmount()));
            lblTotalOverdue.setText(String.format("%,.0f VNĐ", report.getTotalOverdueAmount()));
            lblUnpaidInvoices.setText(report.getTotalUnpaidInvoices() + " hóa đơn");

            // 2. Tải chi tiết vào bảng
            List<ApartmentDebt> debtList = financialService.getDetailedDebtList();
            debtTable.getItems().setAll(debtList);

        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", "Bạn không có quyền xem thông tin này.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể tải dữ liệu báo cáo: " + e.getMessage());
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

    /**
     * Xử lý sự kiện xem chi tiết công nợ khi click đúp.
     */
    private void handleViewDetails(ApartmentDebt selectedDebt) {
        try {
            List<Invoice> invoiceList = financialService.getDetailedDebtForApartment(selectedDebt.getApartmentId());
            if (invoiceList.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không tìm thấy chi tiết hóa đơn chưa thanh toán cho căn hộ này.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/debt_detail_view.fxml"));
            Parent root = loader.load();
            DebtDetailController detailController = loader.getController();
            detailController.setData(selectedDebt, invoiceList);

            Stage detailStage = new Stage();
            detailStage.setTitle("Chi tiết Công nợ");
            detailStage.initModality(Modality.APPLICATION_MODAL); // Thêm Modality
            detailStage.initOwner((Stage) debtTable.getScene().getWindow());
            detailStage.setScene(new Scene(root));
            detailStage.showAndWait();

        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao diện", "Không thể tải cửa sổ chi tiết.");
        } catch (Exception e) { // Bắt lỗi chung
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xử lý khi nhấn nút "Gửi nhắc nợ thủ công ngay" trong Tab Tự động hóa.
     */
    @FXML
    private void handleSendManualReminders() {
        try {
            int daysBefore = 3; // Giá trị mặc định
            // Lấy giá trị từ TextField, xử lý nếu không phải số
            if (txtDaysBefore != null && !txtDaysBefore.getText().trim().isEmpty()) {
                try {
                    daysBefore = Integer.parseInt(txtDaysBefore.getText().trim());
                    if (daysBefore <= 0) daysBefore = 3; // Đảm bảo số dương
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.WARNING, "Đầu vào không hợp lệ", "Số ngày báo trước phải là một số nguyên dương. Sử dụng giá trị mặc định là 3.");
                    daysBefore = 3; // Quay về mặc định nếu nhập sai
                }
            }

            // Gọi service để gửi thông báo
            List<String> upcomingLogs = notificationService.sendUpcomingReminders(daysBefore);
            List<String> overdueLogs = notificationService.sendOverdueNotifications();

            // Cập nhật log trên giao diện
            logMessages(upcomingLogs);
            logMessages(overdueLogs);

            // Hiển thị thông báo kết quả
            if (upcomingLogs.isEmpty() && overdueLogs.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không tìm thấy hóa đơn nào cần nhắc nợ hoặc đã quá hạn.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi thông báo nhắc nợ và/hoặc quá hạn.");
            }

        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", e.getMessage());
        } catch (Exception e) { // Bắt lỗi SQL hoặc lỗi khác
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Đã xảy ra lỗi khi gửi thông báo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Thêm các thông báo log vào TextArea.
     */
    private void logMessages(List<String> messages) {
        if (txtAutomationLog != null && messages != null && !messages.isEmpty()) {
            for (String msg : messages) {
                // Thêm vào đầu để log mới nhất hiện lên trên
                txtAutomationLog.insertText(0, msg + "\n");
            }
        }
    }

    /**
     * Phương thức tiện ích để hiển thị thông báo Alert.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}