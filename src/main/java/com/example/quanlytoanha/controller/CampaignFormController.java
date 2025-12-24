package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.FeeType;
import com.example.quanlytoanha.service.FeeTypeService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CampaignFormController {

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtMinAmount;
    @FXML
    private DatePicker dpDueDate;
    @FXML
    private TextArea txtDescription;

    private Stage dialogStage;
    private FeeTypeService feeTypeService;

    // Kết quả trả về cho Dashboard
    private boolean isLaunched = false;
    private FeeType createdFee = null;
    private LocalDate selectedDueDate = null;
    private FeeType feeToEdit = null;
    private boolean isUpdateMode = false;

    @FXML
    public void initialize() {
        this.feeTypeService = new FeeTypeService();
        // Setup text field chỉ nhập số
        txtMinAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtMinAmount.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleLaunch() {
        if (isInputValid()) {
            try {
                if (isUpdateMode) {
                    // --- LOGIC CẬP NHẬT ---
                    feeToEdit.setFeeName(txtName.getText());
                    String amountStr = txtMinAmount.getText().isEmpty() ? "0" : txtMinAmount.getText();
                    feeToEdit.setUnitPrice(new BigDecimal(amountStr));
                    feeToEdit.setDescription(txtDescription.getText());

                    // Gọi Service update thông tin cơ bản
                    feeTypeService.updateFee(feeToEdit);

                    this.createdFee = feeToEdit; // Để Dashboard biết fee nào vừa sửa
                    this.selectedDueDate = dpDueDate.getValue();
                    this.isLaunched = true; // Đánh dấu là đã bấm nút OK

                    dialogStage.close();

                } else {
                    // --- LOGIC TẠO MỚI (CŨ) ---
                    FeeType newFee = new FeeType();
                    newFee.setFeeName(txtName.getText());
                    String amountStr = txtMinAmount.getText().isEmpty() ? "0" : txtMinAmount.getText();
                    newFee.setUnitPrice(new BigDecimal(amountStr));

                    newFee.setUnit(""); // <--- ĐÃ SỬA: Để trống đơn vị theo ý bạn

                    newFee.setDescription(txtDescription.getText());
                    newFee.setDefault(true);
                    newFee.setPricingModel("VOLUNTARY");

                    feeTypeService.addFee(newFee);
                    this.createdFee = feeTypeService.getLatestFee();
                    this.selectedDueDate = dpDueDate.getValue();
                    this.isLaunched = true;
                    dialogStage.close();
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xảy ra lỗi: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (txtName.getText() == null || txtName.getText().trim().isEmpty()) {
            errorMessage += "Chưa nhập tên đợt đóng góp!\n";
        }
        if (dpDueDate.getValue() == null) {
            errorMessage += "Chưa chọn ngày hết hạn!\n";
        } else if (dpDueDate.getValue().isBefore(LocalDate.now())) {
            errorMessage += "Ngày hết hạn không được ở quá khứ!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Thiếu thông tin", errorMessage);
            return false;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters để Dashboard lấy dữ liệu
    public boolean isLaunched() {
        return isLaunched;
    }

    public FeeType getCreatedFee() {
        return createdFee;
    }

    public LocalDate getSelectedDueDate() {
        return selectedDueDate;
    }

    /**
     * HÀM MỚI: Chuyển form sang chế độ chỉnh sửa
     */
    public void setCampaignToEdit(FeeType fee, LocalDate currentDueDate) {
        this.feeToEdit = fee;
        this.isUpdateMode = true;

        // Điền dữ liệu cũ
        txtName.setText(fee.getFeeName());
        txtMinAmount.setText(String.format("%.0f", fee.getUnitPrice()));
        txtDescription.setText(fee.getDescription());

        if (currentDueDate != null) {
            dpDueDate.setValue(currentDueDate);
        }
    }
}