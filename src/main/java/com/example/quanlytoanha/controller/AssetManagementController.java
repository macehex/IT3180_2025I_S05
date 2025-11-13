// Vị trí: src/main/java/com/example/quanlytoanha/controller/AssetManagementController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Asset;
import com.example.quanlytoanha.service.AssetService;
import javafx.application.Platform; // <-- BỔ SUNG IMPORT
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList; // <-- BẮT BUỘC IMPORT
import java.util.Arrays;    // <-- BẮT BUỘC IMPORT
import java.util.List;      // <-- BẮT BUỘC IMPORT

public class AssetManagementController {

    @FXML private TableView<Asset> assetTable;
    @FXML private ComboBox<String> cmbStatusFilter; // <-- LỖI NẰM Ở ĐÂY
    @FXML private TextField txtSearch;

    private AssetService assetService;

    @FXML
    public void initialize() {
        // Khởi tạo Service an toàn
        this.assetService = new AssetService();

        // --- SỬA LỖI UnsupportedOperationException ---
        // 1. Tạo một ArrayList (có thể sửa đổi)
        List<String> filterList = new ArrayList<>(Arrays.asList(
                "Tất cả", "AVAILABLE", "IN_MAINTENANCE", "BROKEN", "IN_USE"
        ));
        // 2. Bọc danh sách đó
        cmbStatusFilter.setItems(FXCollections.observableArrayList(filterList));
        // --- KẾT THÚC SỬA LỖI ---

        cmbStatusFilter.setValue("Tất cả");

        // Tải dữ liệu ban đầu
        // Bọc trong Platform.runLater để tránh lỗi Alert khi CSDL gặp sự cố lúc khởi tạo
        Platform.runLater(this::loadAssetsData);
    }

    // Hàm load/refresh dữ liệu
    private void loadAssetsData() {
        try {
            List<Asset> assets = assetService.getAllAssets();
            assetTable.setItems(FXCollections.observableArrayList(assets));
        } catch (Exception e) {
            // Lỗi này giờ đã an toàn vì nằm trong runLater
            new Alert(Alert.AlertType.ERROR, "Lỗi khi tải dữ liệu tài sản: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleFilterAction() {
        // TODO: Triển khai logic lọc ở AssetService
        loadAssetsData();
    }

    @FXML
    private void handleCreateNewAsset() {
        showAssetForm(null); // Mở Form ở chế độ Tạo mới
    }

    @FXML
    private void handleEditAsset() {
        Asset selectedAsset = assetTable.getSelectionModel().getSelectedItem();
        if (selectedAsset != null) {
            showAssetForm(selectedAsset); // Mở Form ở chế độ Chỉnh sửa
        } else {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một tài sản để chỉnh sửa.").showAndWait();
        }
    }

    // Hàm này mở 'asset_form.fxml'
    private void showAssetForm(Asset assetToEdit) {
        try { // BẮT ĐẦU KHỐI TRY
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/asset_form.fxml"));
            Parent root = loader.load();

            AssetFormController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle(assetToEdit == null ? "Tạo Tài sản mới" : "Cập nhật Tài sản #" + assetToEdit.getAssetId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(assetTable.getScene().getWindow());
            stage.setResizable(false);

            controller.setAssetData(assetToEdit, this::loadAssetsData);

            stage.showAndWait();

        } catch (Exception e) { // <-- SỬA TỪ (IOException e) THÀNH (Exception e)
            e.printStackTrace();
            // BÂY GIỜ BẠN SẼ THẤY LỖI THẬT
            new Alert(Alert.AlertType.ERROR, "Không thể mở Form: " + e.getMessage()).showAndWait();
        }
    }
}
