// Vị trí: src/main/java/com/example/quanlytoanha/controller/AddResidentController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.service.ValidationException;
import com.example.quanlytoanha.model.ResidentPOJO;
import com.example.quanlytoanha.service.ResidentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;

// Cần thêm các imports sau (nếu chưa có):
// import javafx.collections.FXCollections; 
// import javafx.collections.ObservableList;

public class AddResidentController {

    // --- KHAI BÁO CÁC THÀNH PHẦN FXML CỦA FORM ---
    @FXML private TextField txtFullName;
    @FXML private ComboBox<Integer> cbApartmentId; // Giả sử Apartment ID là số nguyên
    @FXML private TextField txtIdCard;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private ComboBox<String> cbRelationship;
    @FXML private Button btnSave; // Cần thiết để đóng Stage

    // --- KHAI BÁO SERVICE ---
    private final ResidentService residentService = new ResidentService();

    /**
     * Phương thức khởi tạo logic cho các ComboBox (Chạy sau khi FXML load)
     */
    @FXML
    public void initialize() {
        // 1. Khởi tạo ComboBox Căn hộ (Apartment IDs)
        // (Đây là dữ liệu giả định, bạn sẽ cần lấy từ ApartmentDAO trong thực tế)
        cbApartmentId.getItems().addAll(101, 102, 201, 202, 301, 302);

        // 2. Khởi tạo ComboBox Mối quan hệ
        cbRelationship.getItems().addAll("Chủ hộ", "Vợ/Chồng", "Con", "Khách thuê");
    }

    /**
     * Xử lý sự kiện khi nhấn nút LƯU HỒ SƠ. (Logic của form con)
     */
    @FXML
    private void handleSaveButtonAction() {
        try {
            // 1. Lấy dữ liệu từ form
            String fullName = txtFullName.getText().trim();
            Integer apartmentId = cbApartmentId.getSelectionModel().getSelectedItem();
            String idCard = txtIdCard.getText().trim();

            // Xử lý giá trị có thể NULL
            String finalIdCard = idCard.isEmpty() ? null : idCard;
            Date dateOfBirth = (dpDateOfBirth.getValue() != null)
                    ? Date.from(dpDateOfBirth.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant())
                    : null;
            String relationship = cbRelationship.getSelectionModel().getSelectedItem();

            // 2. Tạo đối tượng ResidentPOJO và gán giá trị
            ResidentPOJO newResident = new ResidentPOJO();
            newResident.setFullName(fullName);
            newResident.setApartmentId(apartmentId);
            newResident.setIdCardNumber(finalIdCard);
            newResident.setDateOfBirth(dateOfBirth);
            newResident.setRelationship(relationship);

            // 3. Gọi Service và xử lý kết quả
            if (residentService.createNewResident(newResident)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm cư dân mới thành công: " + fullName);

                // Đóng cửa sổ
                Stage stage = (Stage) btnSave.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm cư dân (Lỗi không xác định).");
            }

        } catch (ValidationException e) {
            // Lỗi Validation (Thiếu trường bắt buộc)
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", e.getMessage());
        } catch (SQLException e) {
            // Lỗi Database
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Lỗi DB: Không thể lưu hồ sơ. " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút HỦY BỎ.
     */
    @FXML
    private void handleCancelButtonAction() {
        // Đóng cửa sổ (dùng bất kỳ control nào trong form để lấy Stage)
        Stage stage = (Stage) txtFullName.getScene().getWindow();
        stage.close();
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