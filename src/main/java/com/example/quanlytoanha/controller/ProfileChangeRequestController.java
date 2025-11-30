package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.dao.ProfileChangeRequestDAO;
import com.example.quanlytoanha.dao.ResidentDAO;
import com.example.quanlytoanha.dao.UserDAO;
import com.example.quanlytoanha.model.ProfileChangeRequest;
import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ProfileChangeRequestController implements Initializable {

    @FXML private TextField txtUsername;
    @FXML private TextField txtPhoneNumber;
    @FXML private TextField txtEmail;
    @FXML private TextField txtFullName;
    @FXML private ComboBox<String> cmbRelationship;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtIdCardNumber;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;
    @FXML private Label lblCurrentUsername;
    @FXML private Label lblCurrentPhoneNumber;
    @FXML private Label lblCurrentEmail;
    @FXML private Label lblCurrentFullName;
    @FXML private Label lblCurrentRelationship;
    @FXML private Label lblCurrentDateOfBirth;
    @FXML private Label lblCurrentIdCardNumber;

    private ProfileChangeRequestDAO profileChangeRequestDAO;
    private ResidentDAO residentDAO;
    private UserDAO userDAO;
    private Resident currentResident;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        profileChangeRequestDAO = new ProfileChangeRequestDAO();
        residentDAO = new ResidentDAO();
        userDAO = new UserDAO();
        
        // Initialize relationship dropdown
        initializeRelationshipComboBox();
        
        loadCurrentUserData();
        setupValidation();
    }

    private void initializeRelationshipComboBox() {
        cmbRelationship.getItems().addAll(
            "Chủ hộ",
            "Vợ/Chồng", 
            "Con",
            "Khách thuê"
        );
    }

    private void loadCurrentUserData() {
        try {
            // Debug session and user info
            if (SessionManager.getInstance().getCurrentUser() == null) {
                System.err.println("DEBUG: Current user is null in session!");
                showAlert("Lỗi", "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại!", Alert.AlertType.ERROR);
                closeWindow();
                return;
            }
            
            int currentUserId = SessionManager.getInstance().getCurrentUser().getUserId();
            System.out.println("DEBUG: Loading data for user ID: " + currentUserId);
            
            // Check if user has pending request
            if (profileChangeRequestDAO.hasPendingRequest(currentUserId)) {
                showAlert("Thông báo", "Bạn đã có yêu cầu thay đổi thông tin đang chờ xử lý. Vui lòng đợi admin phê duyệt trước khi gửi yêu cầu mới.", Alert.AlertType.WARNING);
                // Don't close window during initialization - let the user navigate away manually
                return;
            }
            
            currentResident = residentDAO.getResidentByUserId(currentUserId);
            System.out.println("DEBUG: Retrieved resident: " + (currentResident != null ? currentResident.getFullName() : "null"));
            
            if (currentResident == null) {
                showAlert("Lỗi", "Không tìm thấy thông tin cư dân cho user ID: " + currentUserId + 
                         "\nBạn có thể cần được thêm vào hệ thống cư dân trước.\n\n" +
                         "Chức năng này chỉ dành cho cư dân. Admin vui lòng sử dụng tài khoản cư dân.", Alert.AlertType.ERROR);
                // Don't close window during initialization - let the user navigate away manually
                return;
            }

            // Display current values
            lblCurrentUsername.setText(currentResident.getUsername() != null ? currentResident.getUsername() : "");
            lblCurrentPhoneNumber.setText(currentResident.getPhoneNumber() != null ? currentResident.getPhoneNumber() : "");
            lblCurrentEmail.setText(currentResident.getEmail() != null ? currentResident.getEmail() : "");
            lblCurrentFullName.setText(currentResident.getFullName() != null ? currentResident.getFullName() : "");
            lblCurrentRelationship.setText(currentResident.getRelationship() != null ? currentResident.getRelationship() : "");
            lblCurrentDateOfBirth.setText(currentResident.getDateOfBirth() != null ? currentResident.getDateOfBirth().toString() : "");
            lblCurrentIdCardNumber.setText(currentResident.getIdCardNumber() != null ? currentResident.getIdCardNumber() : "");

            // Pre-fill new values with current values
            txtUsername.setText(currentResident.getUsername() != null ? currentResident.getUsername() : "");
            txtPhoneNumber.setText(currentResident.getPhoneNumber() != null ? currentResident.getPhoneNumber() : "");
            txtEmail.setText(currentResident.getEmail() != null ? currentResident.getEmail() : "");
            txtFullName.setText(currentResident.getFullName() != null ? currentResident.getFullName() : "");
            cmbRelationship.setValue(currentResident.getRelationship() != null ? currentResident.getRelationship() : null);
            if (currentResident.getDateOfBirth() != null) {
                datePicker.setValue(currentResident.getDateOfBirth().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            }
            txtIdCardNumber.setText(currentResident.getIdCardNumber() != null ? currentResident.getIdCardNumber() : "");

        } catch (Exception e) {
            System.err.println("DEBUG: Exception in loadCurrentUserData:");
            e.printStackTrace();
            
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = "Unknown error: " + e.getClass().getSimpleName();
            }
            
            System.err.println("DEBUG: Exception type: " + e.getClass().getSimpleName());
            System.err.println("DEBUG: Exception message: " + errorMsg);
            
            // Only show error dialog if it's a critical error that prevents form loading
            if (currentResident == null) {
                showAlert("Lỗi", "Không thể tải thông tin người dùng: " + errorMsg + 
                         "\n\nVui lòng thử lại hoặc liên hệ admin.", Alert.AlertType.ERROR);
            } else {
                // Form loaded successfully despite the exception, just log it
                System.err.println("DEBUG: Non-critical exception during initialization, form will continue to work");
            }
        }
    }

    private void setupValidation() {
        // Add input validation
        txtEmail.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty() && !isValidEmail(newValue)) {
                txtEmail.setStyle("-fx-border-color: red;");
            } else {
                txtEmail.setStyle("");
            }
        });

        txtPhoneNumber.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty() && !isValidPhoneNumber(newValue)) {
                txtPhoneNumber.setStyle("-fx-border-color: red;");
            } else {
                txtPhoneNumber.setStyle("");
            }
        });

        txtIdCardNumber.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty() && !isValidIdCard(newValue)) {
                txtIdCardNumber.setStyle("-fx-border-color: red;");
            } else {
                txtIdCardNumber.setStyle("");
            }
        });
    }

    @FXML
    private void handleSubmit() {
        if (!validateInput()) {
            return;
        }

        try {
            // Create profile change request
            ProfileChangeRequest request = new ProfileChangeRequest();
            request.setUserId(currentResident.getUserId());
            
            // Current values
            request.setCurrentUsername(currentResident.getUsername());
            request.setCurrentPhoneNumber(currentResident.getPhoneNumber());
            request.setCurrentEmail(currentResident.getEmail());
            request.setCurrentFullName(currentResident.getFullName());
            request.setCurrentRelationship(currentResident.getRelationship());
            request.setCurrentDateOfBirth(currentResident.getDateOfBirth() != null ? 
                new Date(currentResident.getDateOfBirth().getTime()) : null);
            request.setCurrentIdCardNumber(currentResident.getIdCardNumber());
            
            // New values
            request.setNewUsername(txtUsername.getText().trim());
            request.setNewPhoneNumber(txtPhoneNumber.getText().trim());
            request.setNewEmail(txtEmail.getText().trim());
            request.setNewFullName(txtFullName.getText().trim());
            request.setNewRelationship(cmbRelationship.getValue());
            request.setNewDateOfBirth(datePicker.getValue() != null ? 
                Date.valueOf(datePicker.getValue()) : null);
            request.setNewIdCardNumber(txtIdCardNumber.getText().trim());

            // Check if any changes were made
            if (!hasChanges(request)) {
                showAlert("Thông báo", "Bạn chưa thay đổi thông tin nào!", Alert.AlertType.INFORMATION);
                return;
            }

            boolean success = profileChangeRequestDAO.createProfileChangeRequest(request);
            
            if (success) {
                showAlert("Thành công", "Yêu cầu thay đổi thông tin đã được gửi thành công!\nAdmin sẽ xem xét và phê duyệt trong thời gian sớm nhất.", Alert.AlertType.INFORMATION);
                closeWindow();
            } else {
                showAlert("Lỗi", "Không thể gửi yêu cầu thay đổi thông tin. Vui lòng thử lại!", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Có lỗi xảy ra: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean hasChanges(ProfileChangeRequest request) {
        return !equals(request.getCurrentUsername(), request.getNewUsername()) ||
               !equals(request.getCurrentPhoneNumber(), request.getNewPhoneNumber()) ||
               !equals(request.getCurrentEmail(), request.getNewEmail()) ||
               !equals(request.getCurrentFullName(), request.getNewFullName()) ||
               !equals(request.getCurrentRelationship(), request.getNewRelationship()) ||
               !equals(request.getCurrentDateOfBirth(), request.getNewDateOfBirth()) ||
               !equals(request.getCurrentIdCardNumber(), request.getNewIdCardNumber());
    }

    private boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 == null || o2 == null) return false;
        return o1.equals(o2);
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (txtUsername.getText() == null || txtUsername.getText().trim().isEmpty()) {
            errors.append("- Tên đăng nhập không được để trống\n");
        }

        if (txtFullName.getText() == null || txtFullName.getText().trim().isEmpty()) {
            errors.append("- Họ tên không được để trống\n");
        }

        if (cmbRelationship.getValue() == null) {
            errors.append("- Vui lòng chọn quan hệ với chủ hộ\n");
        }

        if (txtEmail.getText() != null && !txtEmail.getText().trim().isEmpty() && !isValidEmail(txtEmail.getText().trim())) {
            errors.append("- Email không hợp lệ\n");
        }

        if (txtPhoneNumber.getText() != null && !txtPhoneNumber.getText().trim().isEmpty() && !isValidPhoneNumber(txtPhoneNumber.getText().trim())) {
            errors.append("- Số điện thoại không hợp lệ\n");
        }

        if (txtIdCardNumber.getText() != null && !txtIdCardNumber.getText().trim().isEmpty() && !isValidIdCard(txtIdCardNumber.getText().trim())) {
            errors.append("- Số CMND/CCCD không hợp lệ\n");
        }

        if (datePicker.getValue() != null && datePicker.getValue().isAfter(LocalDate.now())) {
            errors.append("- Ngày sinh không được trong tương lai\n");
        }

        if (errors.length() > 0) {
            showAlert("Lỗi nhập liệu", "Vui lòng sửa các lỗi sau:\n" + errors.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^[0-9]{10,11}$");
    }

    private boolean isValidIdCard(String idCard) {
        return idCard.matches("^[0-9]{9,12}$");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        // Check if button is attached to a scene
        if (btnCancel == null || btnCancel.getScene() == null) {
            System.out.println("DEBUG: Cannot close window - button not attached to scene yet");
            return;
        }
        
        // Check if we're in a Stage (popup window) or embedded in dashboard
        if (btnCancel.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            // Only close if it's not the main application window
            if (stage.getOwner() != null) {
                stage.close();
            }
        }
        // If embedded, we don't close anything - the dashboard will handle navigation
    }
}