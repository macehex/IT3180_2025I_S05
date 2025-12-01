// Vị trí: src/main/java/com/example/quanlytoanha/controller/AssetReportController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.AssetReport;
import com.example.quanlytoanha.service.AssetService;
import com.example.quanlytoanha.service.ReportExportService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class AssetReportController {

    @FXML private Label lblTotalAssets;
    @FXML private Label lblTotalInitialCost;
    @FXML private Label lblTotalMaintenanceCost;
    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> locationBarChart;
    @FXML private BarChart<String, Number> maintenanceCostBarChart;
    @FXML private TableView<Map.Entry<String, Integer>> statusTable;
    @FXML private TableColumn<Map.Entry<String, Integer>, String> colStatus;
    @FXML private TableColumn<Map.Entry<String, Integer>, Integer> colStatusCount;
    @FXML private TableView<Map.Entry<String, Integer>> locationTable;
    @FXML private TableColumn<Map.Entry<String, Integer>, String> colLocation;
    @FXML private TableColumn<Map.Entry<String, Integer>, Integer> colLocationCount;
    @FXML private TableView<Map.Entry<String, BigDecimal>> maintenanceCostTable;
    @FXML private TableColumn<Map.Entry<String, BigDecimal>, String> colMaintenanceLocation;
    @FXML private TableColumn<Map.Entry<String, BigDecimal>, String> colMaintenanceCost;
    @FXML private Button btnExportPDF;
    @FXML private Button btnRefresh;

    private AssetService assetService;
    private ReportExportService exportService;
    private AssetReport currentReport;

    @FXML
    public void initialize() {
        this.assetService = new AssetService();
        this.exportService = new ReportExportService();

        // Cấu hình bảng Status
        colStatus.setCellValueFactory(entry -> new javafx.beans.property.SimpleStringProperty(entry.getValue().getKey()));
        colStatusCount.setCellValueFactory(entry -> new javafx.beans.property.SimpleIntegerProperty(entry.getValue().getValue()).asObject());

        // Cấu hình bảng Location
        colLocation.setCellValueFactory(entry -> new javafx.beans.property.SimpleStringProperty(entry.getValue().getKey()));
        colLocationCount.setCellValueFactory(entry -> new javafx.beans.property.SimpleIntegerProperty(entry.getValue().getValue()).asObject());

        // Cấu hình bảng Maintenance Cost
        colMaintenanceLocation.setCellValueFactory(entry -> new javafx.beans.property.SimpleStringProperty(entry.getValue().getKey()));
        colMaintenanceCost.setCellValueFactory(entry -> {
            BigDecimal cost = entry.getValue().getValue();
            String formatted = String.format("%,.0f VNĐ", cost.doubleValue());
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        // Cấu hình tự động điều chỉnh độ rộng cột cho các bảng
        statusTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        locationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        maintenanceCostTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Tự động tải dữ liệu khi khởi động
        Platform.runLater(this::handleGenerateReport);
    }

    @FXML
    private void handleGenerateReport() {
        try {
            currentReport = assetService.generateAssetReport();
            updateUI(currentReport);
            btnExportPDF.setDisable(false);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Dữ liệu", "Không thể tải dữ liệu báo cáo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportPDF() {
        if (currentReport == null) {
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không có dữ liệu để xuất.");
            return;
        }

        try {
            Stage stage = (Stage) btnExportPDF.getScene().getWindow();
            File savedFile = exportService.exportAssetReport(stage, currentReport);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText("Đã xuất file PDF thành công!");
            alert.setContentText("File đã được lưu tại: \n" + savedFile.getAbsolutePath());

            ButtonType buttonTypeOpen = new ButtonType("Mở Thư mục Chứa File");
            ButtonType buttonTypeOK = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(buttonTypeOpen, buttonTypeOK);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == buttonTypeOpen) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(savedFile.getParentFile());
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

    private void updateUI(AssetReport report) {
        // Cập nhật tổng quan
        lblTotalAssets.setText(String.valueOf(report.getTotalAssets()));
        lblTotalInitialCost.setText(String.format("%,.0f VNĐ", report.getTotalInitialCost().doubleValue()));
        lblTotalMaintenanceCost.setText(String.format("%,.0f VNĐ", report.getTotalMaintenanceCost().doubleValue()));

        // Cập nhật Pie Chart theo tình trạng
        updateStatusPieChart(report.getStatusCounts());

        // Cập nhật Bar Chart theo vị trí
        updateLocationBarChart(report.getLocationCounts());

        // Cập nhật Bar Chart chi phí bảo trì
        updateMaintenanceCostBarChart(report.getMaintenanceCostByLocation());

        // Cập nhật bảng Status
        updateStatusTable(report.getStatusCounts());

        // Cập nhật bảng Location
        updateLocationTable(report.getLocationCounts());

        // Cập nhật bảng Maintenance Cost
        updateMaintenanceCostTable(report.getMaintenanceCostByLocation());
    }

    private void updateStatusPieChart(Map<String, Integer> statusCounts) {
        statusPieChart.getData().clear();
        if (statusCounts != null && !statusCounts.isEmpty()) {
            for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
                PieChart.Data data = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
                statusPieChart.getData().add(data);
            }
            statusPieChart.setAnimated(true);
        }
    }

    private void updateLocationBarChart(Map<String, Integer> locationCounts) {
        locationBarChart.getData().clear();
        if (locationCounts != null && !locationCounts.isEmpty()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Số lượng tài sản");
            for (Map.Entry<String, Integer> entry : locationCounts.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            locationBarChart.getData().add(series);
            locationBarChart.setAnimated(true);
        }
    }

    private void updateMaintenanceCostBarChart(Map<String, BigDecimal> costByLocation) {
        maintenanceCostBarChart.getData().clear();
        if (costByLocation != null && !costByLocation.isEmpty()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Chi phí bảo trì (VNĐ)");
            for (Map.Entry<String, BigDecimal> entry : costByLocation.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().doubleValue()));
            }
            maintenanceCostBarChart.getData().add(series);
            maintenanceCostBarChart.setAnimated(true);
        }
    }

    private void updateStatusTable(Map<String, Integer> statusCounts) {
        statusTable.getItems().clear();
        if (statusCounts != null && !statusCounts.isEmpty()) {
            statusTable.getItems().addAll(statusCounts.entrySet());
        }
    }

    private void updateLocationTable(Map<String, Integer> locationCounts) {
        locationTable.getItems().clear();
        if (locationCounts != null && !locationCounts.isEmpty()) {
            locationTable.getItems().addAll(locationCounts.entrySet());
        }
    }

    private void updateMaintenanceCostTable(Map<String, BigDecimal> costByLocation) {
        maintenanceCostTable.getItems().clear();
        if (costByLocation != null && !costByLocation.isEmpty()) {
            maintenanceCostTable.getItems().addAll(costByLocation.entrySet());
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

