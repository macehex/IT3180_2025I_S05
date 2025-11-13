// Vị trí: src/main/java/com/example/quanlytoanha/controller/AccessControlController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Apartment;
import com.example.quanlytoanha.dao.ApartmentDAO;
import com.example.quanlytoanha.model.VehicleAccessLog;
import com.example.quanlytoanha.model.VisitorLog;
import com.example.quanlytoanha.service.AccessControlService;
import com.example.quanlytoanha.service.ReportExportService; // <-- BỔ SUNG: Import
import com.example.quanlytoanha.session.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList; // <-- BỔ SUNG: Import
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage; // <-- BỔ SUNG: Import
import javafx.util.StringConverter;

import java.awt.Desktop; // <-- BỔ SUNG: Import
import java.io.File; // <-- BỔ SUNG: Import
import java.io.IOException; // <-- BỔ SUNG: Import
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // <-- BỔ SUNG: Import

public class AccessControlController {

    // (Các khai báo FXML khác giữ nguyên)
    @FXML private TextField txtLicensePlate;
    @FXML private ComboBox<String> cmbVehicleType;
    @FXML private ComboBox<String> cmbAccessType;
    @FXML private TextField txtVehicleNotes;
    @FXML private Button btnLogVehicle;

    @FXML private TextField txtVisitorName;
    @FXML private TextField txtVisitorIdCard;
    @FXML private TextField txtVisitorPhone;
    @FXML private ComboBox<Apartment> cmbVisitorApartment;
    @FXML private TextField txtVisitorReason;
    @FXML private Button btnCheckInVisitor;

    @FXML private DatePicker dpSearchStartDate;
    @FXML private DatePicker dpSearchEndDate;
    @FXML private Button btnSearchLogs;
    @FXML private TableView<VehicleAccessLog> tableVehicleLogs;
    @FXML private TableView<VisitorLog> tableVisitorLogs;
    @FXML private Button btnCheckOutSelectedVisitor;

    // --- BỔ SUNG: Nút Xuất PDF ---
    @FXML private Button btnExportVehiclePDF;
    @FXML private Button btnExportVisitorPDF;

    private AccessControlService accessControlService;
    private ApartmentDAO apartmentDAO;
    private ReportExportService exportService; // <-- BỔ SUNG: Khai báo Service

    // --- BỔ SUNG: List lưu trữ dữ liệu tra cứu ---
    private ObservableList<VehicleAccessLog> currentVehicleLogs;
    private ObservableList<VisitorLog> currentVisitorLogs;

    @FXML
    public void initialize() {
        this.accessControlService = new AccessControlService();
        this.apartmentDAO = new ApartmentDAO();
        this.exportService = new ReportExportService(); // <-- BỔ SUNG: Khởi tạo

        // Khởi tạo List
        this.currentVehicleLogs = FXCollections.observableArrayList();
        this.currentVisitorLogs = FXCollections.observableArrayList();

        // Gắn List vào Bảng
        tableVehicleLogs.setItems(currentVehicleLogs);
        tableVisitorLogs.setItems(currentVisitorLogs);

        cmbVehicleType.setItems(FXCollections.observableArrayList("Xe máy", "Ô tô", "Xe tải", "Khác"));
        cmbAccessType.setItems(FXCollections.observableArrayList("IN", "OUT"));

        loadApartmentComboBox();

        dpSearchStartDate.setValue(LocalDate.now());
        dpSearchEndDate.setValue(LocalDate.now());

        Platform.runLater(this::handleSearchLogs);

        // Logic bật/tắt nút "Check-out"
        tableVisitorLogs.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    btnCheckOutSelectedVisitor.setDisable(newSelection == null || newSelection.getCheckOutTime() != null);
                }
        );
    }

    private void loadApartmentComboBox() {
        try {
            List<Apartment> apartments = apartmentDAO.getAllApartments();
            cmbVisitorApartment.setItems(FXCollections.observableArrayList(apartments));

            cmbVisitorApartment.setConverter(new StringConverter<Apartment>() {
                @Override public String toString(Apartment apartment) {
                    return apartment == null ? null : "Căn hộ " + apartment.getApartmentId();
                }
                @Override public Apartment fromString(String string) { return null; }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Căn Hộ", "Không thể tải danh sách căn hộ.");
        }
    }

    // (Hàm handleLogVehicleAccess và handleCheckInVisitor giữ nguyên)
    @FXML
    private void handleLogVehicleAccess() {
        try {
            VehicleAccessLog log = new VehicleAccessLog();
            log.setLicensePlate(txtLicensePlate.getText());
            log.setVehicleType(cmbVehicleType.getValue());
            log.setAccessType(cmbAccessType.getValue());
            log.setNotes(txtVehicleNotes.getText());
            log.setGuardUserId(SessionManager.getInstance().getCurrentUser().getUserId());
            accessControlService.logVehicleAccess(log);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã ghi nhận xe: " + log.getLicensePlate());
            clearVehicleForm();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể ghi nhận: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckInVisitor() {
        try {
            VisitorLog log = new VisitorLog();
            log.setVisitorName(txtVisitorName.getText());
            log.setIdCardNumber(txtVisitorIdCard.getText());
            log.setContactPhone(txtVisitorPhone.getText());
            log.setReason(txtVisitorReason.getText());
            Apartment selectedApartment = cmbVisitorApartment.getValue();
            log.setApartmentId(selectedApartment != null ? selectedApartment.getApartmentId() : null);
            log.setGuardUserId(SessionManager.getInstance().getCurrentUser().getUserId());
            accessControlService.checkInVisitor(log);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã check-in khách: " + log.getVisitorName());
            clearVisitorForm();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể check-in: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCheckOutSelectedVisitor() {
        VisitorLog selected = tableVisitorLogs.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getCheckOutTime() != null) return;

        try {
            boolean success = accessControlService.checkOutVisitor(selected.getLogId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã check-out cho khách: " + selected.getVisitorName());
                handleSearchLogs();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật CSDL.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể check-out: " + e.getMessage());
        }
    }

    // --- SỬA LỖI: Cập nhật hàm Tra cứu ---
    @FXML
    private void handleSearchLogs() {
        LocalDate startDate = dpSearchStartDate.getValue();
        LocalDate endDate = dpSearchEndDate.getValue();

        try {
            // Tải Lịch sử Xe
            List<VehicleAccessLog> vehicleLogs = accessControlService.searchVehicleLogs(startDate, endDate);
            currentVehicleLogs.setAll(vehicleLogs); // Cập nhật list

            // Tải Lịch sử Khách
            List<VisitorLog> visitorLogs = accessControlService.searchVisitorLogs(startDate, endDate);
            currentVisitorLogs.setAll(visitorLogs); // Cập nhật list

            // Bật/tắt nút PDF
            btnExportVehiclePDF.setDisable(currentVehicleLogs.isEmpty());
            btnExportVisitorPDF.setDisable(currentVisitorLogs.isEmpty());

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Tra cứu", "Không thể tải lịch sử: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- BỔ SUNG: HÀM XUẤT PDF (AC2) ---
    @FXML
    private void handleExportVehicleLogPDF() {
        LocalDate startDate = dpSearchStartDate.getValue();
        LocalDate endDate = dpSearchEndDate.getValue();

        if (currentVehicleLogs.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không có dữ liệu (Xe) để xuất.");
            return;
        }

        try {
            Stage stage = (Stage) btnExportVehiclePDF.getScene().getWindow();
            File savedFile = exportService.exportVehicleAccessLog(stage, startDate, endDate, currentVehicleLogs);
            showExportSuccessAlert(savedFile);
        } catch (Exception e) {
            handleExportError(e);
        }
    }

    @FXML
    private void handleExportVisitorLogPDF() {
        LocalDate startDate = dpSearchStartDate.getValue();
        LocalDate endDate = dpSearchEndDate.getValue();

        if (currentVisitorLogs.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không có dữ liệu (Khách) để xuất.");
            return;
        }

        try {
            Stage stage = (Stage) btnExportVisitorPDF.getScene().getWindow();
            File savedFile = exportService.exportVisitorLog(stage, startDate, endDate, currentVisitorLogs);
            showExportSuccessAlert(savedFile);
        } catch (Exception e) {
            handleExportError(e);
        }
    }

    // --- Hàm tiện ích (Giữ nguyên) ---

    private void clearVehicleForm() {
        txtLicensePlate.clear();
        cmbVehicleType.setValue(null);
        cmbAccessType.setValue(null);
        txtVehicleNotes.clear();
    }

    private void clearVisitorForm() {
        txtVisitorName.clear();
        txtVisitorIdCard.clear();
        txtVisitorPhone.clear();
        cmbVisitorApartment.setValue(null);
        txtVisitorReason.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- BỔ SUNG: Hàm tiện ích cho PDF ---

    private void handleExportError(Exception e) {
        e.printStackTrace();
        String errorMessage = (e.getMessage() == null) ? "Lỗi không xác định" : e.getMessage();
        if (!errorMessage.contains("Người dùng đã hủy thao tác")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Xuất PDF", errorMessage);
        }
    }

    private void showExportSuccessAlert(File savedFile) throws IOException {
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
                Desktop.getDesktop().open(savedFile.getParentFile()); // Mở thư mục cha
            } else {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Không thể tự động mở thư mục.");
            }
        }
    }
}