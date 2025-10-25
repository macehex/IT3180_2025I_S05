// Vị trí: src/main/java/com/example/quanlytoanha/controller/AddResidentController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.service.ValidationException;
import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.service.ResidentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;
import java.time.LocalDate;

public class AddResidentController {

    // --- KHAI BÁO CÁC THÀNH PHẦN FXML CỦA FORM ---
    @FXML private TextField txtFullName;
    @FXML private ComboBox<Integer> cbApartmentId; // Giả sử Apartment ID là số nguyên
    @FXML private TextField txtIdCard;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private ComboBox<String> cbRelationship;
    @FXML private TextField txtPhoneNumber;
    @FXML private Button btnSave;

    // Yêu cầu thêm Label này vào FXML để hiển thị tiêu đề
    @FXML private Label titleLabel;

    // --- KHAI BÁO SERVICE VÀ ĐỐI TƯỢNG SỬA ---
    private final ResidentService residentService = new ResidentService();
    private Resident residentToEdit; // Đối tượng Resident được truyền vào khi ở chế độ SỬA

    /**
     * Phương thức khởi tạo logic cho các ComboBox (Chạy sau khi FXML load)
     */
    @FXML
    public void initialize() {
        // 1. Khởi tạo ComboBox Căn hộ (Apartment IDs)
        cbApartmentId.getItems().addAll(101, 102, 201, 202, 301, 302);

        // 2. Khởi tạo ComboBox Mối quan hệ
        cbRelationship.getItems().addAll("Chủ hộ", "Vợ/Chồng", "Con", "Khách thuê");

        // Mặc định tiêu đề là Thêm Mới
        if (titleLabel != null) {
            titleLabel.setText("THÊM HỒ SƠ CƯ DÂN MỚI");
        }
    }

    /**
     * Thiết lập Controller sang chế độ SỬA/CẬP NHẬT và điền dữ liệu cũ.
     * @param resident Đối tượng Resident cần chỉnh sửa.
     */
    public void setResident(Resident resident) {
        this.residentToEdit = resident;

        // 1. Cập nhật giao diện
        if (titleLabel != null) {
            titleLabel.setText("CẬP NHẬT HỒ SƠ CƯ DÂN");
        }
        btnSave.setText("CẬP NHẬT");

        // 2. Điền dữ liệu vào form - THÊM CÁC KIỂM TRA NULL AN TOÀN TẠI ĐÂY

        // Xử lý các trường String (Sử dụng chuỗi rỗng nếu giá trị là null)
        txtFullName.setText(resident.getFullName() != null ? resident.getFullName() : "");
        txtIdCard.setText(resident.getIdCardNumber() != null ? resident.getIdCardNumber() : "");
        txtPhoneNumber.setText(resident.getPhoneNumber() != null ? resident.getPhoneNumber() : "");

        // Xử lý DatePicker (Đã đúng)
        if (resident.getDateOfBirth() != null) {
            LocalDate localDate = resident.getDateOfBirth().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            dpDateOfBirth.setValue(localDate);
        } else {
            dpDateOfBirth.setValue(null);
        }

        // Xử lý ComboBox ApartmentId
        // Đảm bảo getApartmentId > 0 (vì nó là int)
        if (resident.getApartmentId() > 0) {
            // Integer.valueOf() là an toàn vì getApartmentId là int
            cbApartmentId.getSelectionModel().select(Integer.valueOf(resident.getApartmentId()));
        }

        // Xử lý ComboBox Relationship
        if (resident.getRelationship() != null) {
            cbRelationship.getSelectionModel().select(resident.getRelationship());
        } else {
            cbRelationship.getSelectionModel().clearSelection(); // Xóa lựa chọn nếu null
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút LƯU/CẬP NHẬT.
     * Logic này phân biệt chế độ Thêm mới và Cập nhật (Chức năng 2).
     */
    @FXML
    private void handleSaveButtonAction() {
        // Nếu residentToEdit đã được gán (tức là đang ở chế độ Sửa)
        if (this.residentToEdit != null) {
            // CHẾ ĐỘ CẬP NHẬT (Chức năng 2)
            updateExistingResident();
        } else {
            // CHẾ ĐỘ THÊM MỚI
            createNewResident();
        }
    }

    // --- LOGIC THÊM MỚI ---
    private void createNewResident() {
        try {
            // 1. Lấy dữ liệu từ form
            String fullName = txtFullName.getText().trim();
            Integer apartmentId = cbApartmentId.getSelectionModel().getSelectedItem();
            String idCard = txtIdCard.getText().trim();

            String finalIdCard = idCard.isEmpty() ? null : idCard;
            Date dateOfBirth = (dpDateOfBirth.getValue() != null)
                    ? Date.from(dpDateOfBirth.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant())
                    : null;
            String relationship = cbRelationship.getSelectionModel().getSelectedItem();
            String phoneNumber = txtPhoneNumber.getText().trim();

            // 2. Tạo đối tượng Resident
            Resident newResident = new Resident();
            newResident.setFullName(fullName);
            newResident.setApartmentId(apartmentId);
            newResident.setIdCardNumber(finalIdCard);
            newResident.setDateOfBirth(dateOfBirth);
            newResident.setRelationship(relationship);
            newResident.setPhoneNumber(phoneNumber);

            // 3. Gọi Service
            if (residentService.createNewResident(newResident)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm cư dân mới thành công: " + fullName);
                // Đóng cửa sổ (Hoàn thành AC)
                Stage stage = (Stage) btnSave.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm cư dân (Lỗi không xác định).");
            }
        } catch (ValidationException e) {
            // Hiển thị lỗi Validation (Đáp ứng AC)
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Lỗi DB: Không thể lưu hồ sơ. " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- LOGIC CẬP NHẬT HỒ SƠ CÓ SẴN (CHỨC NĂNG 2) ---
    private void updateExistingResident() {
        try {
            // 1. Lấy dữ liệu mới từ form
            String fullName = txtFullName.getText().trim();
            Integer apartmentId = cbApartmentId.getSelectionModel().getSelectedItem();
            String idCard = txtIdCard.getText().trim();
            String relationship = cbRelationship.getSelectionModel().getSelectedItem();

            Date dateOfBirth = (dpDateOfBirth.getValue() != null)
                    ? Date.from(dpDateOfBirth.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant())
                    : null;
            String phoneNumber = txtPhoneNumber.getText().trim();

            // 2. Gán giá trị mới vào đối tượng đang chỉnh sửa
            residentToEdit.setFullName(fullName);
            residentToEdit.setApartmentId(apartmentId);
            residentToEdit.setIdCardNumber(idCard.isEmpty() ? null : idCard);
            residentToEdit.setDateOfBirth(dateOfBirth);
            residentToEdit.setRelationship(relationship);
            residentToEdit.setPhoneNumber(phoneNumber);

            // 3. Gọi Service để cập nhật
            // (Service sẽ thực hiện Validation SĐT và các trường bắt buộc khác)
            if (residentService.updateResident(residentToEdit)) {
                // AC: Thì hệ thống lưu thay đổi và hiển thị thông báo thành công.
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật hồ sơ cư dân thành công!");
                // Đóng cửa sổ
                ((Stage) btnSave.getScene().getWindow()).close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi hệ thống: Cập nhật thất bại.");
            }

        } catch (ValidationException e) {
            // AC: Thì hệ thống hiển thị lỗi và không cho phép lưu (khi xóa trường bắt buộc)
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Lỗi DB: Không thể cập nhật hồ sơ. " + e.getMessage());
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