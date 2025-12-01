// Vị trí: src/main/java/com/example/quanlytoanha/service/AnnouncementService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.AnnouncementDAO;
import com.example.quanlytoanha.dao.NotificationDAO;
import com.example.quanlytoanha.model.Announcement;
import com.example.quanlytoanha.model.Notification;
import com.example.quanlytoanha.session.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class AnnouncementService {

    private AnnouncementDAO announcementDAO;
    private NotificationDAO notificationDAO;

    public AnnouncementService() {
        this.announcementDAO = new AnnouncementDAO();
        this.notificationDAO = new NotificationDAO();
    }

    /**
     * Tạo và gửi thông báo đến toàn bộ cư dân
     * @param announcement Thông báo cần gửi
     * @return Số lượng người nhận đã gửi thành công
     * @throws SQLException Nếu có lỗi database
     * @throws SecurityException Nếu không có quyền
     */
    public int sendAnnouncementToAll(Announcement announcement) throws SQLException, SecurityException {
        checkPermission();
        
        // 1. Lưu thông báo vào bảng announcements
        int annId = announcementDAO.createAnnouncement(announcement);
        if (annId <= 0) {
            throw new SQLException("Không thể tạo thông báo trong database.");
        }

        // 2. Lấy danh sách tất cả user_id của cư dân
        List<Integer> recipientIds = announcementDAO.getAllResidentUserIds();

        // 3. Gửi notification cho từng cư dân
        int successCount = 0;
        String title = announcement.isUrgent() 
            ? "🚨 [KHẨN CẤP] " + announcement.getAnnTitle()
            : "📢 " + announcement.getAnnTitle();

        for (Integer userId : recipientIds) {
            try {
                Notification notification = new Notification(
                    userId,
                    title,
                    announcement.getContent(),
                    null // Không liên quan đến invoice
                );
                
                if (notificationDAO.createNotification(notification)) {
                    successCount++;
                }
            } catch (SQLException e) {
                System.err.println("Lỗi khi gửi thông báo cho User ID " + userId + ": " + e.getMessage());
            }
        }

        return successCount;
    }

    /**
     * Tạo và gửi thông báo đến nhóm cư dân cụ thể (theo danh sách apartment_id)
     * @param announcement Thông báo cần gửi
     * @param apartmentIds Danh sách ID căn hộ
     * @return Số lượng người nhận đã gửi thành công
     * @throws SQLException Nếu có lỗi database
     * @throws SecurityException Nếu không có quyền
     */
    public int sendAnnouncementToGroup(Announcement announcement, List<Integer> apartmentIds) throws SQLException, SecurityException {
        checkPermission();
        
        if (apartmentIds == null || apartmentIds.isEmpty()) {
            throw new IllegalArgumentException("Danh sách căn hộ không được để trống.");
        }

        // 1. Lưu thông báo vào bảng announcements
        int annId = announcementDAO.createAnnouncement(announcement);
        if (annId <= 0) {
            throw new SQLException("Không thể tạo thông báo trong database.");
        }

        // 2. Lấy danh sách user_id của cư dân theo apartment_ids
        List<Integer> recipientIds = announcementDAO.getResidentUserIdsByApartments(apartmentIds);

        // 3. Gửi notification cho từng cư dân
        int successCount = 0;
        String title = announcement.isUrgent() 
            ? "🚨 [KHẨN CẤP] " + announcement.getAnnTitle()
            : "📢 " + announcement.getAnnTitle();

        for (Integer userId : recipientIds) {
            try {
                Notification notification = new Notification(
                    userId,
                    title,
                    announcement.getContent(),
                    null // Không liên quan đến invoice
                );
                
                if (notificationDAO.createNotification(notification)) {
                    successCount++;
                }
            } catch (SQLException e) {
                System.err.println("Lỗi khi gửi thông báo cho User ID " + userId + ": " + e.getMessage());
            }
        }

        return successCount;
    }

    /**
     * Lấy tất cả thông báo đã gửi
     */
    public List<Announcement> getAllAnnouncements() throws SQLException {
        return announcementDAO.getAllAnnouncements();
    }

    /**
     * Kiểm tra quyền: Chỉ Admin mới được gửi thông báo
     */
    private void checkPermission() throws SecurityException {
        com.example.quanlytoanha.model.User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getRole() != com.example.quanlytoanha.model.Role.ADMIN) {
            throw new SecurityException("Chỉ Ban quản trị mới có quyền gửi thông báo chung.");
        }
    }
}

