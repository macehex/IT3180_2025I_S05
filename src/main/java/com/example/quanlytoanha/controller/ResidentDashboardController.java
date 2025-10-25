package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Bill;
import com.example.quanlytoanha.model.Transaction;
import com.example.quanlytoanha.ui.DashboardTile;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ResidentDashboardController implements Initializable {

    @FXML private HBox billsTilePane;
    @FXML private ComboBox<Bill> billSelector;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> paymentMethodSelector;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, LocalDateTime> dateColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> statusColumn;
    @FXML private TableColumn<Transaction, String> paymentMethodColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTiles();
        setupTransactionTable();
        setupPaymentControls();
    }

    private void setupTiles() {
        DashboardTile totalDueTile = new DashboardTile(
            "Total Due",
            "0 VND",
            "Total amount due"
        );

        DashboardTile upcomingBillsTile = new DashboardTile(
            "Upcoming Bills",
            "0",
            "Bills due this month"
        );

        DashboardTile lastPaymentTile = new DashboardTile(
            "Last Payment",
            "N/A",
            "No recent payments"
        );

        billsTilePane.getChildren().addAll(totalDueTile, upcomingBillsTile, lastPaymentTile);
    }

    private void setupTransactionTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
    }

    private void setupPaymentControls() {
        paymentMethodSelector.getItems().addAll("Credit Card", "Bank Transfer", "E-Wallet");
        fromDate.setValue(LocalDate.now().minusMonths(1));
        toDate.setValue(LocalDate.now());
    }

    @FXML
    private void handlePayment() {
        // This will be implemented later to handle actual payments
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment");
        alert.setHeaderText(null);
        alert.setContentText("Payment feature will be implemented soon!");
        alert.showAndWait();
    }

    @FXML
    private void filterTransactions() {
        // This will be implemented later to filter transactions
        // based on the selected date range
    }
}
