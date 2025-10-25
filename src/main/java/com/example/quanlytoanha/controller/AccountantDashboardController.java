// Vị trí: src/main/java/com/example/quanlytoanha/controller/AccountantDashboardController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.ApartmentDebt;
import com.example.quanlytoanha.model.DebtReport;
import com.example.quanlytoanha.service.FinancialService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import java.util.List;

public class AccountantDashboardController {

    // Thống kê
    @FXML private Label lblTotalDebt;
    @FXML private Label lblTotalOverdue;
    @FXML private Label lblUnpaidInvoices;

    // Bảng chi tiết
    @FXML private TableView<ApartmentDebt> debtTable;

    private FinancialService financialService;

    @FXML
    public void initialize() {
        this.financialService = new FinancialService();
        loadDashboardData();
    }

    @FXML
    private void loadDashboardData() {
        try {
            // 1. Tải dữ liệu thống kê
            DebtReport report = financialService.generateDebtReport();

            // Định dạng số liệu (Ví dụ đơn giản)
            lblTotalDebt.setText(String.format("%,.0f VNĐ", report.getTotalDebtAmount()));
            lblTotalOverdue.setText(String.format("%,.0f VNĐ", report.getTotalOverdueAmount()));
            lblUnpaidInvoices.setText(report.getTotalUnpaidInvoices() + " hóa đơn");

            // 2. Tải dữ liệu chi tiết vào Bảng
            List<ApartmentDebt> debtList = financialService.getDetailedDebtList();
            debtTable.getItems().setAll(debtList);

        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", "Bạn không có quyền xem thông tin này.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể tải dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Phương thức tiện ích để hiển thị thông báo Alert.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // Không có tiêu đề phụ
        alert.setContentText(message);
        alert.showAndWait();
    }
}