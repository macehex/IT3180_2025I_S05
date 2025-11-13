// Vị trí: src/main/java/com/example/quanlytoanha/dao/ServiceRequestDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ServiceRequestDAO {

    /**
     * THỐNG KÊ DASHBOARD (US7_1_1): Đếm tổng số yêu cầu/sự cố đang chờ xử lý.
     * Trạng thái: PENDING hoặc IN_PROGRESS.
     * @return Số lượng yêu cầu cần xử lý.
     */
    public int countPendingServiceRequests() {
        // Đếm các request chưa hoàn thành (COMPLETED hoặc CANCELLED)
        String sql = "SELECT COUNT(*) FROM service_requests WHERE status NOT IN ('COMPLETED', 'CANCELLED')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // (Các hàm CRUD khác cho Service Request sẽ được thêm vào đây khi triển khai US2_3_1)
}