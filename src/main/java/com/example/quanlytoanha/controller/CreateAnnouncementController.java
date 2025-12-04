package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.dao.AnnouncementDAO;
import com.example.quanlytoanha.model.Announcement;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class CreateAnnouncementController {

    @FXML private TextField txtTitle;
    @FXML private TextArea txtContent;
    @FXML private CheckBox chkUrgent;

    private AnnouncementDAO announcementDAO;

    public void initialize() {
        this.announcementDAO = new AnnouncementDAO();
    }

    @FXML
    private void handleSend() {
        // 1. Validate dữ liệu đầu vào
        if (txtTitle.getText().isEmpty() || txtContent.getText().isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ tiêu đề và nội dung.");
            return;
        }

        // 2. Lấy thông tin người gửi (Admin đang đăng nhập)
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert("Lỗi", "Phiên đăng nhập hết hạn.");
            closeWindow();
            return;
        }

        // 3. Tạo object Announcement
        Announcement ann = new Announcement();
        ann.setAuthorId(currentUser.getUserId()); // ID của Admin
        ann.setAnnTitle(txtTitle.getText());
        ann.setContent(txtContent.getText());
        ann.setUrgent(chkUrgent.isSelected());
        ann.setCreatedAt(LocalDateTime.now()); // Thời gian hiện tại

        // 4. Gọi DAO để lưu xuống DB
        boolean success = announcementDAO.addAnnouncement(ann);

        if (success) {
            showAlert("Thành công", "Đã gửi thông báo đến toàn bộ cư dân.");
            closeWindow();
        } else {
            showAlert("Thất bại", "Có lỗi xảy ra khi lưu thông báo.");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}