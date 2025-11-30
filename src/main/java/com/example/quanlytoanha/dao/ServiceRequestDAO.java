// Vị trí: src/main/java/com/example/quanlytoanha/dao/ServiceRequestDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.ServiceRequest;
import com.example.quanlytoanha.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceRequestDAO {

    /**
     * THỐNG KÊ DASHBOARD (US7_1_1):
     */
    public int countPendingServiceRequests() {
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

    /**
     * CƯ DÂN (US3_1_1): Tạo một yêu cầu dịch vụ/báo cáo sự cố mới. (CẬP NHẬT)
     *
     * @param request Đối tượng ServiceRequest chứa đầy đủ thông tin
     * @return
     */
    public boolean createServiceRequest(ServiceRequest request) {
        String sql = "INSERT INTO service_requests (req_user_id, req_type, req_title, description, status, created_at, asset_id, image_url) " +
                     "VALUES (?, ?::request_type_enum, ?, ?, ?::request_status_enum, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, request.getReqUserId());
            pstmt.setString(2, request.getReqType());
            pstmt.setString(3, request.getReqTitle());
            pstmt.setString(4, request.getDescription());
            pstmt.setString(5, request.getStatus()); // Java gửi String "PENDING", SQL sẽ ép kiểu
            pstmt.setDate(6, request.getCreatedAt());

            if (request.getAssetId() != null) {
                pstmt.setInt(7, request.getAssetId());
            } else {
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }

            if (request.getImageUrl() != null) {
                pstmt.setString(8, request.getImageUrl());
            } else {
                pstmt.setNull(8, java.sql.Types.VARCHAR);
            }

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo Service Request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * CƯ DÂN (US3_1_1): Lấy danh sách các yêu cầu đã gửi bởi một người dùng. (CẬP NHẬT)
     * @param userId ID của người dùng (lấy từ req_user_id)
     * @return Danh sách các ServiceRequest
     */
    public List<ServiceRequest> getRequestsByUserId(int userId) {
        List<ServiceRequest> requestList = new ArrayList<>();
        // CẬP NHẬT: Câu SQL "SELECT *" sẽ tự động lấy cả cột 'image_url' mới
        String sql = "SELECT * FROM service_requests WHERE req_user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ServiceRequest request = new ServiceRequest();
                request.setRequestId(rs.getInt("request_id"));
                request.setReqUserId(rs.getInt("req_user_id"));
                request.setReqType(rs.getString("req_type"));
                request.setReqTitle(rs.getString("req_title"));
                request.setDescription(rs.getString("description"));
                request.setStatus(rs.getString("status"));
                request.setCreatedAt(rs.getDate("created_at"));
                request.setCompletedAt(rs.getDate("completed_at"));

                // Lấy asset_id (có thể là null)
                int assetId = rs.getInt("asset_id");
                if (rs.wasNull()) {
                    request.setAssetId(null);
                } else {
                    request.setAssetId(assetId);
                }

                request.setImageUrl(rs.getString("image_url"));

                requestList.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requestList;
    }

    /**
     * ADMIN: Lấy TOÀN BỘ danh sách yêu cầu (để quản lý)
     * Sắp xếp: Mới nhất lên đầu.
     */
    public List<ServiceRequest> getAllServiceRequests() {
        List<ServiceRequest> requestList = new ArrayList<>();
        String sql = "SELECT * FROM service_requests ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ServiceRequest request = new ServiceRequest();
                request.setRequestId(rs.getInt("request_id"));
                request.setReqUserId(rs.getInt("req_user_id"));
                request.setReqType(rs.getString("req_type"));
                request.setReqTitle(rs.getString("req_title"));
                request.setDescription(rs.getString("description"));
                request.setStatus(rs.getString("status"));
                request.setCreatedAt(rs.getDate("created_at"));
                request.setCompletedAt(rs.getDate("completed_at"));

                int assetId = rs.getInt("asset_id");
                if (rs.wasNull()) {
                    request.setAssetId(null);
                } else {
                    request.setAssetId(assetId);
                }

                request.setImageUrl(rs.getString("image_url"));

                requestList.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requestList;
    }

    /**
     * ADMIN (US5_2_1): Cập nhật trạng thái xử lý của yêu cầu
     * @param requestId ID của yêu cầu
     * @param newStatus Trạng thái mới (ví dụ: "IN_PROGRESS", "COMPLETED")
     */
    public boolean updateRequestStatus(int requestId, String newStatus) {
        String sql = "UPDATE service_requests SET status = ?::request_status_enum WHERE request_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, requestId);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}