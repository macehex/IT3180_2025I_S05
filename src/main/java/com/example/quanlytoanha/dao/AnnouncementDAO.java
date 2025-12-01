// Vị trí: src/main/java/com/example/quanlytoanha/dao/AnnouncementDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Announcement;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementDAO {

    /**
     * Tạo thông báo mới
     */
    public int createAnnouncement(Announcement announcement) throws SQLException {
        String sql = "INSERT INTO announcements (author_id, ann_title, content, is_urgent, created_at) " +
                     "VALUES (?, ?, ?, ?, CURRENT_DATE) RETURNING ann_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, announcement.getAuthorId());
            pstmt.setString(2, announcement.getAnnTitle());
            pstmt.setString(3, announcement.getContent());
            pstmt.setBoolean(4, announcement.isUrgent());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int annId = rs.getInt(1);
                    announcement.setAnnId(annId);
                    return annId;
                }
            }
        }
        return -1;
    }

    /**
     * Lấy tất cả thông báo
     */
    public List<Announcement> getAllAnnouncements() throws SQLException {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "SELECT * FROM announcements ORDER BY created_at DESC, is_urgent DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                announcements.add(mapResultSetToAnnouncement(rs));
            }
        }
        return announcements;
    }

    /**
     * Lấy thông báo theo ID
     */
    public Announcement getAnnouncementById(int annId) throws SQLException {
        String sql = "SELECT * FROM announcements WHERE ann_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, annId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAnnouncement(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lấy danh sách user_id của tất cả cư dân đang ở (RESIDING hoặc RENTING)
     * Không bao gồm những người đã chuyển ra (MOVED_OUT)
     */
    public List<Integer> getAllResidentUserIds() throws SQLException {
        List<Integer> userIds = new ArrayList<>();
        
        // Lấy tất cả user_id của cư dân đang ở (status = 'RESIDING' hoặc 'RENTING')
        // JOIN với residents để lọc theo status
        String sql = "SELECT DISTINCT u.user_id FROM users u " +
                     "INNER JOIN residents r ON u.user_id = r.user_id " +
                     "WHERE u.role_id = 4 " + // Role.RESIDENT
                     "AND (r.status = 'RESIDING' OR r.status = 'RENTING' OR r.status IS NULL)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }
        }
        
        // Nếu không tìm thấy, thử cách khác: dùng role_name
        if (userIds.isEmpty()) {
            sql = "SELECT DISTINCT u.user_id FROM users u " +
                  "INNER JOIN residents r ON u.user_id = r.user_id " +
                  "JOIN roles ro ON u.role_id = ro.role_id " +
                  "WHERE (ro.role_name = 'Cư dân' OR LOWER(ro.role_name) = 'resident') " +
                  "AND (r.status = 'RESIDING' OR r.status = 'RENTING' OR r.status IS NULL)";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    userIds.add(rs.getInt("user_id"));
                }
            }
        }
        
        System.out.println("DEBUG: Tìm thấy " + userIds.size() + " cư dân đang ở (RESIDING/RENTING).");
        return userIds;
    }

    /**
     * Lấy danh sách user_id của cư dân theo danh sách apartment_id
     */
    public List<Integer> getResidentUserIdsByApartments(List<Integer> apartmentIds) throws SQLException {
        if (apartmentIds == null || apartmentIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Integer> userIds = new ArrayList<>();
        String placeholders = apartmentIds.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("");
        String sql = "SELECT DISTINCT u.user_id FROM users u " +
                     "JOIN residents r ON u.user_id = r.user_id " +
                     "JOIN apartments a ON r.apartment_id = a.apartment_id " +
                     "WHERE a.apartment_id IN (" + placeholders + ") " +
                     "AND u.role_id = 4 " + // Role.RESIDENT có roleId = 4
                     "AND (r.status = 'RESIDING' OR r.status = 'RENTING' OR r.status IS NULL)"; // Chỉ lấy cư dân đang ở
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < apartmentIds.size(); i++) {
                pstmt.setInt(i + 1, apartmentIds.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getInt("user_id"));
                }
            }
        }
        return userIds;
    }

    /**
     * Ánh xạ ResultSet thành Announcement
     */
    private Announcement mapResultSetToAnnouncement(ResultSet rs) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setAnnId(rs.getInt("ann_id"));
        announcement.setAuthorId(rs.getInt("author_id"));
        announcement.setAnnTitle(rs.getString("ann_title"));
        announcement.setContent(rs.getString("content"));
        announcement.setUrgent(rs.getBoolean("is_urgent"));
        announcement.setCreatedAt(rs.getDate("created_at"));
        return announcement;
    }
}

