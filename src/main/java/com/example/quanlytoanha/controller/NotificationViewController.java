// Vị trí: src/main/java/com/example/quanlytoanha/controller/NotificationViewController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Notification;
import com.example.quanlytoanha.service.NotificationService; // Import service tương ứng
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // Implement Initializable để dùng hàm initialize()
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip; // Import Tooltip để hiển thị đầy đủ message
import javafx.util.Callback; // Import Callback

import java.net.URL; // Import URL
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle; // Import ResourceBundle

public class NotificationViewController implements Initializable { // Implement Initializable

    @FXML
    private ListView<Notification> notificationListView; // ListView từ FXML

    private NotificationService notificationService; // Service để lấy dữ liệu
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy"); // Định dạng ngày giờ

    /**
     * Phương thức này được gọi tự động sau khi FXML được tải.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.notificationService = new NotificationService(); // Khởi tạo service

        // Cấu hình cách hiển thị từng mục trong ListView
        notificationListView.setCellFactory(param -> new ListCell<Notification>() {
            private Tooltip tooltip = new Tooltip(); // Tạo tooltip để xem message dài

            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null); // Xóa graphic nếu ô rỗng
                    setStyle(""); // Xóa style cũ
                    setTooltip(null);
                } else {
                    // Tạo nội dung hiển thị: Ngày giờ + Tiêu đề
                    String displayText = String.format("[%s] %s",
                            dateTimeFormat.format(item.getCreatedAt()), // Dùng timestamp từ DB
                            item.getTitle());
                    setText(displayText);

                    // Đặt nội dung đầy đủ vào tooltip
                    tooltip.setText(item.getMessage());
                    setTooltip(tooltip);

                    // Đặt style cho thông báo chưa đọc
                    if (!item.isRead()) {
                        // In đậm và có thể thêm màu nền nhẹ nhàng
                        setStyle("-fx-font-weight: bold; -fx-background-color: #e8f4ff;");
                    } else {
                        // Xóa style nếu đã đọc
                        setStyle("");
                    }
                }
            }
        });

        // Thêm sự kiện click chuột vào ListView
        notificationListView.setOnMouseClicked(event -> {
            Notification selectedNotification = notificationListView.getSelectionModel().getSelectedItem();
            // Nếu có mục được chọn và nó chưa đọc
            if (selectedNotification != null && !selectedNotification.isRead()) {
                markNotificationAsRead(selectedNotification); // Gọi hàm đánh dấu đã đọc
            }
        });

        // Tải danh sách thông báo lần đầu
        loadNotifications();
    }

    /**
     * Tải danh sách thông báo (chưa đọc) từ service và hiển thị lên ListView.
     */
    private void loadNotifications() {
        try {
            // Gọi service để lấy thông báo chưa đọc của người dùng hiện tại
            List<Notification> notifications = notificationService.getMyUnreadNotifications();
            // Cập nhật dữ liệu cho ListView
            notificationListView.getItems().setAll(notifications);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể tải danh sách thông báo.");
            e.printStackTrace(); // In lỗi ra console để debug
        } catch (Exception e) { // Bắt các lỗi khác (ví dụ: SecurityException nếu cần)
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gọi service để đánh dấu thông báo là đã đọc và cập nhật lại giao diện.
     * @param notification Thông báo được click.
     */
    private void markNotificationAsRead(Notification notification) {
        try {
            boolean success = notificationService.markNotificationAsRead(notification.getNotificationId());
            if (success) {
                // Đánh dấu là đã đọc trong đối tượng model
                notification.setRead(true);
                // Làm mới ListView để cập nhật lại style (xóa in đậm)
                notificationListView.refresh();
            } else {
                System.err.println("Không thể đánh dấu đã đọc cho thông báo ID: " + notification.getNotificationId());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể cập nhật trạng thái thông báo.");
            e.printStackTrace();
        }
    }

    /**
     * Hàm tiện ích để hiển thị thông báo Alert.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // Không có tiêu đề phụ
        alert.setContentText(message);
        alert.showAndWait();
    }
}