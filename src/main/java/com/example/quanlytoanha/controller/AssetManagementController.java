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
import java.sql.SQLException;
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
        // 1. Lấy giá trị từ giao diện
        String keyword = txtSearch.getText();
        String status = cmbStatusFilter.getValue();

        // 2. Gọi Service để tìm kiếm
        try {
            List<Asset> filteredList = assetService.searchAssets(keyword, status);

            // 3. Cập nhật lại bảng
            assetTable.setItems(FXCollections.observableArrayList(filteredList));

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Lỗi khi lọc dữ liệu: " + e.getMessage()).showAndWait();
        }
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

    @FXML
    private void handleDeleteAsset() {
        // 1. Lấy tài sản đang chọn
        Asset selectedAsset = assetTable.getSelectionModel().getSelectedItem();

        if (selectedAsset == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một tài sản để xóa.").showAndWait();
            return;
        }

        // 2. Hiện hộp thoại xác nhận
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa tài sản này?");
        alert.setContentText("Tài sản: " + selectedAsset.getAssetType() + "\nTại: " + selectedAsset.getLocation());

        // Chờ người dùng bấm nút
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                // 3. Gọi Service xóa
                boolean deleted = assetService.deleteAsset(selectedAsset.getAssetId());

                if (deleted) {
                    new Alert(Alert.AlertType.INFORMATION, "Đã xóa tài sản thành công.").showAndWait();
                    // 4. Tải lại bảng dữ liệu
                    loadAssetsData();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Không thể xóa tài sản (Lỗi không xác định).").showAndWait();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                // Hiển thị thông báo lỗi chi tiết (bao gồm cả lỗi khóa ngoại đã bắt ở DAO)
                new Alert(Alert.AlertType.ERROR, "Lỗi xóa dữ liệu:\n" + e.getMessage()).showAndWait();
            }
        }
    }
}
