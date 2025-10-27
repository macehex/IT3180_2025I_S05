package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Notification;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.*;
        import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    /**
     * Tạo một thông báo mới cho một người dùng cụ thể.
     */
    public boolean createNotification(Notification notification) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, message, related_invoice_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getTitle());
            stmt.setString(3, notification.getMessage());

            if (notification.getRelatedInvoiceId() != null) {
                stmt.setInt(4, notification.getRelatedInvoiceId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Lấy tất cả thông báo CHƯA ĐỌC cho một người dùng.
     */
    public List<Notification> getUnreadNotificationsForUser(int userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        }
        return notifications;
    }

    /**
     * Đánh dấu một thông báo là đã đọc.
     */
    public boolean markAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Hàm tiện ích để chuyển đổi ResultSet thành Notification.
     */
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));
        n.setUserId(rs.getInt("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        n.setRead(rs.getBoolean("is_read"));
        // Lấy related_invoice_id, có thể null
        int relatedId = rs.getInt("related_invoice_id");
        if (!rs.wasNull()) {
            n.setRelatedInvoiceId(relatedId);
        }
        return n;
    }
}