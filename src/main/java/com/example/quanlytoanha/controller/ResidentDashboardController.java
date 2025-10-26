package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ResidentDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button invoiceHistoryButton;

    @FXML
    private Button loginHistoryButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Xin chào, " + currentUser.getUsername() + " (Cư dân)");
        }
    }

    @FXML
    private void handleInvoiceHistoryButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/invoice_history.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Hóa đơn và Lịch sử giao dịch");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Optionally, show an error alert to the user
        }
    }
}