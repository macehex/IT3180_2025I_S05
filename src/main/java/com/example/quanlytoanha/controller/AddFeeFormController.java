package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.FeeType;
import com.example.quanlytoanha.service.FeeTypeService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class AddFeeFormController {

    @FXML private Label lblTitle;
    @FXML private TextField txtName;
    @FXML private TextField txtUnitPrice;
    @FXML private TextField txtUnit;
    @FXML private TextArea txtDescription;

    @FXML private CheckBox chkIsDefault;
    @FXML private ComboBox<String> cmbPricingModel;
    // ------------------------------------

    private Stage dialogStage;
    private FeeTypeService feeTypeService;
    private boolean isEditMode = false;
    private FeeType editingFeeType;
    private boolean saveClicked = false;

    @FXML
    public void initialize() {
        this.feeTypeService = new FeeTypeService();


        cmbPricingModel.getItems().addAll("FIXED", "PER_SQM", "VOLUNTARY");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Controller chính gọi hàm này khi ở chế độ CHỈNH SỬA
     */
    public void setFeeToEdit(FeeType fee) {
        this.editingFeeType = fee;
        this.isEditMode = true;

        lblTitle.setText("Chỉnh sửa loại phí");
        txtName.setText(fee.getFeeName());
        txtUnitPrice.setText(fee.getUnitPrice().toString());
        txtUnit.setText(fee.getUnit());
        txtDescription.setText(fee.getDescription());
        chkIsDefault.setSelected(fee.isDefault());
        cmbPricingModel.setValue(fee.getPricingModel());
    }

    /**
     * Xử lý lưu
     */
    @FXML
    private void handleSave() {
        try {
            // 1. Validation (Xác thực dữ liệu)
            String name = txtName.getText();
            String priceStr = txtUnitPrice.getText();
            String pricingModel = cmbPricingModel.getValue(); // <-- Đọc giá trị mới

            if (name == null || name.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên phí không được để trống.");
                return;
            }
            if (priceStr == null || priceStr.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Đơn giá không được để trống.");
                return;
            }
            if (pricingModel == null) { // <-- Thêm validation
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn hình thức tính.");
                return;
            }

            BigDecimal unitPrice = new BigDecimal(priceStr);
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Đơn giá không được là số âm.");
                return;
            }
            boolean isDefault = chkIsDefault.isSelected(); // <-- Đọc giá trị mới

            // 2. Gọi Service
            if (isEditMode) {
                // Cập nhật đối tượng cũ
                editingFeeType.setFeeName(name);
                editingFeeType.setUnitPrice(unitPrice);
                editingFeeType.setUnit(txtUnit.getText());
                editingFeeType.setDescription(txtDescription.getText());
                editingFeeType.setDefault(isDefault); // <-- Set giá trị mới
                editingFeeType.setPricingModel(pricingModel); // <-- Set giá trị mới

                feeTypeService.updateFee(editingFeeType);
            } else {
                // Tạo đối tượng mới
                FeeType newFee = new FeeType();
                newFee.setFeeName(name);
                newFee.setUnitPrice(unitPrice);
                newFee.setUnit(txtUnit.getText());
                newFee.setDescription(txtDescription.getText());
                newFee.setDefault(isDefault); // <-- Set giá trị mới
                newFee.setPricingModel(pricingModel); // <-- Set giá trị mới

                feeTypeService.addFee(newFee);
            }
            this.saveClicked = true;

            // 3. Đóng cửa sổ
            dialogStage.close();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đơn giá phải là một con số hợp lệ.");
        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể lưu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }
}