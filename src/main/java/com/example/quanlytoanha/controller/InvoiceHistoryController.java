// Vị trí: src/main/java/com/example/quanlytoanha/controller/InvoiceHistoryController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.Transaction;
import com.example.quanlytoanha.service.InvoiceService;
import com.example.quanlytoanha.session.SessionManager;
import com.example.quanlytoanha.ui.DashboardTile; // Đảm bảo import đúng
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

public class InvoiceHistoryController implements Initializable {
    private final InvoiceService invoiceService = InvoiceService.getInstance();
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
        setupTiles(); // Tạo và thêm Tile lần đầu
        setupTransactionTable();
        setupPaymentControls();
        loadData(); // Tải dữ liệu bảng giao dịch lần đầu
    }

    private void setupTiles() {
        // Tạo các đối tượng Tile
        totalDueTile = new DashboardTile(
                "Tổng Nợ",
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
        // Cấu hình các cột (Giữ nguyên)
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

        // Cấu hình hiển thị ComboBox (Giữ nguyên)
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
            // Lấy lại số tiền từ hóa đơn để tránh người dùng sửa TextField
            BigDecimal amountToPay = selectedInvoice.getTotalAmount();
            // (Bạn có thể cho phép thanh toán một phần nếu muốn, logic sẽ phức tạp hơn)

            // Gọi service xử lý thanh toán
            Transaction transaction = invoiceService.processPayment(residentId, selectedInvoice, amountToPay);

            if (transaction != null) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thanh toán cho hóa đơn #" + selectedInvoice.getInvoiceId() + " thành công!");

                refreshTiles(); // Chỉ cập nhật dữ liệu Tile

                // FIX: Ensure the new transaction is visible by expanding date range to include today
                LocalDate today = LocalDate.now();
                LocalDate currentFrom = fromDate.getValue();
                LocalDate currentTo = toDate.getValue();
                
                // Expand the date range if today is outside the current filter
                if (currentFrom == null || currentTo == null || today.isBefore(currentFrom) || today.isAfter(currentTo)) {
                    // Set a range that includes today
                    fromDate.setValue(currentFrom != null && today.isAfter(currentFrom) ? currentFrom : today.minusMonths(1));
                    toDate.setValue(currentTo != null && today.isBefore(currentTo) ? currentTo : today.plusMonths(1));
                }
                
                filterTransactions(); // Tải lại bảng giao dịch với date range bao gồm hôm nay

                // Cập nhật lại ComboBox
                invoiceSelector.getItems().remove(selectedInvoice); // Xóa hóa đơn đã thanh toán
                invoiceSelector.getSelectionModel().clearSelection(); // Bỏ chọn
                amountField.clear(); // Xóa số tiền
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xử lý thanh toán thất bại. Vui lòng thử lại.");
            }

        } catch (Exception e) { // Bắt lỗi chung
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi khi xử lý thanh toán: " + e.getMessage());
            e.printStackTrace();
        }
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