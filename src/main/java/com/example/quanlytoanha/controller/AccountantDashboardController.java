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
import com.example.quanlytoanha.model.ContributionHistoryDTO;
import com.example.quanlytoanha.dao.InvoiceDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AccountantDashboardController {

    // --- KHAI BÁO @FXML ---
    @FXML private Label lblTotalDebt;
    @FXML private Label lblTotalOverdue;
    @FXML private Label lblUnpaidInvoices;
    @FXML private Button btnLogout;
    @FXML private TableView<ApartmentDebt> debtTable;
    @FXML private TableColumn<ApartmentDebt, Integer> colApartmentId;
    @FXML private TableColumn<ApartmentDebt, String> colOwnerName;
    @FXML private TableColumn<ApartmentDebt, String> colPhoneNumber;
    @FXML private TableColumn<ApartmentDebt, Integer> colUnpaidCount;
    @FXML private TableColumn<ApartmentDebt, BigDecimal> colTotalDue;
    @FXML private TableColumn<ApartmentDebt, java.util.Date> colEarliestDueDate;
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

    @FXML private Button btnSendSingleReminder;
    @FXML private Button btnLaunchCampaign;

    // --- TAB 4: BÁO CÁO & LỊCH SỬ ---
    @FXML private TableView<Invoice> tblPaidInvoices;
    @FXML private TableColumn<Invoice, Integer> colHistInvId;
    @FXML private TableColumn<Invoice, Integer> colHistAptId;
    @FXML private TableColumn<Invoice, BigDecimal> colHistAmount;
    @FXML private TableColumn<Invoice, java.util.Date> colHistDate; // Hoặc String tùy model

    @FXML private TableView<ContributionHistoryDTO> tblContributions;
    @FXML private TableColumn<ContributionHistoryDTO, LocalDateTime> colConDate;
    @FXML private TableColumn<ContributionHistoryDTO, String> colConRoom;
    @FXML private TableColumn<ContributionHistoryDTO, String> colConName;
    @FXML private TableColumn<ContributionHistoryDTO, String> colConFee;
    @FXML private TableColumn<ContributionHistoryDTO, BigDecimal> colConAmount;

    @FXML private Label lblPaidInvoices;
    @FXML private Label lblTotalCollected;
    @FXML private Label lblTotalContribution;

    private FinancialService financialService;
    private FeeTypeService feeTypeService;
    private InvoiceGenerationService invoiceGenerationService;
    private NotificationService notificationService;
    private InvoiceDAO invoiceDAO;

    @FXML
    public void initialize() {
        this.financialService = new FinancialService();
        this.feeTypeService = new FeeTypeService();
        this.invoiceGenerationService = new InvoiceGenerationService();
        this.notificationService = new NotificationService();
        this.invoiceDAO = InvoiceDAO.getInstance();

        setupNumericTextField(txtDaysBefore);
        setupNumericTextField(txtBillingMonth);
        setupNumericTextField(txtBillingYear);

        loadDashboardData();
        configureDebtTableColumns();
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

        setupReportTables();
        loadReportData();

        btnLaunchCampaign.setOnAction(event -> handleLaunchCampaign());
    }


    private void setupNumericTextField(TextField textField) {
        // Sử dụng UnaryOperator để lọc thay đổi
        java.util.function.UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            // Cho phép chuỗi rỗng (khi xóa hết) hoặc chuỗi chỉ chứa số
            if (newText.matches("\\d*")) {
                return change;
            }
            return null; // Từ chối thay đổi nếu không phải số
        };

        TextFormatter<String> formatter = new TextFormatter<>(filter);
        textField.setTextFormatter(formatter);
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

            if (lblPaidInvoices != null) {
                lblPaidInvoices.setText(report.getTotalPaidInvoices() + " hóa đơn");
            }

            if (lblTotalCollected != null) {
                lblTotalCollected.setText(String.format("%,.0f VNĐ", report.getTotalCollectedAmount()));
            }
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
    private void configureDebtTableColumns() {
        colApartmentId.setCellValueFactory(new PropertyValueFactory<>("apartmentId"));
        colOwnerName.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        colPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colUnpaidCount.setCellValueFactory(new PropertyValueFactory<>("unpaidCount"));
        colTotalDue.setCellValueFactory(new PropertyValueFactory<>("totalDue"));
        colEarliestDueDate.setCellValueFactory(new PropertyValueFactory<>("earliestDueDate"));
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

        // --- PHÂN LUỒNG XỬ LÝ MỚI ---
        if ("VOLUNTARY".equals(selectedFee.getPricingModel())) {
            // Nếu là phí Đóng góp -> Dùng logic sửa đặc biệt
            editVoluntaryFee(selectedFee);
        } else {
            // Nếu là phí thường -> Dùng logic cũ
            if (showFeeEditDialog(selectedFee)) {
                loadFeeData();
            }
        }
    }

    // Hàm xử lý riêng cho phí Đóng góp
    private void editVoluntaryFee(FeeType fee) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/campaign_form.fxml"));
            Parent root = loader.load();
            CampaignFormController controller = loader.getController();

            // 1. Lấy hạn cũ từ DB
            LocalDate currentDueDate = invoiceDAO.getLatestDueDateForFee(fee.getFeeId());

            // 2. Mở form Edit
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Chỉnh sửa Đợt đóng góp");
            dialogStage.setScene(new Scene(root));
            controller.setDialogStage(dialogStage);
            controller.setCampaignToEdit(fee, currentDueDate); // Đẩy data cũ vào

            dialogStage.showAndWait();

            // 3. Xử lý sau khi Lưu
            if (controller.isLaunched()) {
                LocalDate newDueDate = controller.getSelectedDueDate();

                // Cập nhật ngày trong DB nếu có thay đổi
                boolean isDateChanged = newDueDate != null && !newDueDate.equals(currentDueDate);
                if (isDateChanged) {
                    invoiceDAO.updateCampaignDueDate(fee.getFeeId(), newDueDate);
                }
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Đã cập nhật thông tin đợt đóng góp.");
                loadFeeData();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form chỉnh sửa: " + e.getMessage());
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
        alert.setHeaderText("Bạn có chắc chắn muốn kết thúc/hủy loại phí này?");
        alert.setContentText("Phí: " + selectedFee.getFeeName() + "\n(Lưu ý: Các khoản chưa đóng sẽ bị xóa bỏ)");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // 1. Thực hiện Hủy phí (Service sẽ tự gọi DAO để xóa hóa đơn UNPAID)
                    boolean success = feeTypeService.deactivateFee(selectedFee.getFeeId());

                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã kết thúc loại phí và dọn dẹp dữ liệu.");
                        loadFeeData(); // Refresh lại bảng
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hủy phí. Vui lòng thử lại.");
                    }
                } catch (SecurityException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Phân Quyền", e.getMessage());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Lỗi: " + e.getMessage());
                    e.printStackTrace();
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

    // --- LOGIC CHO TAB BÁO CÁO ---

    private void setupReportTables() {
        // 1. Setup bảng Hóa đơn đã thu
        colHistInvId.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        colHistAptId.setCellValueFactory(new PropertyValueFactory<>("apartmentId"));
        colHistAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colHistDate.setCellValueFactory(new PropertyValueFactory<>("dueDate")); // Lưu ý: Cần đảm bảo InvoiceDAO trả về ngày thanh toán vào cột này

        // 2. Setup bảng Đóng góp
        colConRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colConName.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        colConFee.setCellValueFactory(new PropertyValueFactory<>("feeName"));
        colConAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colConDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));

        // Format ngày giờ cho đẹp (dd/MM/yyyy HH:mm)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colConDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(formatter.format(item));
            }
        });
    }

    private void loadReportData() {
        try {
            // 1. Load Hóa đơn đã thu
            List<Invoice> paidInvoices = invoiceDAO.getPaidInvoicesHistory();
            tblPaidInvoices.getItems().setAll(paidInvoices);

            // 2. Load Lịch sử đóng góp
            List<ContributionHistoryDTO> contributions = invoiceDAO.getContributionHistory();
            tblContributions.getItems().setAll(contributions);

            // 3. Tính tổng tiền đóng góp
            BigDecimal total = contributions.stream()
                    .map(ContributionHistoryDTO::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            lblTotalContribution.setText(String.format("TỔNG THU: %,.0f VNĐ", total));

        } catch (Exception e) {
            e.printStackTrace();
            // Có thể thêm showAlert nếu cần thiết, nhưng nên hạn chế popup lúc khởi động
        }
    }

    @FXML
    private void handleLaunchCampaign() {
        try {
            // 1. Load file FXML mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/campaign_form.fxml"));
            Parent root = loader.load();

            // 2. Lấy Controller và setup Stage
            CampaignFormController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Phát động Đóng góp");
            dialogStage.initOwner((Stage) btnLaunchCampaign.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            controller.setDialogStage(dialogStage);

            // 3. Hiển thị và chờ người dùng thao tác
            dialogStage.showAndWait();

            // 4. Xử lý sau khi đóng form
            if (controller.isLaunched()) {
                FeeType newFee = controller.getCreatedFee();
                LocalDate dueDate = controller.getSelectedDueDate();

                if (newFee != null && dueDate != null) {
                    // Gọi Service để tạo hóa đơn hàng loạt (như logic cũ)
                    String result = invoiceGenerationService.createContributionCampaign(newFee, LocalDate.now(), dueDate);

                    showAlert(Alert.AlertType.INFORMATION, "Thành công", result);

                    // Refresh bảng
                    loadDashboardData();
                } else {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Đã tạo phí nhưng không lấy được ID để tạo hóa đơn. Vui lòng kiểm tra lại.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao diện", "Không thể mở form phát động: " + e.getMessage());
        }
    }
}