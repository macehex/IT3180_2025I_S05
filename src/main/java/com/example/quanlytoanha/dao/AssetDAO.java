package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Asset;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal; // Import cho kiểu DECIMAL

public class AssetDAO {

    /**
     * TẠO MỚI: Thêm một tài sản mới vào CSDL.
     * @param asset Đối tượng Asset cần lưu.
     * @return ID của tài sản vừa tạo, hoặc -1 nếu thất bại.
     */
    public int createAsset(Asset asset) {
        // SQL: Bảng assets
        String sql = "INSERT INTO assets (asset_type, description, location, status, purchase_date, initial_cost) VALUES (?, ?, ?, ?, ?, ?) RETURNING asset_id";
        int newAssetId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, asset.getAssetType());
            pstmt.setString(2, asset.getDescription());
            pstmt.setString(3, asset.getLocation());
            pstmt.setString(4, asset.getStatus());
            // Chuyển java.util.Date sang java.sql.Date
            pstmt.setDate(5, (asset.getPurchaseDate() != null) ? new Date(asset.getPurchaseDate().getTime()) : null);
            pstmt.setBigDecimal(6, asset.getInitialCost());

            pstmt.executeUpdate();

            // Lấy ID tự tăng
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    newAssetId = rs.getInt(1);
                    asset.setAssetId(newAssetId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newAssetId;
    }

    /**
     * CẬP NHẬT: Cập nhật thông tin chi tiết của tài sản (trừ Status).
     * @param asset Đối tượng Asset chứa dữ liệu mới.
     * @return true nếu cập nhật thành công.
     */
    public boolean updateAsset(Asset asset) {
        String sql = "UPDATE assets SET asset_type = ?, description = ?, location = ?, purchase_date = ?, initial_cost = ? WHERE asset_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, asset.getAssetType());
            pstmt.setString(2, asset.getDescription());
            pstmt.setString(3, asset.getLocation());
            pstmt.setDate(4, (asset.getPurchaseDate() != null) ? new Date(asset.getPurchaseDate().getTime()) : null);
            pstmt.setBigDecimal(5, asset.getInitialCost());
            pstmt.setInt(6, asset.getAssetId());

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * CẬP NHẬT TRẠNG THÁI: Thay đổi trạng thái tài sản và ghi log lịch sử (Hàm cốt lõi của US2_1_1)
     * @param assetId ID tài sản
     * @param newStatus Trạng thái mới
     * @param changedByUserId ID người dùng thay đổi
     * @param notes Ghi chú (nếu có)
     * @return true nếu cập nhật thành công.
     */
    public boolean updateAssetStatusAndLog(int assetId, String newStatus, int changedByUserId, String notes) throws SQLException {
        Connection conn = null;
        String oldStatus = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Lấy trạng thái cũ
            String getStatusSql = "SELECT status FROM assets WHERE asset_id = ?";
            try (PreparedStatement getStmt = conn.prepareStatement(getStatusSql)) {
                getStmt.setInt(1, assetId);
                try (ResultSet rs = getStmt.executeQuery()) {
                    if (rs.next()) {
                        oldStatus = rs.getString("status");
                    } else {
                        throw new SQLException("Không tìm thấy Asset ID: " + assetId);
                    }
                }
            }

            // Thoát nếu trạng thái không thay đổi
            if (oldStatus.equals(newStatus)) {
                conn.commit();
                return true;
            }

            // 2. Cập nhật trạng thái mới
            String updateSql = "UPDATE assets SET status = ? WHERE asset_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newStatus);
                updateStmt.setInt(2, assetId);
                if (updateStmt.executeUpdate() == 0) {
                    throw new SQLException("Cập nhật trạng thái tài sản thất bại.");
                }
            }

            // 3. Ghi lịch sử thay đổi (Audit Log)
            String logSql = "INSERT INTO asset_status_history (asset_id, changed_by_user_id, old_status, new_status, notes) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement logStmt = conn.prepareStatement(logSql)) {
                logStmt.setInt(1, assetId);
                logStmt.setInt(2, changedByUserId);
                logStmt.setString(3, oldStatus);
                logStmt.setString(4, newStatus);
                logStmt.setString(5, notes);
                logStmt.executeUpdate();
            }

            conn.commit(); // Hoàn tất Transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            e.printStackTrace();
            throw e; // Ném lỗi để tầng service xử lý
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * ĐỌC: Lấy chi tiết một tài sản bằng ID.
     */
    public Asset getAssetById(int assetId) {
        Asset asset = null;
        String sql = "SELECT * FROM assets WHERE asset_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, assetId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    asset = mapResultSetToAsset(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return asset;
    }

    /**
     * ĐỌC: Lấy danh sách tất cả tài sản.
     */
    public List<Asset> getAllAssets() {
        List<Asset> assets = new ArrayList<>();
        String sql = "SELECT * FROM assets ORDER BY asset_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                assets.add(mapResultSetToAsset(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assets;
    }

    // --- Hàm tiện ích ánh xạ ---
    private Asset mapResultSetToAsset(ResultSet rs) throws SQLException {
        Asset asset = new Asset();
        asset.setAssetId(rs.getInt("asset_id"));
        asset.setAssetType(rs.getString("asset_type"));
        asset.setDescription(rs.getString("description"));
        asset.setLocation(rs.getString("location"));
        asset.setStatus(rs.getString("status"));
        asset.setPurchaseDate(rs.getDate("purchase_date"));
        asset.setInitialCost(rs.getBigDecimal("initial_cost"));
        return asset;
    }
    /**
     * THỐNG KÊ DASHBOARD: Đếm số lượng tài sản đang gặp sự cố.
     * Sự cố = BROKEN (Hư hỏng) hoặc IN_MAINTENANCE (Đang bảo trì).
     * @return Số lượng tài sản cần xử lý.
     */
    public int countTroubleAssets() {
        String sql = "SELECT COUNT(*) FROM assets WHERE status = 'BROKEN' OR status = 'IN_MAINTENANCE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
