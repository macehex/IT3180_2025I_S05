// Vị trí: src/main/java/com/example/quanlytoanha/controller/DebtReportController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.ApartmentDebt;
import com.example.quanlytoanha.service.ReportService;
import com.example.quanlytoanha.service.ReportExportService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*; // <-- SỬA: Import Alert, Button, ButtonBar, ButtonType
import javafx.stage.Stage;

import java.awt.Desktop; // <-- BỔ SUNG: Import cho việc mở thư mục
import java.io.File; // <-- BỔ SUNG: Import File
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // <-- BỔ SUNG: Import Optional

public class DebtReportController {

    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TableView<ApartmentDebt> debtTable;
    @FXML private Button btnExportPDF;

    private ReportService reportService;
    private ReportExportService exportService;

    private ObservableList<ApartmentDebt> currentReportData;

    @FXML
    public void initialize() {
        this.reportService = new ReportService();
        this.exportService = new ReportExportService();
        this.currentReportData = FXCollections.observableArrayList();

        debtTable.setItems(currentReportData);

        dpStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        dpEndDate.setValue(LocalDate.now());

        Platform.runLater(this::handleGenerateReport);
    }

    @FXML
    private void handleGenerateReport() {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();

        try {
            List<ApartmentDebt> reportData = reportService.getDebtReport(startDate, endDate);
            currentReportData.setAll(reportData);
            btnExportPDF.setDisable(currentReportData.isEmpty());

        } catch (IllegalArgumentException | SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Dữ liệu", e.getMessage());
            e.printStackTrace();
        }
    }

    // --- SỬA LỖI: Cải thiện thông báo và thêm nút "Mở Thư mục" ---
    @FXML
    private void handleExportPDF() {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();

        if (currentReportData.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không có dữ liệu để xuất.");
            return;
        }

        try {
            Stage stage = (Stage) btnExportPDF.getScene().getWindow();

            // 1. Lấy file đã lưu từ Service
            File savedFile = exportService.exportDebtReport(stage, startDate, endDate, currentReportData);

            // 2. Tạo Alert tùy chỉnh
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText("Đã xuất file PDF thành công!");
            alert.setContentText("File đã được lưu tại: \n" + savedFile.getAbsolutePath());

            ButtonType buttonTypeOpen = new ButtonType("Mở Thư mục Chứa File");
            ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

            alert.getButtonTypes().setAll(buttonTypeOpen, buttonTypeOK);

            // 3. Chờ người dùng nhấn nút
            Optional<ButtonType> result = alert.showAndWait();

            // 4. Nếu người dùng nhấn "Mở Thư mục"
            if (result.isPresent() && result.get() == buttonTypeOpen) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(savedFile.getParentFile()); // Mở thư mục cha
                } else {
                    showAlert(Alert.AlertType.WARNING, "Lỗi", "Không thể tự động mở thư mục trên hệ điều hành này.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = (e.getMessage() == null) ? "Lỗi không xác định" : e.getMessage();
            if (!errorMessage.contains("Người dùng đã hủy thao tác")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Xuất PDF", errorMessage);
            }
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