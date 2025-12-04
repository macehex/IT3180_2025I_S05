package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Announcement;
import com.example.quanlytoanha.utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementDAO {

    // 1. Hàm lấy tất cả thông báo, sắp xếp mới nhất lên đầu
    // Tương ứng User Story: "hiển thị tất cả thông báo theo thứ tự thời gian"
    public List<Announcement> getAllAnnouncements() {
        List<Announcement> list = new ArrayList<>();

        // SQL query: Lấy tất cả và sắp xếp giảm dần theo thời gian (DESC)
        String sql = "SELECT * FROM announcements ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Mapping dữ liệu từ ResultSet sang Object Java
                Announcement ann = new Announcement();

                ann.setAnnId(rs.getInt("ann_id"));
                ann.setAuthorId(rs.getInt("author_id"));
                ann.setAnnTitle(rs.getString("ann_title"));
                ann.setContent(rs.getString("content"));
                ann.setUrgent(rs.getBoolean("is_urgent"));

                // Xử lý chuyển đổi từ SQL Timestamp sang Java LocalDateTime
                Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    ann.setCreatedAt(timestamp.toLocalDateTime());
                }

                list.add(ann);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Nên dùng Logger trong dự án thực tế
        }

        return list;
    }

    // 2. Hàm thêm thông báo mới (Dùng cho Admin/BQL để test dữ liệu)
    public boolean addAnnouncement(Announcement ann) {
        String sql = "INSERT INTO announcements (author_id, ann_title, content, is_urgent, created_at) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ann.getAuthorId());
            pstmt.setString(2, ann.getAnnTitle());
            pstmt.setString(3, ann.getContent());
            pstmt.setBoolean(4, ann.isUrgent());

            // Chuyển từ LocalDateTime về Timestamp để lưu xuống DB
            pstmt.setTimestamp(5, Timestamp.valueOf(ann.getCreatedAt()));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy danh sách thông báo được tạo bởi một tác giả cụ thể (Dành cho Admin xem lịch sử gửi).
     * @param authorId ID của người tạo (Admin).
     * @return Danh sách thông báo.
     */
    public List<Announcement> getAnnouncementsByAuthor(int authorId) {
        List<Announcement> list = new ArrayList<>();
        String sql = "SELECT * FROM announcements WHERE author_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, authorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Announcement ann = new Announcement();
                    ann.setAnnId(rs.getInt("ann_id"));
                    ann.setAuthorId(rs.getInt("author_id"));
                    ann.setAnnTitle(rs.getString("ann_title"));
                    ann.setContent(rs.getString("content"));
                    ann.setUrgent(rs.getBoolean("is_urgent"));

                    Timestamp timestamp = rs.getTimestamp("created_at");
                    if (timestamp != null) {
                        ann.setCreatedAt(timestamp.toLocalDateTime());
                    }
                    list.add(ann);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
