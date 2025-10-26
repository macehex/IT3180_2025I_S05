package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.Transaction;
import com.example.quanlytoanha.service.InvoiceService;
import com.example.quanlytoanha.session.SessionManager;
import com.example.quanlytoanha.ui.DashboardTile;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        residentId = SessionManager.getInstance().getCurrentUser().getUserId();
        setupTiles();
        setupTransactionTable();
        setupPaymentControls();
        loadData();
    }

    private void setupTiles() {
        DashboardTile totalDueTile = new DashboardTile(
            "Total Due",
            String.format("%.2f VND", invoiceService.getTotalDueAmount(residentId)),
            "Total amount due"
        );

        DashboardTile upcomingInvoicesTile = new DashboardTile(
            "Unpaid Invoices",
            String.valueOf(invoiceService.getUnpaidInvoicesCount(residentId)),
            "Invoices pending payment"
        );

        DashboardTile lastPaymentTile = new DashboardTile(
            "Last Payment",
            invoiceService.getLastPaymentInfo(residentId),
            "Most recent payment"
        );

        invoiceTilePane.getChildren().addAll(totalDueTile, upcomingInvoicesTile, lastPaymentTile);
    }

    private void setupTransactionTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupPaymentControls() {
        fromDate.setValue(LocalDate.now().minusMonths(1));
        toDate.setValue(LocalDate.now());

        // Load invoices into invoice selector
        List<Invoice> invoices = invoiceService.getUnpaidInvoices(residentId);
        invoiceSelector.getItems().addAll(invoices);

        // Update amount field when invoice is selected
        invoiceSelector.setOnAction(event -> {
            Invoice selectedInvoice = invoiceSelector.getValue();
            if (selectedInvoice != null) {
                amountField.setText(String.valueOf(selectedInvoice.getTotalAmount()));
            }
        });
    }

    private void loadData() {
        filterTransactions();
    }

    @FXML
    private void handlePayment() {
        Invoice selectedInvoice = invoiceSelector.getValue();

        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select an invoice");
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountField.getText());
            if (amount.compareTo(selectedInvoice.getTotalAmount()) != 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Payment amount must match invoice amount");
                return;
            }

            Transaction transaction = invoiceService.processPayment(residentId, selectedInvoice, amount);

            if (transaction != null) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Payment processed successfully");
                setupTiles(); // Refresh tiles
                filterTransactions(); // Refresh transaction table
                invoiceSelector.getItems().remove(selectedInvoice);
                amountField.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Payment processing failed");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid amount");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Payment processing failed: " + e.getMessage());
        }
    }

    @FXML
    private void filterTransactions() {
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();

        if (from == null || to == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select valid dates");
            return;
        }

        List<Transaction> transactions = invoiceService.getTransactions(residentId, from, to);
        transactionTable.getItems().clear();
        transactionTable.getItems().addAll(transactions);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        // Close (kill) the current Login Management window
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}
