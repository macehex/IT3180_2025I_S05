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
import java.time.LocalDate; // Cần thiết cho DatePicker

public class AddResidentController {

    // --- KHAI BÁO CÁC THÀNH PHẦN FXML CỦA FORM ---
    @FXML private TextField txtFullName;
    @FXML private ComboBox<Integer> cbApartmentId; // Giả sử Apartment ID là số nguyên
    @FXML private TextField txtIdCard;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private ComboBox<String> cbRelationship;
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
        // (Đây là dữ liệu giả định, bạn sẽ cần lấy từ ApartmentDAO trong thực tế)
        cbApartmentId.getItems().addAll(101, 102, 201, 202, 301, 302);

        // 2. Khởi tạo ComboBox Mối quan hệ
        cbRelationship.getItems().addAll("Chủ hộ", "Vợ/Chồng", "Con", "Khách thuê");

        // Mặc định tiêu đề là Thêm Mới
        if (titleLabel != null) {
            titleLabel.setText("THÊM HỒ SƠ CƯ DÂN MỚI");
        }
    }

    // --- CHẾ ĐỘ SỬA: PHƯƠNG THỨC TIẾP NHẬN DỮ LIỆU ---
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
        btnSave.setText("Cập nhật");

        // 2. Điền dữ liệu vào form
        txtFullName.setText(resident.getFullName());
        txtIdCard.setText(resident.getIdCardNumber());

        // Chuyển java.util.Date sang LocalDate cho DatePicker
        if (resident.getDateOfBirth() != null) {
            LocalDate localDate = resident.getDateOfBirth().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            dpDateOfBirth.setValue(localDate);
        }

        // getApartmentId() của bạn trả về int, cần Box (ép kiểu) để chọn trong ComboBox<Integer>
        cbApartmentId.getSelectionModel().select(Integer.valueOf(resident.getApartmentId()));
        cbRelationship.getSelectionModel().select(resident.getRelationship());

        // TODO: Nếu bạn thêm các trường Email/Phone vào form, hãy thêm logic điền ở đây
    }

    /**
     * Xử lý sự kiện khi nhấn nút LƯU/CẬP NHẬT.
     */
    @FXML
    private void handleSaveButtonAction() {
        if (residentToEdit == null) {
            // CHẾ ĐỘ THÊM MỚI
            createNewResident();
        } else {
            // CHẾ ĐỘ CẬP NHẬT
            updateExistingResident();
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

            // 2. Tạo đối tượng Resident
            Resident newResident = new Resident();
            newResident.setFullName(fullName);
            // setApartmentId nhận int, an toàn
            newResident.setApartmentId(apartmentId);
            newResident.setIdCardNumber(finalIdCard);
            newResident.setDateOfBirth(dateOfBirth);
            newResident.setRelationship(relationship);

            // 3. Gọi Service
            if (residentService.createNewResident(newResident)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm cư dân mới thành công: " + fullName);
                // Đóng cửa sổ
                Stage stage = (Stage) btnSave.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm cư dân (Lỗi không xác định).");
            }
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Lỗi DB: Không thể lưu hồ sơ. " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- LOGIC CẬP NHẬT HỒ SƠ CÓ SẴN ---
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

            // 2. Gán giá trị mới vào đối tượng đang chỉnh sửa
            residentToEdit.setFullName(fullName);
            // setApartmentId nhận int, an toàn
            residentToEdit.setApartmentId(apartmentId);
            residentToEdit.setIdCardNumber(idCard.isEmpty() ? null : idCard);
            residentToEdit.setDateOfBirth(dateOfBirth);
            residentToEdit.setRelationship(relationship);
            // Lưu ý: Nếu có email/phone, cần gán: residentToEdit.setEmail(txtEmail.getText());

            // 3. Gọi Service để cập nhật
            if (residentService.updateResident(residentToEdit)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật hồ sơ cư dân thành công!");
                // Đóng cửa sổ
                ((Stage) btnSave.getScene().getWindow()).close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi hệ thống: Cập nhật thất bại.");
            }

        } catch (ValidationException e) {
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