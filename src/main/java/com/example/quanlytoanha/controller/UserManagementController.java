// Vị trí: src/main/java/com/example/quanlytoanha/controller/UserManagementController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.service.ResidentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class UserManagementController {

    // --- FXML FIELDS ---
    @FXML private TableView<Resident> residentTable;
    @FXML private TableColumn<Resident, String> colFullName;
    @FXML private TableColumn<Resident, Integer> colApartmentId;
    @FXML private TableColumn<Resident, String> colPhoneNumber;
    @FXML private TableColumn<Resident, String> colRelationship;
    @FXML private Button btnEditResident;
    @FXML private Button btnAddResident;
    // Bạn có thể thêm các cột khác như colEmail, colIdCardNumber, etc.

    // --- SERVICE ---
    private final ResidentService residentService = new ResidentService();
    private ObservableList<Resident> residentList = FXCollections.observableArrayList();

    /**
     * Khởi tạo Controller. Được gọi sau khi FXML được tải.
     */
    @FXML
    public void initialize() {
        // 1. Cấu hình các cột của TableView
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colApartmentId.setCellValueFactory(new PropertyValueFactory<>("apartmentId"));
        colPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colRelationship.setCellValueFactory(new PropertyValueFactory<>("relationship"));
        // Thêm các cấu hình cột khác ở đây

        // 2. Tải dữ liệu ban đầu
        loadResidentData();
    }

    /**
     * Nạp dữ liệu cư dân vào TableView.
     */
    public void loadResidentData() {
        try {
            // Giả định ResidentDAO/Service có phương thức getAllResidents()
            // (Bạn cần triển khai phương thức này trong DAO)
            residentList = FXCollections.observableArrayList(residentService.getAllResidents());
            residentTable.setItems(residentList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể tải danh sách cư dân: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------
    // --- XỬ LÝ CHỨC NĂNG SỬA (CHỨC NĂNG 2) ---
    // ----------------------------------------------------------------------

    /**
     * Xử lý sự kiện khi nhấn nút "CHỈNH SỬA".
     */
    @FXML
    private void handleEditResidentAction() {
        Resident selectedResident = residentTable.getSelectionModel().getSelectedItem();

        if (selectedResident != null) {
            try {
                // 1. Lấy UserID (hoặc ResidentId) và nạp lại Resident đầy đủ từ DB
                Resident residentToEdit = residentService.getResidentById(selectedResident.getUserId());

                if (residentToEdit == null) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy hồ sơ cư dân cần chỉnh sửa.");
                    return;
                }

                // 2. Mở cửa sổ chỉnh sửa ở chế độ Edit
                openResidentForm(residentToEdit);

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Lỗi khi nạp dữ liệu chi tiết: " + e.getMessage());
                e.printStackTrace();
            }

        } else {
            showAlert(Alert.AlertType.WARNING, "Lựa chọn", "Vui lòng chọn một cư dân để chỉnh sửa.");
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút "THÊM MỚI".
     */
    @FXML
    private void handleAddResidentAction() {
        // Mở cửa sổ ở chế độ Thêm mới (truyền null)
        openResidentForm(null);
    }

    /**
     * Mở form Thêm/Sửa Cư dân.
     */
    private void openResidentForm(Resident residentToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/AddResidentController.fxml"));
            Parent root = loader.load();

            // Lấy Controller của form
            AddResidentController controller = loader.getController();

            // Thiết lập chế độ Sửa nếu có đối tượng Resident
            if (residentToEdit != null) {
                controller.setResident(residentToEdit);
            }

            Stage stage = new Stage();
            stage.setTitle(residentToEdit != null ? "Chỉnh sửa Hồ sơ Cư dân" : "Thêm Cư dân Mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Chặn tương tác với cửa sổ chính
            stage.showAndWait();

            // Làm mới bảng sau khi form đóng (để thấy thay đổi)
            loadResidentData();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi giao diện", "Không thể mở form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Phương thức tiện ích
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}