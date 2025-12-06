// Vị trí: src/main/java/com/example/quanlytoanha/controller/AddResidentController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.service.ResidentService.ValidationException;
import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.model.Apartment;
import com.example.quanlytoanha.service.ResidentService;
import com.example.quanlytoanha.session.SessionManager; // <-- BỔ SUNG: Import SessionManager
import com.example.quanlytoanha.model.User;             // <-- BỔ SUNG: Import User
import com.example.quanlytoanha.dao.ApartmentDAO;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;
import java.time.LocalDate;

public class AddResidentController {

    // --- KHAI BÁO CÁC THÀNH PHẦN FXML CỦA FORM ---
    @FXML private TextField txtFullName;
    @FXML private ComboBox<Integer> cbApartmentId;
    @FXML private TextField txtIdCard;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private ComboBox<String> cbRelationship;
    @FXML private TextField txtPhoneNumber;
    @FXML private Button btnSave;
    @FXML private Label titleLabel; // (Giữ nguyên)

    // --- KHAI BÁO SERVICE VÀ ĐỐI TƯỢNG SỬA ---
    // (Giữ nguyên)
    private final ResidentService residentService = new ResidentService();
    private final ApartmentDAO apartmentDAO = new ApartmentDAO();
    private Resident residentToEdit;

    /**
     * Phương thức khởi tạo logic cho các ComboBox (Chạy sau khi FXML load)
     */
    @FXML
    public void initialize() {
        // ⚠️ Bọc toàn bộ logic trong try-catch để bắt lỗi (nếu có)
        try {
            // 1. Khởi tạo ComboBox Căn hộ - Load từ database
            loadApartmentList();

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
     * Load danh sách căn hộ từ database vào ComboBox
     */
    private void loadApartmentList() {
        try {
            if (cbApartmentId != null) {
                java.util.List<Apartment> apartments = apartmentDAO.getAllApartments();
                
                // Tạo ObservableList chỉ chứa apartment_id
                javafx.collections.ObservableList<Integer> apartmentIds = FXCollections.observableArrayList();
                for (Apartment apt : apartments) {
                    apartmentIds.add(apt.getApartmentId());
                }
                
                cbApartmentId.setItems(apartmentIds);
                
                // Cấu hình hiển thị: ID - Diện tích - Chủ hộ
                cbApartmentId.setConverter(new StringConverter<Integer>() {
                    @Override
                    public String toString(Integer aptId) {
                        if (aptId == null) return "";
                        
                        // Tìm apartment tương ứng để hiển thị thông tin
                        for (Apartment apt : apartments) {
                            if (apt.getApartmentId() == aptId) {
                                String ownerInfo = (apt.getOwnerName() != null && !apt.getOwnerName().isEmpty()) 
                                    ? " - " + apt.getOwnerName() 
                                    : " - Căn hộ trống";
                                return String.format("CH %d (%.2f m²)%s", aptId, apt.getArea(), ownerInfo);
                            }
                        }
                        return String.valueOf(aptId);
                    }

                    @Override
                    public Integer fromString(String string) {
                        return null;
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi load danh sách căn hộ: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Thiết lập Controller sang chế độ SỬA/CẬP NHẬT và điền dữ liệu cũ.
     */
    public void setResident(Resident resident) {
        this.residentToEdit = resident;

        if (titleLabel != null) {
            titleLabel.setText("CẬP NHẬT HỒ SƠ CƯ DÂN");
        }
        if (btnSave != null) {
            btnSave.setText("CẬP NHẬT");
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
            throw new RuntimeException("ERROR A: STRING FIELDS FAILED", e);
        }

        // --- SECTION B: DATE PICKER ---
        try {
            if (dpDateOfBirth != null) {
                Date dob = resident.getDateOfBirth();

                if (dob != null) {
                    try {
                        // FIX: Chuyển đổi an toàn kiểu Date
                        LocalDate localDate = new java.util.Date(dob.getTime())
                                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        dpDateOfBirth.setValue(localDate);
                    } catch (Exception dateEx) {
                        System.err.println("CẢNH BÁO: Ngày sinh không hợp lệ trong DB. Đặt giá trị null. " + dateEx.getMessage());
                        dpDateOfBirth.setValue(null);
                    }
                } else {
                    dpDateOfBirth.setValue(null);
                }
            }
        } catch (Exception e) {
            System.err.println("!!! ERROR B: DATE PICKER FAILED");
            e.printStackTrace();
            throw new RuntimeException("ERROR B: DATE PICKER FAILED", e);
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
            throw new RuntimeException("ERROR C: APARTMENT COMBOBOX FAILED", e);
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
            throw new RuntimeException("ERROR D: RELATIONSHIP COMBOBOX FAILED", e);
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút LƯU/CẬP NHẬT.
     * Logic này phân biệt chế độ Thêm mới và Cập nhật.
     */
    @FXML
    private void handleSaveButtonAction() {
        if (this.residentToEdit != null) {
            // CHẾ ĐỘ CẬP NHẬT (Gọi hàm mới có Audit Log)
            updateExistingResident();
        } else {
            // CHẾ ĐỘ THÊM MỚI
            createNewResident();
        }
    }

    // --- LOGIC THÊM MỚI (GIỮ NGUYÊN) ---
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

    // --- LOGIC CẬP NHẬT HỒ SƠ CÓ SẴN (BỔ SUNG AUDIT LOG - US1_1_1.4) ---
    private void updateExistingResident() {
        try {
            // 1. Lấy dữ liệu MỚI từ form và gán vào residentToEdit
            String fullName = txtFullName.getText().trim();
            Integer apartmentId = cbApartmentId.getSelectionModel().getSelectedItem();
            String idCard = txtIdCard.getText().trim();
            String relationship = cbRelationship.getSelectionModel().getSelectedItem();

            Date dateOfBirth = (dpDateOfBirth.getValue() != null)
                    ? Date.from(dpDateOfBirth.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant())
                    : null;
            String phoneNumber = txtPhoneNumber.getText().trim();

            // Gán giá trị mới vào đối tượng đang chỉnh sửa
            residentToEdit.setFullName(fullName);
            residentToEdit.setApartmentId(apartmentId);
            residentToEdit.setIdCardNumber(idCard.isEmpty() ? null : idCard);
            residentToEdit.setDateOfBirth(dateOfBirth);
            residentToEdit.setRelationship(relationship);
            residentToEdit.setPhoneNumber(phoneNumber);

            // 2. Lấy ID người dùng (Ban Quản trị) đang thực hiện thay đổi
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Phiên", "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
                return;
            }
            int changedByUserId = currentUser.getUserId();


            // 3. Gọi Service để cập nhật và GHI LOG
            if (residentService.updateResidentAndLog(residentToEdit, changedByUserId)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật hồ sơ cư dân thành công và đã ghi nhận lịch sử!");
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
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Không Xác Định", "Lỗi: " + e.getMessage());
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