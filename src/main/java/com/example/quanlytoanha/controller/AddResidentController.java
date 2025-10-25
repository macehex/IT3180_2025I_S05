// Vị trí: src/main/java/com/example/quanlytoanha/controller/AddResidentController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.service.ValidationException;
import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.service.ResidentService;
import javafx.collections.FXCollections;
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
        // ⚠️ Bọc toàn bộ logic trong try-catch để bắt lỗi (nếu có)
        try {
            // 1. Khởi tạo ComboBox Căn hộ (Sử dụng setItems an toàn)
            if (cbApartmentId != null) {
                cbApartmentId.setItems(FXCollections.observableArrayList(
                        101, 102, 201, 202, 301, 302
                ));
            }

            // 2. Khởi tạo ComboBox Mối quan hệ (Sử dụng setItems an toàn)
            if (cbRelationship != null) {
                cbRelationship.setItems(FXCollections.observableArrayList(
                        "Chủ hộ", "Vợ/Chồng", "Con", "Khách thuê"
                ));
            }

            // Mặc định tiêu đề là Thêm Mới (chỉ hoạt động nếu titleLabel được tiêm đúng)
            if (titleLabel != null) {
                titleLabel.setText("THÊM HỒ SƠ CƯ DÂN MỚI");
            }
        } catch (Exception e) {
            System.err.println("LỖI KHỞI TẠO COMBOBOX TRONG ADD_RESIDENT_CONTROLLER:");
            e.printStackTrace();
        }
    }


    /**
     * Thiết lập Controller sang chế độ SỬA/CẬP NHẬT và điền dữ liệu cũ.
     * @param resident Đối tượng Resident cần chỉnh sửa.
     */
    public void setResident(Resident resident) {
        this.residentToEdit = resident;

        // *** KHÔNG CẦN TRY/CATCH BAO BỌC NỮA, CHỈ TRY CATCH TỪNG PHẦN ***
        if (titleLabel != null) {
            titleLabel.setText("CAP NHAT HO SO CU DAN"); // Dùng tiếng Việt không dấu
        }
        if (btnSave != null) {
            btnSave.setText("CAP NHAT"); // Dùng tiếng Việt không dấu
        }

        // --- SECTION A: STRING FIELDS ---
        try {
            if (txtFullName != null) {
                txtFullName.setText(resident.getFullName() != null ? resident.getFullName() : "");
            }
            if (txtIdCard != null) {
                txtIdCard.setText(resident.getIdCardNumber() != null ? resident.getIdCardNumber() : "");
            }
            if (txtPhoneNumber != null) {
                txtPhoneNumber.setText(resident.getPhoneNumber() != null ? resident.getPhoneNumber() : "");
            }
        } catch (Exception e) {
            System.err.println("!!! ERROR A: STRING FIELDS FAILED");
            e.printStackTrace();
            throw new RuntimeException("ERROR A: STRING FIELDS FAILED", e); // Ném lỗi rõ ràng
        }

        // --- SECTION B: DATE PICKER ---
        try {
            if (dpDateOfBirth != null) {
                Date dob = resident.getDateOfBirth();

                // Đảm bảo dob KHÔNG NULL và CÓ THỂ CHUYỂN ĐỔI.
                if (dob != null) {
                    // Cố gắng chuyển đổi
                    try {
                        LocalDate localDate = dob.toInstant()
                                .atZone(ZoneId.systemDefault()).toLocalDate();
                        dpDateOfBirth.setValue(localDate);
                    } catch (Exception dateEx) {
                        // Nếu chuyển đổi thất bại (dữ liệu không hợp lệ), đặt giá trị null
                        System.err.println("CANH BAO: Ngay sinh khong hop le trong DB. Dat gia tri null. " + dateEx.getMessage());
                        dpDateOfBirth.setValue(null);
                    }
                } else {
                    // Nếu dob là NULL, đặt giá trị null
                    dpDateOfBirth.setValue(null);
                }
            }
        } catch (Exception e) {
            System.err.println("!!! ERROR B: DATE PICKER FAILED (Sau Fix)");
            e.printStackTrace();
            throw new RuntimeException("ERROR B: DATE PICKER FAILED (Sau Fix)", e);
        }

        // --- SECTION C: APARTMENT COMBOBOX ---
        try {
            if (cbApartmentId != null && resident.getApartmentId() > 0) {
                Integer selectedApartmentId = Integer.valueOf(resident.getApartmentId());
                if (cbApartmentId.getItems() != null && cbApartmentId.getItems().contains(selectedApartmentId)) {
                    cbApartmentId.getSelectionModel().select(selectedApartmentId);
                } else {
                    cbApartmentId.getSelectionModel().clearSelection();
                }
            }
        } catch (Exception e) {
            System.err.println("!!! ERROR C: APARTMENT COMBOBOX FAILED");
            e.printStackTrace();
            throw new RuntimeException("ERROR C: APARTMENT COMBOBOX FAILED", e); // Ném lỗi rõ ràng
        }

        // --- SECTION D: RELATIONSHIP COMBOBOX ---
        try {
            if (cbRelationship != null && resident.getRelationship() != null) {
                String selectedRelationship = resident.getRelationship();
                if (cbRelationship.getItems() != null && cbRelationship.getItems().contains(selectedRelationship)) {
                    cbRelationship.getSelectionModel().select(selectedRelationship);
                } else {
                    cbRelationship.getSelectionModel().clearSelection();
                }
            }
        } catch (Exception e) {
            System.err.println("!!! ERROR D: RELATIONSHIP COMBOBOX FAILED");
            e.printStackTrace();
            throw new RuntimeException("ERROR D: RELATIONSHIP COMBOBOX FAILED", e); // Ném lỗi rõ ràng
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