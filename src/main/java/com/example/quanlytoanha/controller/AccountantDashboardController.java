package com.example.quanlytoanha.controller;

// --- THÊM CÁC IMPORT CẦN THIẾT ---
import com.example.quanlytoanha.model.Role; // Đảm bảo bạn có Enum/Class Role
import com.example.quanlytoanha.service.NotificationService; // Import Service mới
import java.sql.SQLException; // Import SQLException
import java.util.ArrayList; // Import ArrayList
// ------------------------------------

import com.example.quanlytoanha.model.ApartmentDebt;
import com.example.quanlytoanha.model.DebtReport;
import com.example.quanlytoanha.model.FeeType;
import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.service.FeeTypeService;
import com.example.quanlytoanha.service.FinancialService;
import com.example.quanlytoanha.service.InvoiceGenerationService;
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
import java.time.LocalDate;
import java.util.List;

public class AccountantDashboardController {

    // --- KHAI BÁO @FXML ---
    // (Các @FXML cũ không thay đổi)
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
    @FXML private TextField txtBillingMonth;
    @FXML private TextField txtBillingYear;
    @FXML private Button btnGenerateInvoices;
    @FXML private Button btnRecalculateInvoices;

    // --- THÊM @FXML CHO TAB 3 (TỰ ĐỘNG HÓA) ---
    @FXML private CheckBox chkAutoSendNewInvoice;
    @FXML private CheckBox chkAutoSendReminder;
    @FXML private TextField txtDaysBefore;
    @FXML private CheckBox chkAutoSendOverdue;
    @FXML private Button btnSendManualReminder;
    @FXML private TextArea txtAutomationLog;
    // -----------------------------------------

    @FXML private Button btnSendSingleReminder;

    private FinancialService financialService;
    private FeeTypeService feeTypeService;
    private InvoiceGenerationService invoiceGenerationService;
    // --- KHAI BÁO SERVICE MỚI ---
    private NotificationService notificationService;
    // ---------------------------

    @FXML
    public void initialize() {
        this.financialService = new FinancialService();
        this.feeTypeService = new FeeTypeService();
        this.invoiceGenerationService = new InvoiceGenerationService();
        // --- KHỞI TẠO SERVICE MỚI ---
        this.notificationService = new NotificationService();
        // ---------------------------

        loadDashboardData();
        configureFeeTableColumns();
        loadFeeData();
        applyUIStyles();

        // Gán sự kiện cho các nút
        btnAddFee.setOnAction(event -> handleAddFee());
        btnEditFee.setOnAction(event -> handleEditFee());
        btnDeleteFee.setOnAction(event -> handleDeleteFee());
        btnGenerateInvoices.setOnAction(event -> handleGenerateInvoices());
        btnRecalculateInvoices.setOnAction(event -> handleRecalculateInvoices());

        // --- GÁN SỰ KIỆN CHO NÚT NHẮC NỢ ---
        btnSendManualReminder.setOnAction(event -> handleSendManualReminders());
        // ---------------------------------

        btnSendSingleReminder.setOnAction(event -> handleSendSingleReminder());

        debtTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ApartmentDebt selectedDebt = debtTable.getSelectionModel().getSelectedItem();
                if (selectedDebt != null) {
                    handleViewDetails(selectedDebt);
                }
            }
        });
    }

    // ... (Các hàm loadDashboardData, handleLogout, handleViewDetails, showAlert,
    //      configureFeeTableColumns, loadFeeData, handleAddFee, handleEditFee,
    //      handleDeleteFee, showFeeEditDialog, handleGenerateInvoices,
    //      handleRecalculateInvoices không thay đổi) ...
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
            loginStage.setScene(new Scene(root, 800, 600));
            loginStage.setMaximized(true);
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
    @FXML
    private void handleGenerateInvoices() {
        String monthStr = txtBillingMonth.getText();
        String yearStr = txtBillingYear.getText();
        int month, year;
        try {
            month = Integer.parseInt(monthStr);
            year = Integer.parseInt(yearStr);
            if (month < 1 || month > 12) {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Tháng phải là số từ 1 đến 12.");
                return;
            }
            if (year < 2000 || year > 2100) {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Năm không hợp lệ.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Tháng và Năm phải là số.");
            return;
        }
        LocalDate billingMonth = LocalDate.of(year, month, 1);
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
                    loadDashboardData();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi nghiêm trọng", "Đã xảy ra lỗi khi tạo hóa đơn: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    @FXML
    private void handleRecalculateInvoices() {
        String monthStr = txtBillingMonth.getText();
        String yearStr = txtBillingYear.getText();
        int month, year;
        try {
            month = Integer.parseInt(monthStr);
            year = Integer.parseInt(yearStr);
            if (month < 1 || month > 12) {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Tháng phải là số từ 1 đến 12.");
                return;
            }
            if (year < 2000 || year > 2100) {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Năm không hợp lệ.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Tháng và Năm phải là số.");
            return;
        }
        LocalDate billingMonth = LocalDate.of(year, month, 1);
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Bạn sắp TÍNH TOÁN LẠI hóa đơn cho tháng " + billingMonth.getMonthValue() + "/" + billingMonth.getYear());
        confirmAlert.setContentText("Hệ thống sẽ XÓA chi tiết cũ và tính lại phí cho TẤT CẢ căn hộ dựa trên cấu hình phí HIỆN TẠI.\nChỉ thực hiện nếu bạn chắc chắn muốn cập nhật lại hóa đơn cho tháng này.\nBạn có chắc chắn muốn tiếp tục?");
        confirmAlert.getDialogPane().setMinHeight(180);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String result = invoiceGenerationService.recalculateMonthlyInvoices(billingMonth);
                    showAlert(Alert.AlertType.INFORMATION, "Hoàn thành", result);
                    loadDashboardData();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi nghiêm trọng", "Đã xảy ra lỗi khi tính toán lại: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // --- HÀM MỚI CHO TAB 3 (GỬI NHẮC NỢ) ---

    @FXML
    private void handleSendManualReminders() {
        List<String> allLogs = new ArrayList<>();
        try {
            // Kiểm tra xem checkbox "Nhắc sắp đến hạn" có được chọn không
            if (chkAutoSendReminder.isSelected()) {
                // Kiểm tra txtDaysBefore có giá trị hợp lệ
                if(txtDaysBefore.getText() == null || txtDaysBefore.getText().trim().isEmpty()){
                    showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập số ngày báo trước hạn.");
                    return;
                }
                int daysBefore = Integer.parseInt(txtDaysBefore.getText());
                if(daysBefore <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Không hợp lệ", "Số ngày báo trước phải lớn hơn 0.");
                    return;
                }
                List<String> upcomingLogs = notificationService.sendUpcomingReminders(daysBefore);
                allLogs.addAll(upcomingLogs);
            }

            // Kiểm tra xem checkbox "Nhắc quá hạn" có được chọn không
            if (chkAutoSendOverdue.isSelected()) {
                List<String> overdueLogs = notificationService.sendOverdueNotifications();
                allLogs.addAll(overdueLogs);
            }

            // Hiển thị kết quả log vào TextArea
            updateLogDisplay(allLogs);

            if (allLogs.isEmpty()){
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không có thông báo nào cần gửi dựa trên cấu hình hiện tại.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Hoàn thành", "Đã gửi xong thông báo nhắc nợ. Xem chi tiết trong nhật ký.");
            }


        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Số ngày báo trước phải là một con số nguyên.");
        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể gửi thông báo: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) { // Bắt các lỗi khác
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Đã xảy ra lỗi không xác định khi gửi thông báo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Hàm helper để cập nhật TextArea log
    private void updateLogDisplay(List<String> logMessages) {
        StringBuilder logContent = new StringBuilder();
        // Giới hạn số lượng dòng log hiển thị (ví dụ: 100 dòng gần nhất)
        int maxLogLines = 100;

        // Thêm log mới vào đầu
        for (String msg : logMessages) {
            logContent.append(msg).append("\n");
        }

        // Lấy log cũ (nếu có)
        String oldLog = txtAutomationLog.getText();
        if (oldLog != null && !oldLog.isEmpty()) {
            logContent.append("---------------------\n"); // Thêm phân cách
            logContent.append(oldLog);
        }

        // Cắt bớt nếu log quá dài
        String[] lines = logContent.toString().split("\n");
        if (lines.length > maxLogLines) {
            StringBuilder truncatedLog = new StringBuilder();
            for (int i = 0; i < maxLogLines; i++) {
                truncatedLog.append(lines[i]).append("\n");
            }
            txtAutomationLog.setText(truncatedLog.toString());
        } else {
            txtAutomationLog.setText(logContent.toString());
        }
        txtAutomationLog.setScrollTop(0); // Cuộn lên đầu
    }

    /**
     * Xử lý sự kiện nhấn nút "Gửi nhắc nợ riêng" (từ Tab 1)
     */
    @FXML
    private void handleSendSingleReminder() {
        System.out.println("DEBUG: Entering handleSendSingleReminder"); // Thêm dòng debug này
        ApartmentDebt selectedDebt = debtTable.getSelectionModel().getSelectedItem();
        if (selectedDebt == null) {
            System.out.println("DEBUG: No apartment selected."); // Thêm dòng debug
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một căn hộ trong bảng Công nợ để gửi nhắc nợ.");
            return;
        }

        // Lấy ownerUserId (đã thêm vào ApartmentDebt)
        int recipientUserId = selectedDebt.getOwnerUserId();
        System.out.println("DEBUG: Selected apartmentId=" + selectedDebt.getApartmentId() + ", ownerUserId=" + recipientUserId); // Thêm dòng debug

        if (recipientUserId <= 0) { // Kiểm tra user ID hợp lệ
            System.out.println("DEBUG: Invalid ownerUserId."); // Thêm dòng debug
            showAlert(Alert.AlertType.ERROR, "Lỗi Dữ liệu", "Không tìm thấy thông tin người dùng hợp lệ cho căn hộ này.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận gửi");
        confirmAlert.setHeaderText("Gửi thông báo nhắc nợ riêng cho:");
        confirmAlert.setContentText("Căn hộ: " + selectedDebt.getApartmentId() + " - Chủ hộ: " + selectedDebt.getOwnerName());

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    System.out.println("DEBUG: Calling notificationService.sendSingleReminder..."); // Thêm dòng debug
                    boolean success = notificationService.sendSingleReminder(recipientUserId, selectedDebt);
                    if (success) {
                        System.out.println("DEBUG: sendSingleReminder successful."); // Thêm dòng debug
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi thông báo nhắc nợ thành công.");
                    } else {
                        System.out.println("DEBUG: sendSingleReminder failed (returned false)."); // Thêm dòng debug
                        showAlert(Alert.AlertType.ERROR, "Thất bại", "Không thể gửi thông báo (Service trả về false).");
                    }
                } catch (SecurityException e) {
                    System.err.println("DEBUG: SecurityException: " + e.getMessage()); // Thêm dòng debug
                    showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", e.getMessage());
                } catch (SQLException e) {
                    System.err.println("DEBUG: SQLException: " + e.getMessage()); // Thêm dòng debug
                    showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể gửi thông báo: " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    System.err.println("DEBUG: General Exception: " + e.getMessage()); // Thêm dòng debug
                    showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Đã xảy ra lỗi không xác định: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("DEBUG: User cancelled confirmation alert."); // Thêm dòng debug
            }
        });
    }

    /**
     * Áp dụng styles cho các components UI
     */
    private void applyUIStyles() {
        // Style cho nút đăng xuất với hiệu ứng hover
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle("-fx-background-color: #b71c1c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 16; -fx-effect: dropshadow(gaussian, rgba(211,47,47,0.3), 10, 0, 0, 3);"));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 16;"));

        // Style cho các nút khác khi hover - màu xanh mới #3d6ba8 -> #4d7ac0
        styleButtonHover(btnSendSingleReminder, "#3d6ba8", "#4d7ac0");
        styleButtonHover(btnAddFee, "#2e7d32", "#3d9d52"); // Màu xanh lá cho nút Thêm phí mới
        styleButtonHover(btnEditFee, "#3d6ba8", "#4d7ac0");
        styleButtonHover(btnGenerateInvoices, "#2e7d32", "#3d9d52");
        styleButtonHover(btnRecalculateInvoices, "#3d6ba8", "#4d7ac0");
        styleButtonHover(btnSendManualReminder, "#3d6ba8", "#4d7ac0");
    }

    /**
     * Helper method để thêm hiệu ứng hover cho button
     */
    private void styleButtonHover(Button button, String baseColor, String hoverColor) {
        String originalStyle = button.getStyle();
        button.setOnMouseEntered(e -> button.setStyle(originalStyle.replace(baseColor, hoverColor) + " -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle(originalStyle));
    }

} // <-- Dấu ngoặc cuối cùng của lớp