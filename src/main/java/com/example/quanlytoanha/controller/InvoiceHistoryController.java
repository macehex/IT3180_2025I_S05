package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.Transaction;
import com.example.quanlytoanha.service.InvoiceService;
import com.example.quanlytoanha.session.SessionManager;
import com.example.quanlytoanha.ui.DashboardTile;
import com.example.quanlytoanha.service.InvoiceGenerationService;
import com.example.quanlytoanha.dao.InvoiceDAO;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import javafx.scene.control.ButtonType;

public class InvoiceHistoryController implements Initializable {
    private final InvoiceService invoiceService = InvoiceService.getInstance();
    private final InvoiceGenerationService invoiceGenerationService = new InvoiceGenerationService();
    public Button backButton;
    private int residentId;

    @FXML private HBox invoiceTilePane;
    @FXML private ComboBox<Invoice> invoiceSelector;
    @FXML private TextField amountField;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, LocalDateTime> dateColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private TableColumn<Transaction, BigDecimal> amountColumn;
    @FXML private TableColumn<Transaction, String> statusColumn;

    private DashboardTile totalDueTile;
    private DashboardTile unpaidInvoicesTile;
    private DashboardTile lastPaymentTile;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Lấy userId từ SessionManager (Cần đảm bảo SessionManager hoạt động đúng)
        // Nếu SessionManager chưa sẵn sàng, bạn có thể cần cơ chế khác để lấy residentId
        residentId = SessionManager.getInstance().getCurrentUser().getUserId();

        // Đảm bảo ô nhập tiền luôn cho phép chỉnh sửa
        if (amountField != null) {
            amountField.setEditable(true);
            amountField.setDisable(false);
        }

        setupTiles(); // Tạo và thêm Tile lần đầu
        setupTransactionTable();
        setupPaymentControls();
        loadData(); // Tải dữ liệu bảng giao dịch lần đầu
    }

    private void setupTiles() {
        // Tạo các đối tượng Tile
        totalDueTile = new DashboardTile(
                "Tổng Nợ (Tất cả căn hộ)",
                "Loading...",
                "Tổng số tiền hóa đơn đang chờ thanh toán"
        );
        unpaidInvoicesTile = new DashboardTile(
                "Hóa Đơn Chưa Thanh Toán",
                "Loading...",
                "Hóa đơn đang chờ thanh toán"
        );
        lastPaymentTile = new DashboardTile(
                "Thanh Toán Hôm Nay",
                "Loading...",
                "Giao dịch trong ngày"
        );

        // Xóa các Tile cũ (nếu có) và thêm các Tile mới
        invoiceTilePane.getChildren().clear();
        invoiceTilePane.getChildren().addAll(totalDueTile, unpaidInvoicesTile, lastPaymentTile);

        // Nạp dữ liệu lần đầu cho Tile
        refreshTiles();
        
    }

    /**
     * Lấy dữ liệu mới nhất và cập nhật nội dung các Tile đã có.
     */
    private void refreshTiles() {
        if (totalDueTile != null) {
            // SỬA: Gọi hàm setValue() của DashboardTile
            totalDueTile.setValue(String.format("%,.0f VND", invoiceService.getTotalDueAmount(residentId)));
        }
        if (unpaidInvoicesTile != null) {
            // SỬA: Gọi hàm setValue()
            unpaidInvoicesTile.setValue(String.valueOf(invoiceService.getUnpaidInvoicesCount(residentId)));
        }
        if (lastPaymentTile != null) {
            // SỬA: Gọi hàm setValue()
            lastPaymentTile.setValue(invoiceService.getTodayPaymentTotal(residentId));
        }
    }

    private void setupTransactionTable() {
        // Cấu hình các cột
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        dateColumn.setText("Ngày");
        descriptionColumn.setText("Mô tả");
        amountColumn.setText("Số tiền");
        statusColumn.setText("Trạng thái");
    }

    private void setupPaymentControls() {
        // FIXED: Set wider default date range to catch any existing transactions
        fromDate.setValue(LocalDate.now().minusYears(2)); // Look back 2 years
        toDate.setValue(LocalDate.now().plusMonths(1));   // Include future dates

        // Load hóa đơn chưa trả vào ComboBox
        List<Invoice> invoices = invoiceService.getUnpaidInvoices(residentId);
        invoiceSelector.getItems().setAll(invoices); // Dùng setAll để cập nhật thay vì addAll

        // Cấu hình hiển thị ComboBox
        Callback<ListView<Invoice>, ListCell<Invoice>> cellFactory = lv -> new ListCell<Invoice>() {
            @Override
            protected void updateItem(Invoice item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String detailNames = "(Chưa có chi tiết)"; // Mặc định
                    if (item.getDetails() != null && !item.getDetails().isEmpty()) {
                        detailNames = item.getDetails().stream()
                                .map(detail -> detail.getName())
                                .collect(Collectors.joining(", "));
                    }
                    setText(String.format("HĐ #%d - %s - Hạn: %s - Tổng: %,.0f VNĐ",
                            item.getInvoiceId(),
                            detailNames,
                            item.getDueDate() != null ? dateFormat.format(item.getDueDate()) : "N/A",
                            item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO
                    ));
                }
            }
        };
        invoiceSelector.setCellFactory(cellFactory);
        invoiceSelector.setButtonCell(cellFactory.call(null));

        // Cập nhật số tiền khi chọn hóa đơn (Giữ nguyên)
        invoiceSelector.setOnAction(event -> {
            Invoice selectedInvoice = invoiceSelector.getValue();
            if (selectedInvoice != null) {
                // Định dạng số tiền có dấu phẩy
                amountField.setText(String.format("%,.0f", selectedInvoice.getTotalAmount()));
            } else {
                amountField.clear();
            }
        });
    }

    private void loadData() {
        filterTransactions(); // Tải bảng giao dịch
    }

    @FXML
    private void handlePayment() {
        Invoice selectedInvoice = invoiceSelector.getValue();
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn hóa đơn để thanh toán.");
            return;
        }

        try {
            // 1. Lấy số tiền từ ô nhập liệu và xử lý định dạng
            String amountText = amountField.getText().replaceAll("[,.]", "");
            BigDecimal amountToPay;
            try {
                amountToPay = new BigDecimal(amountText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền nhập vào không hợp lệ.");
                return;
            }

            // Kiểm tra số tiền tối thiểu (chung cho mọi trường hợp)
            if (amountToPay.compareTo(new BigDecimal("1000")) < 0) {
                showAlert(Alert.AlertType.WARNING, "Số tiền quá nhỏ",
                        "Số tiền thanh toán tối thiểu phải là 1.000 VNĐ.");
                return;
            }

            BigDecimal invoiceTotal = selectedInvoice.getTotalAmount();

            // --- PHÂN LUỒNG XỬ LÝ ---

            // TRƯỜNG HỢP 1: Hóa đơn Đóng góp (Gốc = 0đ)
            if (invoiceTotal.compareTo(BigDecimal.ZERO) == 0) {
                // Với loại này, cho phép đóng bao nhiêu cũng được (miễn là > 1k)
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận đóng góp");
                confirm.setHeaderText("Ghi nhận khoản đóng góp tự nguyện");
                confirm.setContentText("Bạn xác nhận muốn đóng góp số tiền: " +
                        String.format("%,.0f", amountToPay) + " VNĐ vào quỹ chung cư không?");

                if (confirm.showAndWait().get() != ButtonType.OK) {
                    return; // Người dùng bấm Cancel
                }
            }
            // TRƯỜNG HỢP 2: Hóa đơn Dịch vụ thông thường (Điện, Nước...)
            else {
                // BẮT BUỘC: Phải trả ĐÚNG số tiền (Không thừa, không thiếu)
                if (amountToPay.compareTo(invoiceTotal) != 0) {
                    showAlert(Alert.AlertType.WARNING, "Sai số tiền",
                            "Vui lòng chỉ thanh toán ĐÚNG số tiền trên hóa đơn (" +
                                    String.format("%,.0f", invoiceTotal) + " VNĐ).\n" +
                                    "Hệ thống không hỗ trợ thanh toán dư hoặc thiếu cho hóa đơn dịch vụ.");

                    // Tự động điền lại số đúng cho người dùng đỡ phải gõ lại
                    amountField.setText(String.format("%,.0f", invoiceTotal));
                    return;
                }

                // Nếu đúng số tiền -> Hiện xác nhận thanh toán bình thường
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận thanh toán");
                confirm.setHeaderText("Thanh toán hóa đơn");
                confirm.setContentText("Bạn xác nhận thanh toán số tiền: " +
                        String.format("%,.0f", amountToPay) + " VNĐ?");

                if (confirm.showAndWait().get() != ButtonType.OK) {
                    return;
                }
            }

            // -----------------------------------

            // 2. Gọi Service xử lý thanh toán (Logic giữ nguyên)
            Transaction transaction = invoiceService.processPayment(residentId, selectedInvoice, amountToPay);

            if (transaction != null) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Giao dịch thành công! Mã giao dịch: " + transaction.getTransactionId());

                refreshTiles();

                // Tái tạo hóa đơn đóng góp (nếu cần)
                invoiceGenerationService.regenerateVoluntaryInvoice(selectedInvoice.getInvoiceId());

                // Refresh UI
                fromDate.setValue(LocalDate.now().minusDays(1));
                toDate.setValue(LocalDate.now().plusDays(1));
                filterTransactions();

                List<Invoice> updatedInvoices = invoiceService.getUnpaidInvoices(residentId);
                invoiceSelector.getItems().setAll(updatedInvoices);
                invoiceSelector.getSelectionModel().clearSelection();
                amountField.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xử lý thanh toán thất bại. Vui lòng thử lại.");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hàm phụ trợ: Thực hiện gọi Service thanh toán (Tách ra để code gọn hơn)
     */
    private void performPaymentTransaction(Invoice invoice, BigDecimal amount) {
        try {
            Transaction transaction = invoiceService.processPayment(residentId, invoice, amount);

            if (transaction != null) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Thanh toán thành công! Cảm ơn cư dân.");

                refreshTiles();

                // Logic mở rộng ngày lọc để thấy giao dịch mới
                LocalDate today = LocalDate.now();
                LocalDate currentFrom = fromDate.getValue();
                LocalDate currentTo = toDate.getValue();
                if (currentFrom == null || currentTo == null || today.isBefore(currentFrom) || today.isAfter(currentTo)) {
                    fromDate.setValue(currentFrom != null && today.isAfter(currentFrom) ? currentFrom : today.minusMonths(1));
                    toDate.setValue(currentTo != null && today.isBefore(currentTo) ? currentTo : today.plusMonths(1));
                }

                filterTransactions();

                // Cập nhật UI
                invoiceSelector.getItems().remove(invoice);
                invoiceSelector.getSelectionModel().clearSelection();
                amountField.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xử lý thanh toán thất bại. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hàm phụ trợ: Format tiền tệ sang tiếng Việt (VND)
     */
    private String formatCurrency(BigDecimal amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return currencyFormat.format(amount);
    }

    @FXML
    private void filterTransactions() {
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();

        if (from == null || to == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn cả ngày bắt đầu và ngày kết thúc.");
            return;
        }
        if (from.isAfter(to)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Ngày bắt đầu không được sau ngày kết thúc.");
            return;
        }

        try { // Thêm try-catch nếu getTransactions có thể ném lỗi
            List<Transaction> transactions = invoiceService.getTransactions(residentId, from, to);
            transactionTable.getItems().setAll(transactions); // Dùng setAll để thay thế dữ liệu cũ
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải lịch sử giao dịch: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        // (Giữ nguyên hàm này)
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        // (Giữ nguyên hàm này)
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}