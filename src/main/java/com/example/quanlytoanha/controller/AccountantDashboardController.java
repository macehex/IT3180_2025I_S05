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

    // --- THÊM CÁC TRƯỜNG CỦA TAB 2 ---
    @FXML private TableView<FeeType> feeTable;
    @FXML private TableColumn<FeeType, String> colFeeName;
    @FXML private TableColumn<FeeType, BigDecimal> colFeePrice;
    @FXML private TableColumn<FeeType, String> colFeeUnit;
    @FXML private TableColumn<FeeType, String> colFeeDesc;

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
        try {
            // 1. Tải dữ liệu thống kê
            DebtReport report = financialService.generateDebtReport();

            // Định dạng số liệu (Ví dụ đơn giản)
            lblTotalDebt.setText(String.format("%,.0f VNĐ", report.getTotalDebtAmount()));
            lblTotalOverdue.setText(String.format("%,.0f VNĐ", report.getTotalOverdueAmount()));
            lblUnpaidInvoices.setText(report.getTotalUnpaidInvoices() + " hóa đơn");

            // 2. Tải dữ liệu chi tiết vào Bảng
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

        // 2. Đóng cửa sổ Dashboard hiện tại
        Stage currentStage = (Stage) btnLogout.getScene().getWindow();
        currentStage.close();

        // 3. Mở lại cửa sổ Login (copy code từ lớp Main.java)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Quản lý Tòa nhà - Đăng nhập");
            loginStage.setScene(new Scene(root, 450, 500));
            loginStage.setResizable(true);
            loginStage.setMinWidth(450);
            loginStage.setMinHeight(500);
            loginStage.setMaximized(true); // Set full screen
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleViewDetails(ApartmentDebt selectedDebt) {
        try {
            // 1. Lấy dữ liệu chi tiết từ service
            List<Invoice> invoiceList = financialService.getDetailedDebtForApartment(selectedDebt.getApartmentId());

            if (invoiceList.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không tìm thấy chi tiết hóa đơn chưa thanh toán cho căn hộ này.");
                return;
            }

            // 2. Tải FXML của cửa sổ chi tiết
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/debt_detail_view.fxml"));
            Parent root = loader.load();

            // 3. Lấy controller của cửa sổ mới
            DebtDetailController detailController = loader.getController();

            // 4. TRUYỀN DỮ LIỆU SANG
            detailController.setData(selectedDebt, invoiceList);

            // 5. Hiển thị cửa sổ mới (modal)
            Stage detailStage = new Stage();
            detailStage.setTitle("Chi tiết Công nợ");
            detailStage.initOwner((Stage) debtTable.getScene().getWindow()); // Đặt cửa sổ cha
            detailStage.setScene(new Scene(root));
            detailStage.showAndWait(); // Hiển thị và chờ

        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao diện", "Không thể tải cửa sổ chi tiết.");
        }
    }

    /**
     * Phương thức tiện ích để hiển thị thông báo Alert.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // Không có tiêu đề phụ
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- CÁC HÀM CỦA TAB 2 (QUẢN LÝ PHÍ) ---
    // (Những hàm này PHẢI nằm BÊN TRONG dấu ngoặc của lớp AccountantDashboardController)

    private void configureFeeTableColumns() {
        // Tên "feeName", "unitPrice" PHẢI TRÙNG KHỚP với tên thuộc tính
        // trong file FeeType.java của bạn.
        colFeeName.setCellValueFactory(new PropertyValueFactory<>("feeName"));
        colFeePrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colFeeUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colFeeDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    /**
     * Tải (hoặc tải lại) dữ liệu từ CSDL lên Bảng Quản lý Phí
     */
    private void loadFeeData() {
        try {
            // 1. Gọi Service để lấy dữ liệu
            List<FeeType> fees = feeTypeService.getAllFees();

            // 2. Xóa dữ liệu cũ và cập nhật dữ liệu mới lên TableView
            feeTable.getItems().setAll(fees);

        } catch (SecurityException e) {
            // Hàm showAlert đã có sẵn trong code của bạn
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể tải danh sách phí: " + e.getMessage());
            e.printStackTrace(); // In lỗi ra console để debug
        }
    }

    @FXML
    private void handleAddFee() {
        // Lấy kết quả (true/false) từ cửa sổ popup
        // SỬA LỖI TYPO: showFeeDailog -> showFeeEditDialog
        boolean saveClicked = showFeeEditDialog(null);

        // Chỉ tải lại bảng NẾU người dùng nhấn "Lưu"
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

        // Lấy kết quả (true/false) từ cửa sổ popup
        boolean saveClicked = showFeeEditDialog(selectedFee);

        // Chỉ tải lại bảng NẾU người dùng nhấn "Lưu"
        if (saveClicked) {
            loadFeeData();
        }
    }

    /**
     * Xử lý khi nhấn nút "Xóa/Hủy phí"
     */
    @FXML
    private void handleDeleteFee() {
        FeeType selectedFee = feeTable.getSelectionModel().getSelectedItem();

        if (selectedFee == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một loại phí để xóa/hủy.");
            return;
        }

        // Hiển thị hộp thoại xác nhận (Alert)
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc chắn muốn hủy loại phí này?");
        alert.setContentText(selectedFee.getFeeName());

        // Chờ người dùng bấm OK hoặc Cancel
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Gọi service để cập nhật CSDL
                    feeTypeService.deactivateFee(selectedFee.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy loại phí thành công.");
                    loadFeeData(); // Tải lại bảng
                } catch (SecurityException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", e.getMessage());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể hủy phí: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Mở cửa sổ popup (form) để Thêm hoặc Sửa phí.
     * @param fee Đối tượng FeeType cần sửa. Nếu là NULL, form sẽ ở chế độ THÊM MỚI.
     * @return true nếu người dùng nhấn "Lưu", false nếu nhấn "Hủy".
     */
    // --- ĐÂY LÀ PHIÊN BẢN ĐÃ SỬA LỖI HOÀN CHỈNH ---
    private boolean showFeeEditDialog(FeeType fee) {
        try {
            // 1. Tải FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/add_fee_form.fxml"));
            Parent root = loader.load();

            // 2. Lấy Controller của popup
            AddFeeFormController controller = loader.getController();

            // 3. Tạo Stage (cửa sổ) MỚI
            Stage dialogStage = new Stage();
            dialogStage.setTitle(fee == null ? "Thêm phí mới" : "Chỉnh sửa phí");
            dialogStage.initOwner((Stage) btnAddFee.getScene().getWindow()); // Đặt cửa sổ cha
            dialogStage.setScene(new Scene(root));

            // 4. Truyền dữ liệu
            controller.setDialogStage(dialogStage);
            if (fee != null) {
                controller.setFeeToEdit(fee); // Bật chế độ Edit
            }

            // 5. Hiển thị và chờ
            dialogStage.showAndWait();

            // 6. Hỏi popup xem "Lưu" đã được nhấn chưa?
            return controller.isSaveClicked();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao diện", "Không thể tải cửa sổ form.");
            return false;
        }
    }

} // <-- ĐÂY LÀ DẤU NGOẶC ĐÓNG LỚP CUỐI CÙNG VÀ DUY NHẤT