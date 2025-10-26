package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.dao.UserDAO;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.utils.PasswordUtil;
import com.example.quanlytoanha.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;

import java.time.format.DateTimeFormatter;

public class ResidentLoginHistoryAndManagementController {

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private Button changePasswordButton;

    @FXML
    private Button backButton;

    @FXML
    private TableView<LoginHistory> loginHistoryTable;

    @FXML
    private TableColumn<LoginHistory, String> colDate;

    @FXML
    private TableColumn<LoginHistory, String> colTime;

    @FXML
    private TableColumn<LoginHistory, String> colStatus;

    @FXML
    private TableColumn<LoginHistory, String> colIp;

    private final ObservableList<LoginHistory> loginHistoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colIp.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));

        loadLoginHistory();
        loginHistoryTable.setItems(loginHistoryList);
    }

    @FXML
    private void handleChangePassword() {
        String newPassword = newPasswordField.getText().trim();

        if (newPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập mật khẩu mới!");
            return;
        }

        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không xác định được người dùng hiện tại.");
                return;
            }

            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            boolean success = UserDAO.updateUserPassword(currentUser.getUserId(), hashedPassword);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Mật khẩu đã được thay đổi thành công!");
                currentUser.setPassword(hashedPassword);
                // Note: SessionManager doesn't have setCurrentUser method, but we can update via login
                SessionManager.getInstance().login(currentUser);
                newPasswordField.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thay đổi mật khẩu. Vui lòng thử lại sau.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi khi cập nhật mật khẩu.");
        }
    }

    @FXML
    private void handleBack() {
        // TODO: Navigate back to the Resident Dashboard
        // Example: SceneController.switchTo("/com/example/quanlytoanha/view/resident_dashboard.fxml");
        showAlert(Alert.AlertType.INFORMATION, "Quay lại", "Trở về trang chính (chưa được cài đặt).");
    }

    private void loadLoginHistory() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin người dùng.");
            return;
        }

        loginHistoryList.clear();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        if (currentUser.getLastLogin() != null) {
            // Convert Timestamp to LocalDateTime for formatting
            java.time.LocalDateTime lastLoginTime = currentUser.getLastLogin().toLocalDateTime();
            loginHistoryList.add(new LoginHistory(
                    lastLoginTime.format(dateFormatter),
                    lastLoginTime.format(timeFormatter),
                    "Thành công",
                    "192.168.1.12"
            ));
        }

        // TODO: Load more login records from the database if available
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    public static class LoginHistory {
        private final String date;
        private final String time;
        private final String status;
        private final String ipAddress;

        public LoginHistory(String date, String time, String status, String ipAddress) {
            this.date = date;
            this.time = time;
            this.status = status;
            this.ipAddress = ipAddress;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getStatus() {
            return status;
        }

        public String getIpAddress() {
            return ipAddress;
        }
    }
}
