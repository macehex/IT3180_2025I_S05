package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Announcement;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;

public class AnnouncementDetailController {

    @FXML private Label lblDate;
    @FXML private Label lblStatus;
    @FXML private Label lblTitle;
    @FXML private TextArea txtContent;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");

    public void setAnnouncement(Announcement announcement) {
        if (announcement == null) return;

        lblTitle.setText(announcement.getAnnTitle());
        txtContent.setText(announcement.getContent());

        if (announcement.getCreatedAt() != null) {
            lblDate.setText("Gửi lúc: " + dateFormat.format(announcement.getCreatedAt()));
        } else {
            lblDate.setText("");
        }

        if (announcement.isUrgent()) {
            lblStatus.setText("🔥 Khẩn cấp");
            lblStatus.getStyleClass().removeAll("badge-normal");
            lblStatus.getStyleClass().add("badge-urgent");
        } else {
            lblStatus.setText("Thông thường");
            lblStatus.getStyleClass().removeAll("badge-urgent");
            lblStatus.getStyleClass().add("badge-normal");
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) lblTitle.getScene().getWindow();
        stage.close();
    }
}
