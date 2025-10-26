// Vị trí: src/main/java/com/example/quanlytoanha/controller/AccountantDashboardController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.ApartmentDebt;
import com.example.quanlytoanha.model.DebtReport;
import com.example.quanlytoanha.model.FeeType;
import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.service.FeeTypeService;
import com.example.quanlytoanha.service.FinancialService;
// --- THAY ĐỔI 1: Đổi tên import ---
import com.example.quanlytoanha.service.InvoiceGenerationService; // Đổi tên từ InvoiceService
// ------------------------------
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
import java.time.LocalDate; // <-- THÊM IMPORT NÀY
import java.util.List;

public class AccountantDashboardController {

    // ... (Các @FXML không thay đổi) ...
    @FXML private Label lblTotalDebt;
    @FXML private Label lblTotalOverdue;
    @FXML private Label lblUnpaidInvoices;
    @FXML private Button btnLogout;
    @FXML private TableView<ApartmentDebt> debtTable;
    @FXML private TableView<FeeType> feeTable;
    @FXML private TableColumn<FeeType, String> colFeeName;
    @FXML private TableColumn<FeeType, BigDecimal> colFeePrice;
    @FXML private TableColumn<FeeType, String> colFeeUnit;
    @FXML private TableColumn<FeeType, String> colFeeDesc;
    @FXML private TableColumn<FeeType, Boolean> colFeeIsDefault;
    @FXML private TableColumn<FeeType, String> colFeePricingModel;
    @FXML private Button btnAddFee;
    @FXML private Button btnEditFee;
    @FXML private Button btnDeleteFee;
    @FXML private Button btnGenerateInvoices;
    @FXML private TextField txtBillingMonth;
    @FXML private TextField txtBillingYear;

    private FinancialService financialService;
    private FeeTypeService feeTypeService;
    // --- THAY ĐỔI 2: Đổi tên biến ---
    private InvoiceGenerationService invoiceGenerationService; // Đổi tên từ invoiceService
    // ----------------------------

    @FXML
    public void initialize() {
        this.financialService = new FinancialService();
        this.feeTypeService = new FeeTypeService();
        // --- THAY ĐỔI 3: Khởi tạo service mới ---
        this.invoiceGenerationService = new InvoiceGenerationService(); // Đổi tên
        // ------------------------------------
        loadDashboardData();

        configureFeeTableColumns();
        loadFeeData();

        // Gán sự kiện cho các nút
        btnAddFee.setOnAction(event -> handleAddFee());
        btnEditFee.setOnAction(event -> handleEditFee());
        btnDeleteFee.setOnAction(event -> handleDeleteFee());

        // --- GÁN SỰ KIỆN CHO NÚT TẠO HÓA ĐƠN ---
        btnGenerateInvoices.setOnAction(event -> handleGenerateInvoices()); // Đảm bảo dòng này tồn tại
        // -------------------------------------

        debtTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ApartmentDebt selectedDebt = debtTable.getSelectionModel().getSelectedItem();
                if (selectedDebt != null) {
                    handleViewDetails(selectedDebt);
                }
            }
        });
    }

    // ... (loadDashboardData, handleLogout, handleViewDetails, showAlert không thay đổi) ...
    @FXML
    private void loadDashboardData() {
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
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // --- CÁC HÀM CỦA TAB 2 (QUẢN LÝ PHÍ) ---
    // ... (configureFeeTableColumns, loadFeeData, handleAddFee, handleEditFee, handleDeleteFee, showFeeEditDialog không thay đổi) ...
    private void configureFeeTableColumns() {
        colFeeName.setCellValueFactory(new PropertyValueFactory<>("feeName"));
        colFeePrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colFeeUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colFeeDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colFeeIsDefault.setCellValueFactory(new PropertyValueFactory<>("default"));
        colFeePricingModel.setCellValueFactory(new PropertyValueFactory<>("pricingModel"));
    }
    private void loadFeeData() {
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
        boolean saveClicked = showFeeEditDialog(null);
        if (saveClicked) {
            loadFeeData();
        }
    }
    @FXML
    private void handleEditFee() {
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
                    feeTypeService.deactivateFee(selectedFee.getFeeId());
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

    // --- HÀM MỚI CHO TAB 3 (TẠO HÓA ĐƠN) ---

    @FXML
    private void handleGenerateInvoices() {
        String monthStr = txtBillingMonth.getText();
        String yearStr = txtBillingYear.getText();

        // 1. Validation (Kiểm tra đầu vào)
        int month, year;
        try {
            month = Integer.parseInt(monthStr);
            year = Integer.parseInt(yearStr);

            if (month < 1 || month > 12) {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Tháng phải là số từ 1 đến 12.");
                return;
            }
            if (year < 2000 || year > 2100) { // Giới hạn năm hợp lý
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Năm không hợp lệ.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Tháng và Năm phải là số.");
            return;
        }

        // 2. Tạo ngày đầu tiên của tháng
        LocalDate billingMonth = LocalDate.of(year, month, 1);

        // 3. Hiển thị xác nhận (Giống như cũ)
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Bạn sắp tạo hóa đơn cho tháng " + billingMonth.getMonthValue() + "/" + billingMonth.getYear());
        confirmAlert.setContentText("Hệ thống sẽ tính toán phí cho TẤT CẢ căn hộ.\nQuá trình này có thể mất vài phút.\nBạn có chắc chắn muốn tiếp tục?");
        confirmAlert.getDialogPane().setMinHeight(150);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String result = invoiceGenerationService.generateMonthlyInvoices(billingMonth);
                    showAlert(Alert.AlertType.INFORMATION, "Hoàn thành", result);
                    loadDashboardData(); // Tải lại Tab 1
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi nghiêm trọng", "Đã xảy ra lỗi khi tạo hóa đơn: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

} // <-- Dấu ngoặc cuối cùng của lớp