// Vị trí: src/main/java/com/example/quanlytoanha/controller/PopulationReportController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.service.ReportService;
import com.example.quanlytoanha.service.ReportExportService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*; // <-- SỬA: Import Alert, Button, ButtonBar, ButtonType
import javafx.stage.Stage;

import java.awt.Desktop; // <-- BỔ SUNG: Import
import java.io.File; // <-- BỔ SUNG: Import
import java.io.IOException; // <-- BỔ SUNG: Import
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional; // <-- BỔ SUNG: Import

public class PopulationReportController {

    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private Label lblMoveIns;
    @FXML private Label lblMoveOuts;
    @FXML private BarChart<String, Number> barChart;
    @FXML private Button btnExportPDF;

    private ReportService reportService;
    private ReportExportService exportService;

    private Map<String, Integer> currentStats;

    @FXML
    public void initialize() {
        this.reportService = new ReportService();
        this.exportService = new ReportExportService();
        this.currentStats = new HashMap<>();

        dpStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        dpEndDate.setValue(LocalDate.now());

        Platform.runLater(this::handleGenerateReport);
    }

    @FXML
    private void handleGenerateReport() {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();

        try {
            this.currentStats = reportService.getPopulationReport(startDate, endDate);
            int moveIns = currentStats.getOrDefault("moveIns", 0);
            int moveOuts = currentStats.getOrDefault("moveOuts", 0);

            lblMoveIns.setText(String.valueOf(moveIns));
            lblMoveOuts.setText(String.valueOf(moveOuts));

            updateBarChart(moveIns, moveOuts);
            btnExportPDF.setDisable(currentStats.isEmpty());

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

        if (currentStats == null || currentStats.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không có dữ liệu để xuất.");
            return;
        }

        try {
            Stage stage = (Stage) btnExportPDF.getScene().getWindow();

            // 1. Lấy file đã lưu
            File savedFile = exportService.exportPopulationReport(stage, startDate, endDate, currentStats);

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

    private void updateBarChart(int moveIns, int moveOuts) {
        barChart.getData().clear();
        barChart.setAnimated(true);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        XYChart.Data<String, Number> moveInData = new XYChart.Data<>("Chuyển vào", moveIns);
        XYChart.Data<String, Number> moveOutData = new XYChart.Data<>("Chuyển đi", moveOuts);
        series.getData().add(moveInData);
        series.getData().add(moveOutData);
        barChart.getData().add(series);

        Platform.runLater(() -> {
            if (moveInData.getNode() != null) {
                moveInData.getNode().setStyle("-fx-bar-fill: #28a745;");
            }
            if (moveOutData.getNode() != null) {
                moveOutData.getNode().setStyle("-fx-bar-fill: #dc3545;");
            }
        });

        barChart.setLegendVisible(false);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}