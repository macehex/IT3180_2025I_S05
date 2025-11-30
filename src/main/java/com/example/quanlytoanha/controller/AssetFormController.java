// Vị trí: src/main/java/com/example/quanlytoanha/controller/AssetFormController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Asset;
import com.example.quanlytoanha.service.AssetService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AssetFormController {

    @FXML private Label lblTitle;
    @FXML private TextField txtAssetId;
    @FXML private TextField txtAssetType;
    @FXML private TextField txtLocation;
    @FXML private TextField txtInitialCost;
    @FXML private DatePicker dpPurchaseDate;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private TextArea txtDescription;
    @FXML private Button btnSave;
    @FXML private Button btnCancel; // (Đã thêm)

    private AssetService assetService;
    private Asset assetToEdit;
    private Runnable refreshCallback;

    @FXML
    public void initialize() {
        // Khởi tạo Service
        this.assetService = new AssetService();

        // Sửa lỗi ComboBox (Đã sửa ở lần trước)
        List<String> statusList = new ArrayList<>(Arrays.asList(
                "AVAILABLE", "IN_MAINTENANCE", "BROKEN", "IN_USE"
        ));
        cmbStatus.setItems(FXCollections.observableArrayList(statusList));
    }

    // Hàm này được gọi từ AssetManagementController
    public void setAssetData(Asset asset, Runnable callback) {
        this.assetToEdit = asset;
        this.refreshCallback = callback;

        if (assetToEdit != null) {
            // Chế độ Chỉnh sửa
            lblTitle.setText("CẬP NHẬT TÀI SẢN #" + assetToEdit.getAssetId());
            txtAssetId.setText(String.valueOf(asset.getAssetId()));
            txtAssetType.setText(asset.getAssetType());
            txtLocation.setText(asset.getLocation());
            txtInitialCost.setText(asset.getInitialCost() != null ? asset.getInitialCost().toString() : "");
            cmbStatus.setValue(asset.getStatus());
            txtDescription.setText(asset.getDescription());

            if (asset.getPurchaseDate() != null) {
                // 1. Lấy java.sql.Date (dưới dạng java.util.Date)
                java.util.Date sqlDate = asset.getPurchaseDate();

                // 2. Tạo một java.util.Date MỚI (thuần túy) từ time millis
                //    (Điều này "làm sạch" kiểu đối tượng, cho phép .toInstant() hoạt động)
                java.util.Date utilDate = new java.util.Date(sqlDate.getTime());

                // 3. Bây giờ .toInstant() sẽ hoạt động
                dpPurchaseDate.setValue(utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }

        } else {
            // Chế độ Tạo mới
            lblTitle.setText("TẠO HỒ SƠ TÀI SẢN MỚI");
            cmbStatus.setValue("AVAILABLE"); // Mặc định khi tạo mới
        }
    }

    @FXML
    private void handleSaveAction() {
        try {
            Asset asset = createAssetFromForm();
            int currentUserId = 1; // TODO: Lấy User ID của Ban Quản trị từ Session/Context

            boolean success = assetService.saveOrUpdateAsset(asset, currentUserId);

            if (success) {
                if (refreshCallback != null) {
                    refreshCallback.run(); // Gọi hàm refresh bảng danh sách
                }
                closeWindow();
            } else {
                new Alert(Alert.AlertType.ERROR, "Lưu dữ liệu thất bại. Vui lòng kiểm tra lại.").showAndWait();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Dữ liệu Chi phí không hợp lệ.").showAndWait();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi CSDL: " + e.getMessage()).showAndWait();
            e.printStackTrace();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Đã xảy ra lỗi: " + e.getMessage()).showAndWait();
        }
    }

    private Asset createAssetFromForm() {
        Asset asset = assetToEdit != null ? assetToEdit : new Asset();

        if (!txtAssetId.getText().isEmpty()) {
            asset.setAssetId(Integer.parseInt(txtAssetId.getText()));
        }
        asset.setAssetType(txtAssetType.getText().trim());
        asset.setLocation(txtLocation.getText().trim());
        asset.setStatus(cmbStatus.getValue());
        asset.setDescription(txtDescription.getText().trim());

        try {
            asset.setInitialCost(new BigDecimal(txtInitialCost.getText().trim()));
        } catch (Exception e) {
            throw new NumberFormatException("Chi phí không phải là số.");
        }

        if (dpPurchaseDate.getValue() != null) {
            Date date = Date.from(dpPurchaseDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            asset.setPurchaseDate(date);
        } else {
            asset.setPurchaseDate(null);
        }

        return asset;
    }

    @FXML
    private void handleCancelAction() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }
}