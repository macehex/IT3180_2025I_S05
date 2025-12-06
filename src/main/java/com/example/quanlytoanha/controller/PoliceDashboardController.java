package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager;
import com.example.quanlytoanha.service.ReportExportService; // Service xuất PDF
import com.example.quanlytoanha.service.FinancialService; // Service lấy dữ liệu công nợ
import com.example.quanlytoanha.service.SecurityDataService; // Service lấy dữ liệu an ninh
import com.example.quanlytoanha.model.ApartmentDebt; // Model dữ liệu
import com.example.quanlytoanha.model.VehicleAccessLog; // Model dữ liệu
import com.example.quanlytoanha.model.VisitorLog; // Model dữ liệu

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory; // Import PropertyValueFactory
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Date; // Import java.util.Date (cho VehicleAccessLog, VisitorLog)
import java.util.Map; // Cần cho báo cáo dân cư

public class PoliceDashboardController {

    @FXML private Label lblWelcome;
    @FXML private Button btnLogout;
    @FXML private ComboBox<String> cbReportType;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private Button btnExportPdf;
    @FXML private TextArea txtLogArea;

    @FXML private Button btnPreviewReport;
    @FXML private TitledPane previewPane;
    @FXML private TableView<?> previewTable;

    private ReportExportService reportExportService;
    private FinancialService financialService; // Service lấy công nợ
    private SecurityDataService securityDataService; // Service lấy dữ liệu xe cộ, khách (Cần tạo)
    private User currentUser;

    private Object cachedReportData; // Dùng Object để lưu nhiều loại List
    private String cachedReportType;

    @FXML
    public void initialize() {
        this.reportExportService = new ReportExportService();
        this.financialService = new FinancialService(); // Giả sử đã có
        this.securityDataService = new SecurityDataService(); // Giả sử bạn tạo service này

        this.currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblWelcome.setText("(Xin chào, " + currentUser.getFullName() + ")");
        }

        // Thêm các loại báo cáo mà Công an được phép xem
        cbReportType.setItems(FXCollections.observableArrayList(
                "Báo cáo Biến động Dân cư",
                "Báo cáo Công nợ Chi tiết",
                "Lịch sử Xe Ra/Vào",
                "Lịch sử Khách Ra/Vào"
        ));

        // Đặt ngày mặc định
        dpStartDate.setValue(LocalDate.now().minusMonths(1));
        dpEndDate.setValue(LocalDate.now());

        btnExportPdf.setDisable(true);
        previewPane.setVisible(false);
        previewPane.setManaged(false);
    }

    @FXML
    private void handlePreviewReport() {
        String reportType = cbReportType.getValue();
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();

        // (Kiểm tra lỗi input giữ nguyên)
        if (reportType == null || startDate == null || endDate == null || startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng chọn loại báo cáo và khoảng ngày hợp lệ.");
            return;
        }

        try {
            // Xóa cache cũ
            cachedReportData = null;
            previewTable.getColumns().clear(); // Xóa các cột cũ
// KHÔNG dùng getItems().clear() để tránh lỗi với Immutable List
// Thay vào đó, gán luôn một list mới rỗng
            previewTable.setItems(FXCollections.observableArrayList());
            boolean isTableView = true;

            // Lấy dữ liệu mới
            switch (reportType) {
                case "Báo cáo Công nợ Chi tiết":
                    List<ApartmentDebt> debtData = financialService.getDetailedDebtList();
                    cachedReportData = debtData;
                    setupDebtPreviewTable();
                    // SỬA: Dùng observableArrayList
                    ((TableView<ApartmentDebt>) previewTable).setItems(FXCollections.observableArrayList(debtData));
                    break;

                case "Báo cáo Biến động Dân cư":
                    Map<String, Integer> stats = securityDataService.getPopulationStats(startDate, endDate);
                    cachedReportData = stats;
                    setupPopulationPreviewTable(stats);
                    isTableView = false; // Đánh dấu đây KHÔNG phải là bảng
                    btnExportPdf.setDisable(false);
                    break;

                case "Lịch sử Xe Ra/Vào":
                    List<VehicleAccessLog> vehicleData = securityDataService.getVehicleLogs(startDate, endDate);
                    cachedReportData = vehicleData;
                    setupVehiclePreviewTable();
                    ((TableView<VehicleAccessLog>) previewTable).setItems(FXCollections.observableArrayList(vehicleData));
                    break;

                case "Lịch sử Khách Ra/Vào":
                    List<VisitorLog> visitorData = securityDataService.getVisitorLogs(startDate, endDate);
                    cachedReportData = visitorData;
                    setupVisitorPreviewTable();
                    // SỬA: Dùng observableArrayList
                    ((TableView<VisitorLog>) previewTable).setItems(FXCollections.observableArrayList(visitorData));
                    break;
            }

            cachedReportType = reportType; // Lưu lại loại báo cáo đã chọn

            // Hiển thị vùng xem trước và kích hoạt nút xuất (trừ BĐ Dân cư đã làm)
            if (isTableView) {
                // 1. Đảm bảo TableView hiển thị
                previewTable.setVisible(true);

                // 2. QUAN TRỌNG: Gắn lại TableView vào TitledPane
                // (Vì nếu trước đó xem báo cáo Dân cư, chỗ này đang là TextArea)
                previewPane.setContent(previewTable);

                previewPane.setVisible(true);
                previewPane.setManaged(true);
                btnExportPdf.setDisable(false);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Lấy Dữ liệu", "Không thể lấy dữ liệu xem trước: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Xử lý khi nhấn nút Xuất PDF.
     */
    @FXML
    private void handleExportPdf() {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();

        // Kiểm tra xem đã có dữ liệu xem trước chưa
        if (cachedReportData == null || cachedReportType == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng nhấn 'Xem trước' để lấy dữ liệu trước khi xuất.");
            return;
        }

        try {
            Stage stage = (Stage) btnExportPdf.getScene().getWindow();
            File exportedFile = null;

            // Dùng switch để gọi đúng hàm xuất PDF với dữ liệu đã cache
            switch (cachedReportType) {
                case "Báo cáo Công nợ Chi tiết":
                    exportedFile = reportExportService.exportDebtReport(stage, startDate, endDate, (List<ApartmentDebt>) cachedReportData);
                    break;
                case "Báo cáo Biến động Dân cư":
                    exportedFile = reportExportService.exportPopulationReport(stage, startDate, endDate, (Map<String, Integer>) cachedReportData);
                    break;
                case "Lịch sử Xe Ra/Vào":
                    exportedFile = reportExportService.exportVehicleAccessLog(stage, startDate, endDate, (List<VehicleAccessLog>) cachedReportData);
                    break;
                case "Lịch sử Khách Ra/Vào":
                    exportedFile = reportExportService.exportVisitorLog(stage, startDate, endDate, (List<VisitorLog>) cachedReportData);
                    break;
            }

            // Ghi log hành động (Giữ nguyên)
            if (exportedFile != null) {
                String log = String.format("[%s] Người dùng '%s' (Role: %s) đã truy xuất và xuất file: %s",
                        // ... (log logic)
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                        currentUser.getUsername(),
                        currentUser.getRole().toString(),
                        exportedFile.getName()
                );
                txtLogArea.insertText(0, log + "\n");
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xuất báo cáo thành công:\n" + exportedFile.getAbsolutePath());
            }

        } catch (IOException e) {
            // ... (xử lý lỗi IO)
        } catch (Exception e) {
            // ... (xử lý lỗi chung)
        }
    }

    private void setupDebtPreviewTable() {
        previewTable.getColumns().clear();
        TableColumn<ApartmentDebt, Integer> colId = new TableColumn<>("Căn hộ");
        colId.setCellValueFactory(new PropertyValueFactory<>("apartmentId"));
        TableColumn<ApartmentDebt, String> colOwner = new TableColumn<>("Chủ hộ");
        colOwner.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        TableColumn<ApartmentDebt, BigDecimal> colDue = new TableColumn<>("Tổng nợ");
        colDue.setCellValueFactory(new PropertyValueFactory<>("totalDue"));

        ((TableView<ApartmentDebt>) previewTable).getColumns().addAll(colId, colOwner, colDue);
    }

    private void setupVehiclePreviewTable() {
        previewTable.getColumns().clear();
        TableColumn<VehicleAccessLog, Date> colTime = new TableColumn<>("Thời gian");
        colTime.setCellValueFactory(new PropertyValueFactory<>("accessTime"));
        TableColumn<VehicleAccessLog, String> colPlate = new TableColumn<>("Biển số");
        colPlate.setCellValueFactory(new PropertyValueFactory<>("licensePlate"));
        TableColumn<VehicleAccessLog, String> colType = new TableColumn<>("Loại");
        colType.setCellValueFactory(new PropertyValueFactory<>("accessType"));
        TableColumn<VehicleAccessLog, String> colRes = new TableColumn<>("Cư dân");
        colRes.setCellValueFactory(new PropertyValueFactory<>("residentFullName"));
        TableColumn<VehicleAccessLog, String> colGuard = new TableColumn<>("Bảo vệ");
        colGuard.setCellValueFactory(new PropertyValueFactory<>("guardFullName"));

        ((TableView<VehicleAccessLog>) previewTable).getColumns().addAll(colTime, colPlate, colType, colRes, colGuard);
    }

    private void setupVisitorPreviewTable() {
        previewTable.getColumns().clear();
        TableColumn<VisitorLog, Date> colIn = new TableColumn<>("Giờ vào");
        colIn.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        TableColumn<VisitorLog, Date> colOut = new TableColumn<>("Giờ ra");
        colOut.setCellValueFactory(new PropertyValueFactory<>("checkOutTime"));
        TableColumn<VisitorLog, String> colName = new TableColumn<>("Tên khách");
        colName.setCellValueFactory(new PropertyValueFactory<>("visitorName"));
        TableColumn<VisitorLog, String> colApt = new TableColumn<>("Căn hộ");
        colApt.setCellValueFactory(new PropertyValueFactory<>("apartmentId"));
        TableColumn<VisitorLog, String> colGuard = new TableColumn<>("Bảo vệ");
        colGuard.setCellValueFactory(new PropertyValueFactory<>("guardFullName"));

        ((TableView<VisitorLog>) previewTable).getColumns().addAll(colIn, colOut, colName, colApt, colGuard);
    }

    private void setupPopulationPreviewTable(Map<String, Integer> stats) {
        // Đặc biệt: BĐ Dân cư không dùng TableView, dùng TextArea cho đơn giản
        previewTable.setVisible(false); // Ẩn bảng
        previewPane.setVisible(true); // Hiện TitledPane
        previewPane.setManaged(true);

        TextArea statsText = new TextArea();
        statsText.setEditable(false);
        statsText.setText(
                "Báo cáo Biến động Dân cư:\n" +
                        "----------------------------\n" +
                        "Số lượng cư dân chuyển vào: " + stats.getOrDefault("moveIns", 0) + "\n" +
                        "Số lượng cư dân chuyển đi: " + stats.getOrDefault("moveOuts", 0) + "\n"
        );
        previewPane.setContent(statsText); // Đặt TextArea làm nội dung
    }

    @FXML
    private void handleLogout() {
        // (Copy hàm handleLogout từ AdminDashboardController hoặc LoginController)
        try {
            SessionManager.getInstance().logout();
            Stage currentStage = (Stage) btnLogout.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Quản lý Tòa nhà - Đăng nhập");
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
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