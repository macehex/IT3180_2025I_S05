package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Notification;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;

public class NotificationDetailController {

    @FXML private Label lblDate;
    @FXML private Label lblTitle;
    @FXML private TextArea txtMessage;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");

    public void setNotification(Notification notification) {
        if (notification == null) return;

        // Map dữ liệu từ Notification sang giao diện
        lblTitle.setText(notification.getTitle());
        txtMessage.setText(notification.getMessage());

        if (notification.getCreatedAt() != null) {
            lblDate.setText("Nhận lúc: " + dateFormat.format(notification.getCreatedAt()));
        } else {
            lblDate.setText("");
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) lblTitle.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
