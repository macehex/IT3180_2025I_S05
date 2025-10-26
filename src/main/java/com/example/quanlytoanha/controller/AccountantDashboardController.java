// Vị trí: src/main/java/com/example/quanlytoanha/controller/AccountantDashboardController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.ApartmentDebt;
import com.example.quanlytoanha.model.DebtReport;
import com.example.quanlytoanha.model.FeeType;
import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.service.FeeTypeService;
import com.example.quanlytoanha.service.FinancialService;
import com.example.quanlytoanha.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class AccountantDashboardController {

    // Thống kê
    @FXML private Label lblTotalDebt;
    @FXML private Label lblTotalOverdue;
    @FXML private Label lblUnpaidInvoices;

    @FXML private Button btnLogout;
    // Bảng chi tiết
    @FXML private TableView<ApartmentDebt> debtTable;

    // --- CÁC TRƯỜNG CỦA TAB 2 ---
    @FXML private TableView<FeeType> feeTable;
    @FXML private TableColumn<FeeType, String> colFeeName;
    @FXML private TableColumn<FeeType, BigDecimal> colFeePrice;
    @FXML private TableColumn<FeeType, String> colFeeUnit;
    @FXML private TableColumn<FeeType, String> colFeeDesc;

    // --- CÁC CỘT MỚI ĐÃ THÊM ---
    @FXML private TableColumn<FeeType, Boolean> colFeeIsDefault; // Mới
    @FXML private TableColumn<FeeType, String> colFeePricingModel; // Mới
    // -------------------------

    @FXML private Button btnAddFee;
    @FXML private Button btnEditFee;
    @FXML private Button btnDeleteFee;
    // ---------------------------------

    private FinancialService financialService;
    private FeeTypeService feeTypeService;

    @FXML
    public void initialize() {
        this.financialService = new FinancialService();
        this.feeTypeService = new FeeTypeService();
        loadDashboardData();

        configureFeeTableColumns(); // <-- Gọi hàm helper mới
        loadFeeData(); // <-- Gọi hàm load dữ liệu mới

        // Gán sự kiện cho các nút
        btnAddFee.setOnAction(event -> handleAddFee());
        btnEditFee.setOnAction(event -> handleEditFee());
        btnDeleteFee.setOnAction(event -> handleDeleteFee());

        debtTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Chỉ xử lý khi click đúp
                ApartmentDebt selectedDebt = debtTable.getSelectionModel().getSelectedItem();
                if (selectedDebt != null) {
                    handleViewDetails(selectedDebt);
                }
            }
        });
    }

    @FXML
    private void loadDashboardData() {
        // (Code không thay đổi)
        try {
            DebtReport report = financialService.generateDebtReport();
            lblTotalDebt.setText(String.format("%,.0f VNĐ", report.getTotalDebtAmount()));
            lblTotalOverdue.setText(String.format("%,.0f VNĐ", report.getTotalOverdueAmount()));
            lblUnpaidInvoices.setText(report.getTotalUnpaidInvoices() + " hóa đơn");

            List<ApartmentDebt> debtList = financialService.getDetailedDebtList();
            debtTable.getItems().setAll(debtList);

        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", "Bạn không có quyền xem thông tin này.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể tải dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        // (Code không thay đổi)
        SessionManager.getInstance().logout();
        Stage currentStage = (Stage) btnLogout.getScene().getWindow();
        currentStage.close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Quản lý Tòa nhà - Đăng nhập");
            loginStage.setScene(new Scene(root, 400, 300));
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleViewDetails(ApartmentDebt selectedDebt) {
        // (Code không thay đổi)
        try {
            List<Invoice> invoiceList = financialService.getDetailedDebtForApartment(selectedDebt.getApartmentId());
            if (invoiceList.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không tìm thấy chi tiết hóa đơn chưa thanh toán cho căn hộ này.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/debt_detail_view.fxml"));
            Parent root = loader.load();
            DebtDetailController detailController = loader.getController();
            detailController.setData(selectedDebt, invoiceList);
            Stage detailStage = new Stage();
            detailStage.setTitle("Chi tiết Công nợ");
            detailStage.initOwner((Stage) debtTable.getScene().getWindow());
            detailStage.setScene(new Scene(root));
            detailStage.showAndWait();

        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao diện", "Không thể tải cửa sổ chi tiết.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        // (Code không thay đổi)
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- CÁC HÀM CỦA TAB 2 (QUẢN LÝ PHÍ) ---

    /**
     * Cấu hình các cột (ĐÃ CẬP NHẬT)
     */
    private void configureFeeTableColumns() {
        // Tên thuộc tính ("feeName", "unitPrice") phải khớp với FeeType.java
        colFeeName.setCellValueFactory(new PropertyValueFactory<>("feeName"));
        colFeePrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colFeeUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colFeeDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        // --- CÁC DÒNG MỚI ĐÃ THÊM ---
        // "default" khớp với hàm isDefault()
        // "pricingModel" khớp với hàm getPricingModel()
        colFeeIsDefault.setCellValueFactory(new PropertyValueFactory<>("default"));
        colFeePricingModel.setCellValueFactory(new PropertyValueFactory<>("pricingModel"));
    }

    /**
     * Tải (hoặc tải lại) dữ liệu
     */
    private void loadFeeData() {
        // (Code không thay đổi)
        try {
            List<FeeType> fees = feeTypeService.getAllFees();
            feeTable.getItems().setAll(fees);
        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể tải danh sách phí: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddFee() {
        // (Code không thay đổi)
        boolean saveClicked = showFeeEditDialog(null);
        if (saveClicked) {
            loadFeeData();
        }
    }

    @FXML
    private void handleEditFee() {
        // (Code không thay đổi)
        FeeType selectedFee = feeTable.getSelectionModel().getSelectedItem();
        if (selectedFee == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một loại phí để chỉnh sửa.");
            return;
        }
        boolean saveClicked = showFeeEditDialog(selectedFee);
        if (saveClicked) {
            loadFeeData();
        }
    }

    @FXML
    private void handleDeleteFee() {
        // (Code không thay đổi, đã sửa lỗi dùng getFeeId())
        FeeType selectedFee = feeTable.getSelectionModel().getSelectedItem();
        if (selectedFee == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một loại phí để xóa/hủy.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc chắn muốn hủy loại phí này?");
        alert.setContentText(selectedFee.getFeeName());
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    feeTypeService.deactivateFee(selectedFee.getFeeId()); // Dùng getFeeId()
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy loại phí thành công.");
                    loadFeeData();
                } catch (SecurityException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", e.getMessage());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể hủy phí: " + e.getMessage());
                }
            }
        });
    }

    private boolean showFeeEditDialog(FeeType fee) {
        // (Code không thay đổi)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/add_fee_form.fxml"));
            Parent root = loader.load();
            AddFeeFormController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(fee == null ? "Thêm phí mới" : "Chỉnh sửa phí");
            dialogStage.initOwner((Stage) btnAddFee.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            controller.setDialogStage(dialogStage);
            if (fee != null) {
                controller.setFeeToEdit(fee);
            }
            dialogStage.showAndWait();
            return controller.isSaveClicked();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao diện", "Không thể tải cửa sổ form.");
            return false;
        }
    }

}