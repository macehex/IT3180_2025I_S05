package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Announcement;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;

public class AnnouncementItemController {

    @FXML private HBox itemBox;
    @FXML private Label lblTime;
    @FXML private Label lblTitle;
    @FXML private Label lblContent;
    @FXML private Label lblUrgent;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public void setData(Announcement announcement) {
        // 1. Set thời gian
        if (announcement.getCreatedAt() != null) {
            lblTime.setText("[" + announcement.getCreatedAt().format(formatter) + "]");
        } else {
            lblTime.setText("[--:-- --/--]");
        }

        // 2. Set tiêu đề & Nội dung
        lblTitle.setText(announcement.getAnnTitle());
        lblContent.setText(announcement.getContent());

        // 3. Xử lý tin khẩn cấp (Hiện chữ đỏ)
        if (announcement.isUrgent()) {
            lblUrgent.setVisible(true);
            lblUrgent.setManaged(true); // Chiếm chỗ trong layout
            itemBox.setStyle("-fx-background-color: #fff0f0; -fx-border-color: #ffcccc; -fx-background-radius: 5;");
        } else {
            lblUrgent.setVisible(false);
            lblUrgent.setManaged(false);
        }
    }
}
