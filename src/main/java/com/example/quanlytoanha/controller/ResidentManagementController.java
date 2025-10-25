package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.model.Role;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Controller đơn giản cho giao diện quản lý cư dân
 * Chỉ có bảng danh sách và thanh tìm kiếm
 */
public class ResidentManagementController {

    // UI Components
    @FXML private Label lblWelcome;
    @FXML private Label lblResultCount;
    @FXML private Label lblStatus;
    
    @FXML private TextField txtSearchName;
    @FXML private TextField txtSearchApartment;
    @FXML private ComboBox<String> cmbStatus;
    
    @FXML private Button btnSearch;
    @FXML private Button btnShowAll;
    @FXML private Button btnLogout;
    
    @FXML private TableView<Resident> tableViewResidents;
    @FXML private TableColumn<Resident, Integer> colResidentId;
    @FXML private TableColumn<Resident, String> colFullName;
    @FXML private TableColumn<Resident, Integer> colApartmentId;
    @FXML private TableColumn<Resident, String> colStatus;
    @FXML private TableColumn<Resident, String> colRelationship;
    @FXML private TableColumn<Resident, String> colIdCard;
    @FXML private TableColumn<Resident, String> colPhone;
    @FXML private TableColumn<Resident, String> colEmail;
    @FXML private TableColumn<Resident, String> colMoveInDate;
    @FXML private TableColumn<Resident, String> colMoveOutDate;
    
    // Data
    private ObservableList<Resident> residentsList;
    private ResidentController residentController;
    private User currentUser;
    
    @FXML
    public void initialize() {
        // Kiểm tra quyền truy cập
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            showAlert("Lỗi", "Bạn không có quyền truy cập chức năng này!", Alert.AlertType.ERROR);
            return;
        }
        
        // Khởi tạo
        residentController = new ResidentController();
        residentsList = FXCollections.observableArrayList();
        
        // Cấu hình giao diện
        setupUI();
        setupTableColumns();
        
        // Load dữ liệu ban đầu
        loadAllResidents();
    }
    
    /**
     * Cấu hình giao diện ban đầu
     */
    private void setupUI() {
        // Cập nhật thông tin người dùng
        lblWelcome.setText("Xin chào, " + currentUser.getFullName());
        
        // Cấu hình ComboBox trạng thái
        cmbStatus.getItems().addAll("RESIDING", "MOVED_OUT", "TEMPORARY_ABSENCE", "PENDING_APPROVAL");
        
        // Cấu hình TableView
        tableViewResidents.setItems(residentsList);
    }
    
    /**
     * Cấu hình các cột của TableView
     */
    private void setupTableColumns() {
        colResidentId.setCellValueFactory(new PropertyValueFactory<>("residentId"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colApartmentId.setCellValueFactory(new PropertyValueFactory<>("apartmentId"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRelationship.setCellValueFactory(new PropertyValueFactory<>("relationship"));
        colIdCard.setCellValueFactory(new PropertyValueFactory<>("idCardNumber"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        // Cấu hình cột ngày tháng
        colMoveInDate.setCellValueFactory(cellData -> {
            java.util.Date moveInDate = cellData.getValue().getMoveInDate();
            return new javafx.beans.property.SimpleStringProperty(
                moveInDate != null ? new SimpleDateFormat("dd/MM/yyyy").format(moveInDate) : ""
            );
        });
        
        colMoveOutDate.setCellValueFactory(cellData -> {
            java.util.Date moveOutDate = cellData.getValue().getMoveOutDate();
            return new javafx.beans.property.SimpleStringProperty(
                moveOutDate != null ? new SimpleDateFormat("dd/MM/yyyy").format(moveOutDate) : ""
            );
        });
    }
    
    /**
     * Load tất cả cư dân
     */
    private void loadAllResidents() {
        updateStatus("Đang tải dữ liệu...", "#f39c12");
        
        try {
            Map<String, Object> result = residentController.getAllResidents();
            
            if ((Boolean) result.get("success")) {
                @SuppressWarnings("unchecked")
                List<Resident> residents = (List<Resident>) result.get("data");
                
                Platform.runLater(() -> {
                    residentsList.clear();
                    residentsList.addAll(residents);
                    updateResultCount(residents.size());
                    updateStatus("Tải dữ liệu thành công", "#27ae60");
                });
            } else {
                Platform.runLater(() -> {
                    updateStatus("Lỗi tải dữ liệu: " + result.get("message"), "#e74c3c");
                });
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                updateStatus("Lỗi tải dữ liệu: " + e.getMessage(), "#e74c3c");
            });
        }
    }
    
    /**
     * Xử lý tìm kiếm cư dân
     */
    @FXML
    private void handleSearch() {
        updateStatus("Đang tìm kiếm...", "#f39c12");
        
        try {
            String name = txtSearchName.getText().trim();
            String apartmentStr = txtSearchApartment.getText().trim();
            String status = cmbStatus.getValue();
            
            Integer apartmentId = null;
            if (!apartmentStr.isEmpty()) {
                try {
                    apartmentId = Integer.parseInt(apartmentStr);
                } catch (NumberFormatException e) {
                    showAlert("Lỗi", "ID căn hộ phải là số!", Alert.AlertType.ERROR);
                    return;
                }
            }
            
            Map<String, Object> result = residentController.searchResidentsSimple(
                name.isEmpty() ? null : name,
                apartmentId,
                status
            );
            
            if ((Boolean) result.get("success")) {
                @SuppressWarnings("unchecked")
                List<Resident> residents = (List<Resident>) result.get("data");
                
                Platform.runLater(() -> {
                    residentsList.clear();
                    residentsList.addAll(residents);
                    updateResultCount(residents.size());
                    updateStatus("Tìm kiếm thành công", "#27ae60");
                });
            } else {
                Platform.runLater(() -> {
                    updateStatus("Lỗi tìm kiếm: " + result.get("message"), "#e74c3c");
                });
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                updateStatus("Lỗi tìm kiếm: " + e.getMessage(), "#e74c3c");
            });
        }
    }
    
    /**
     * Xử lý hiển thị tất cả
     */
    @FXML
    private void handleShowAll() {
        // Xóa các tiêu chí tìm kiếm
        txtSearchName.clear();
        txtSearchApartment.clear();
        cmbStatus.setValue(null);
        
        // Load lại tất cả cư dân
        loadAllResidents();
    }
    
    /**
     * Xử lý đăng xuất
     */
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            SessionManager.getInstance().logout();
            
            try {
                // Quay về màn hình đăng nhập
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/login.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) btnLogout.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Đăng nhập - Hệ thống quản lý tòa nhà");
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể quay về màn hình đăng nhập!", Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Cập nhật số lượng kết quả
     */
    private void updateResultCount(int count) {
        lblResultCount.setText("Kết quả: " + count);
    }
    
    /**
     * Cập nhật trạng thái
     */
    private void updateStatus(String message, String color) {
        lblStatus.setText(message);
        lblStatus.setStyle("-fx-text-fill: " + color + ";");
    }
    
    /**
     * Hiển thị thông báo
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}