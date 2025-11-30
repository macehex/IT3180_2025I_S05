package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.dao.ServiceRequestDAO;
import com.example.quanlytoanha.model.ServiceRequest;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser; // Import để mở cửa sổ chọn file
import javafx.stage.Stage;

import java.io.File; // Import để xử lý file
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CreateServiceRequestController implements Initializable {

    @FXML
    private ComboBox<RequestTypeItem> reqTypeComboBox;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Button submitButton;

    @FXML
    private Button cancelButton;

    // MỚI: Cần khai báo nút này để đổi tên nút sau khi chọn ảnh & lấy Stage
    @FXML
    private Button chooseImageButton;

    private int currentResidentId;
    private ServiceRequestDAO serviceRequestDAO;

    // MỚI: Biến để lưu file ảnh đã chọn
    private File selectedImageFile = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceRequestDAO = new ServiceRequestDAO();
        setupRequestTypeComboBox();
    }

    /**
     * Định nghĩa các lựa chọn cho ComboBox loại yêu cầu
     */
    private void setupRequestTypeComboBox() {
        ObservableList<RequestTypeItem> requestTypes = FXCollections.observableArrayList(
                new RequestTypeItem("Sửa chữa / Bảo trì", "SUA_CHUA"),
                new RequestTypeItem("An ninh", "AN_NINH"),
                new RequestTypeItem("Gửi xe", "GUI_XE"),
                new RequestTypeItem("Phàn nàn / Khiếu nại", "PHAN_NAN"),
                new RequestTypeItem("Yêu cầu khác", "KHAC")
        );

        reqTypeComboBox.setItems(requestTypes);

        // Đặt giá trị mặc định (ví dụ: 'KHAC')
        for (RequestTypeItem item : requestTypes) {
            if (item.getDbValue().equals("KHAC")) {
                reqTypeComboBox.setValue(item);
                break;
            }
        }
    }

    /**
     * Xử lý khi nhấn nút "Chọn ảnh..."
     */
    @FXML
    private void handleChooseImageButton() {
        // 1. Tạo FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn hình ảnh minh chứng");

        // 2. Chỉ cho phép chọn file ảnh (png, jpg, jpeg)
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        // 3. Lấy Stage hiện tại để hiển thị cửa sổ con lên trên nó
        // Lưu ý: Đảm bảo fx:id="chooseImageButton" đã có trong file FXML
        Stage stage = (Stage) chooseImageButton.getScene().getWindow();

        // 4. Hiển thị dialog và chờ người dùng chọn
        File file = fileChooser.showOpenDialog(stage);

        // 5. Nếu người dùng đã chọn file (không bấm Cancel)
        if (file != null) {
            this.selectedImageFile = file;

            // Cập nhật text của nút bấm thành tên file để người dùng biết đã chọn
            chooseImageButton.setText(file.getName());

            System.out.println("Đã chọn file: " + file.getAbsolutePath());
        }
    }

    /**
     * Xử lý khi nhấn nút "Gửi Yêu Cầu"
     */
    @FXML
    private void handleSubmitButton() {
        // 1. Thu thập dữ liệu
        RequestTypeItem selectedType = reqTypeComboBox.getSelectionModel().getSelectedItem();
        String title = titleField.getText();
        String description = descriptionArea.getText();

        // 2. Kiểm tra (Validate) dữ liệu
        if (selectedType == null || title.trim().isEmpty() || description.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đầy đủ các trường bắt buộc (*).");
            return;
        }

        // 3. Lấy giá trị ENUM cho DB
        String reqTypeDbValue = selectedType.getDbValue();

        try {
            // 4. Tạo đối tượng ServiceRequest mới
            ServiceRequest newRequest = new ServiceRequest();
            newRequest.setReqUserId(currentResidentId);
            newRequest.setReqType(reqTypeDbValue);
            newRequest.setReqTitle(title);
            newRequest.setDescription(description);
            newRequest.setStatus("PENDING");
            newRequest.setCreatedAt(Date.valueOf(LocalDate.now()));

            // MỚI: Lưu đường dẫn ảnh nếu có
            if (selectedImageFile != null) {
                newRequest.setImageUrl(selectedImageFile.getAbsolutePath());
            } else {
                newRequest.setImageUrl(null);
            }

            // 5. Gọi DAO để lưu
            serviceRequestDAO.createServiceRequest(newRequest);

            // 6. Thông báo thành công
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi yêu cầu của bạn thành công.");

            // Đóng cửa sổ (stage) hiện tại
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    /**
     * Hàm helper để hiển thị Alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Hàm này được gọi từ ResidentDashboardController để truyền ID cư dân vào
     */
    public void setCurrentResidentId(int currentResidentId) {
        this.currentResidentId = currentResidentId;
    }

    @FXML
    private void handleCancelButton() {
        // Clear the form
        clearForm();
    }

    private void clearForm() {
        reqTypeComboBox.setValue(null);
        titleField.clear();
        descriptionArea.clear();
        selectedImageFile = null;
        chooseImageButton.setText("Chọn ảnh...");
        // Clear image preview if exists
        if (chooseImageButton.getScene().lookup("#imagePreview") != null) {
            javafx.scene.image.ImageView imagePreview = (javafx.scene.image.ImageView) chooseImageButton.getScene().lookup("#imagePreview");
            imagePreview.setImage(null);
        }
    }

    // =======================================================================
    // Class nội bộ để quản lý ComboBox
    // =======================================================================

    public static class RequestTypeItem {
        private String displayValue;
        private String dbValue;

        public RequestTypeItem(String displayValue, String dbValue) {
            this.displayValue = displayValue;
            this.dbValue = dbValue;
        }

        public String getDisplayValue() {
            return displayValue;
        }

        public String getDbValue() {
            return dbValue;
        }

        @Override
        public String toString() {
            return displayValue; // Chỉ hiển thị văn bản thân thiện
        }
    }
}