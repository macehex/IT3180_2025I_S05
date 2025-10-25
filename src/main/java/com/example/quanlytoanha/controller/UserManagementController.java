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
        // DÒNG CODE TẠM THỜI ĐỂ BUỘC NÚT HIỂN THỊ
        if (btnEditResident != null) {
            btnEditResident.setVisible(true);
            btnEditResident.setManaged(true);
        }
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
            int userId = selectedResident.getUserId();

            // --- BƯỚC GỠ LỖI: HIỂN THỊ ID ĐANG ĐƯỢC CHỌN ---
            // Hãy kiểm tra console, nếu dòng này in ra '0' thì lỗi là do nạp dữ liệu bảng
            System.out.println("DEBUG: User ID được chọn từ bảng: " + userId);

            // --- BƯỚC 1: KIỂM TRA ID HỢP LỆ ---
            // Nếu userId là 0 hoặc âm, dừng lại ngay lập tức
            if (userId <= 0) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Dữ Liệu", "Hồ sơ cư dân này có User ID không hợp lệ (ID <= 0). Vui lòng kiểm tra dữ liệu gốc.");
                return;
            }

            try {
                // 2. Lấy Resident đầy đủ từ DB bằng UserID hợp lệ
                Resident residentToEdit = residentService.getResidentById(userId);

                if (residentToEdit == null) {
                    // Nếu DAO trả về null, có nghĩa là dữ liệu không đồng bộ (User ID có trong bảng nhưng không có hồ sơ Resident tương ứng)
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy hồ sơ cư dân cần chỉnh sửa (Dữ liệu không đồng bộ).");
                    return;
                }

                // 3. Mở cửa sổ chỉnh sửa ở chế độ Edit
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/add_resident_form.fxml"));

            // ⚠️ DÒNG NÀY PHẢI ĐƯỢC GỌI ĐẦU TIÊN
            Parent root = loader.load();

            // Lấy Controller của form
            AddResidentController controller = loader.getController();

            // *** GỌI HÀM KHỞI TẠO COMBOBOX MỚI ĐÃ TÁCH LOGIC ***
            //controller.initComboBoxes(); // <--- Dòng mới được thêm

            // Thiết lập chế độ Sửa nếu có đối tượng Resident
            if (residentToEdit != null) {
                // Phương thức setResident sẽ điền dữ liệu vào các ComboBox đã được init
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