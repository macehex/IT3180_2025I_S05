// Vị trí: src/main/java/com/example/quanlytoanha/controller/MaintenanceHistoryController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Asset;
import com.example.quanlytoanha.model.Maintenance;
import com.example.quanlytoanha.service.AssetService;
import com.example.quanlytoanha.service.MaintenanceService;
import com.example.quanlytoanha.session.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date; // <-- BẮT BUỘC IMPORT
import java.util.List;
import java.util.Optional; // <-- BẮT BUỘC IMPORT

public class MaintenanceHistoryController {

    @FXML private TableView<Maintenance> maintenanceTable;
    @FXML private ComboBox<Asset> cmbAssetList;
    @FXML private DatePicker dpScheduledDate;
    @FXML private TextField txtScheduleDescription;
    @FXML private Button btnMarkAsCompleted;

    private MaintenanceService maintenanceService;
    private AssetService assetService;

    @FXML
    public void initialize() {
        this.maintenanceService = new MaintenanceService();
        this.assetService = new AssetService(); // Dùng để lấy danh sách tài sản

        // 1. Tải danh sách tài sản vào ComboBox
        loadAssetComboBox();

        // 2. Tải bảng lịch sử
        Platform.runLater(this::loadMaintenanceTable);

        // 3. Logic bật/tắt nút "Ghi nhận Hoàn thành"
        maintenanceTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    // Chỉ bật nút khi chọn 1 dòng và dòng đó có status là PENDING
                    btnMarkAsCompleted.setDisable(newSelection == null || !"PENDING".equals(newSelection.getStatus()));
                }
        );
    }

    private void loadMaintenanceTable() {
        try {
            List<Maintenance> history = maintenanceService.getAllMaintenanceHistory();
            maintenanceTable.setItems(FXCollections.observableArrayList(history));
            btnMarkAsCompleted.setDisable(true);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Dữ liệu", "Không thể tải lịch sử bảo trì.");
            e.printStackTrace();
        }
    }

    private void loadAssetComboBox() {
        try {
            List<Asset> assets = assetService.getAllAssets();
            cmbAssetList.setItems(FXCollections.observableArrayList(assets));

            // Cấu hình cách hiển thị tên trong ComboBox
            cmbAssetList.setConverter(new StringConverter<Asset>() {
                @Override
                public String toString(Asset asset) {
                    return asset == null ? null : asset.getAssetType() + " (ID: " + asset.getAssetId() + ")";
                }
                @Override
                public Asset fromString(String string) {
                    return null; // Không cần thiết
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleScheduleMaintenance() {
        Asset selectedAsset = cmbAssetList.getValue();
        if (selectedAsset == null || dpScheduledDate.getValue() == null || txtScheduleDescription.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn Tài sản, Ngày dự kiến và Mô tả.");
            return;
        }

        try {
            Maintenance newMaintenance = new Maintenance();
            newMaintenance.setAssetId(selectedAsset.getAssetId());
            newMaintenance.setDescription(txtScheduleDescription.getText());
            newMaintenance.setScheduledDate(Date.from(dpScheduledDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            newMaintenance.setCreatedByUserId(SessionManager.getInstance().getCurrentUser().getUserId()); // Lấy ID người dùng

            boolean success = maintenanceService.scheduleMaintenance(newMaintenance);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lập lịch bảo trì thành công.");
                loadMaintenanceTable(); // Tải lại bảng
                // Xóa form
                cmbAssetList.setValue(null);
                dpScheduledDate.setValue(null);
                txtScheduleDescription.clear();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Không thể lập lịch bảo trì.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenCompleteForm() {
        Maintenance selected = maintenanceTable.getSelectionModel().getSelectedItem();
        if (selected == null || !"PENDING".equals(selected.getStatus())) {
            // (Đã được xử lý bởi logic disable nút, nhưng kiểm tra lại cho an toàn)
            return;
        }

        // --- BỔ SUNG: Kiểm tra logic hoàn thành sớm ---
        Date today = new Date();
        Date scheduledDate = selected.getScheduledDate();

        if (scheduledDate != null && today.before(scheduledDate)) {
            // Nếu ngày hôm nay TRƯỚC ngày dự kiến -> Cảnh báo
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Xác nhận Ghi nhận Sớm");
            confirmation.setHeaderText("Công việc này được lập lịch cho ngày: " + scheduledDate);
            confirmation.setContentText("Bạn có chắc chắn muốn ghi nhận hoàn thành SỚM HƠN DỰ KIẾN không?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return; // Người dùng nhấn Cancel
            }
        }
        // --- KẾT THÚC BỔ SUNG ---

        // (Tiếp tục logic cũ: Dùng Dialog đơn giản)
        TextInputDialog costDialog = new TextInputDialog("0");
        costDialog.setTitle("Ghi nhận Hoàn thành");
        costDialog.setHeaderText("Cập nhật chi phí cho: " + selected.getAssetName());
        costDialog.setContentText("Chi phí (VNĐ):");

        Optional<String> costResult = costDialog.showAndWait();

        if (costResult.isPresent()) {
            try {
                selected.setCost(new java.math.BigDecimal(costResult.get()));
                selected.setMaintenanceDate(new Date()); // Ghi nhận ngày hôm nay
                selected.setPerformedBy("Ban Quản trị"); // (Tạm thời)

                maintenanceService.completeMaintenance(selected);

                // Cập nhật trạng thái tài sản (Quan trọng)
                // (Chỉ cập nhật nếu tài sản không còn Hư hỏng)
                assetService.updateAssetStatusOnly(selected.getAssetId(), "AVAILABLE",
                        SessionManager.getInstance().getCurrentUser().getUserId(),
                        "Hoàn thành bảo trì #" + selected.getMaintenanceId());

                loadMaintenanceTable(); // Tải lại
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi cập nhật", "Không thể ghi nhận hoàn thành.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}