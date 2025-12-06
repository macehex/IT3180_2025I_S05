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

    // ==========================================================
    // BÁO CÁO TÀI SẢN - Các phương thức mới
    // ==========================================================

    /**
     * BÁO CÁO THEO TÌNH TRẠNG: Đếm số lượng tài sản theo từng tình trạng.
     * @return Map với key là status, value là số lượng
     */
    public java.util.Map<String, Integer> getAssetCountByStatus() throws SQLException {
        java.util.Map<String, Integer> statusCounts = new java.util.HashMap<>();
        String sql = "SELECT status, COUNT(*) as count FROM assets GROUP BY status ORDER BY status";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                statusCounts.put(status, count);
            }
        }
        return statusCounts;
    }

    /**
     * BÁO CÁO THEO VỊ TRÍ: Đếm số lượng tài sản theo từng vị trí.
     * @return Map với key là location, value là số lượng
     */
    public java.util.Map<String, Integer> getAssetCountByLocation() throws SQLException {
        java.util.Map<String, Integer> locationCounts = new java.util.HashMap<>();
        String sql = "SELECT location, COUNT(*) as count FROM assets GROUP BY location ORDER BY location";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String location = rs.getString("location");
                int count = rs.getInt("count");
                locationCounts.put(location != null ? location : "Chưa xác định", count);
            }
        }
        return locationCounts;
    }

    /**
     * BÁO CÁO CHI PHÍ BẢO TRÌ THEO TÀI SẢN: Tổng chi phí bảo trì cho mỗi tài sản.
     * @return Map với key là assetId, value là tổng chi phí bảo trì
     */
    public java.util.Map<Integer, java.math.BigDecimal> getMaintenanceCostByAsset() throws SQLException {
        java.util.Map<Integer, java.math.BigDecimal> costMap = new java.util.HashMap<>();
        String sql = "SELECT asset_id, COALESCE(SUM(cost), 0) as total_cost " +
                     "FROM maintenance_history " +
                     "WHERE cost IS NOT NULL " +
                     "GROUP BY asset_id " +
                     "ORDER BY asset_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int assetId = rs.getInt("asset_id");
                java.math.BigDecimal totalCost = rs.getBigDecimal("total_cost");
                costMap.put(assetId, totalCost);
            }
        }
        return costMap;
    }

    /**
     * BÁO CÁO CHI PHÍ BẢO TRÌ THEO VỊ TRÍ: Tổng chi phí bảo trì cho mỗi vị trí.
     * @return Map với key là location, value là tổng chi phí bảo trì
     */
    public java.util.Map<String, java.math.BigDecimal> getMaintenanceCostByLocation() throws SQLException {
        java.util.Map<String, java.math.BigDecimal> costMap = new java.util.HashMap<>();
        String sql = "SELECT a.location, COALESCE(SUM(mh.cost), 0) as total_cost " +
                     "FROM assets a " +
                     "LEFT JOIN maintenance_history mh ON a.asset_id = mh.asset_id " +
                     "WHERE mh.cost IS NOT NULL " +
                     "GROUP BY a.location " +
                     "ORDER BY a.location";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String location = rs.getString("location");
                java.math.BigDecimal totalCost = rs.getBigDecimal("total_cost");
                costMap.put(location != null ? location : "Chưa xác định", totalCost);
            }
        }
        return costMap;
    }

    /**
     * BÁO CÁO CHI PHÍ BẢO TRÌ THEO TÌNH TRẠNG: Tổng chi phí bảo trì cho mỗi tình trạng.
     * @return Map với key là status, value là tổng chi phí bảo trì
     */
    public java.util.Map<String, java.math.BigDecimal> getMaintenanceCostByStatus() throws SQLException {
        java.util.Map<String, java.math.BigDecimal> costMap = new java.util.HashMap<>();
        String sql = "SELECT a.status, COALESCE(SUM(mh.cost), 0) as total_cost " +
                     "FROM assets a " +
                     "LEFT JOIN maintenance_history mh ON a.asset_id = mh.asset_id " +
                     "WHERE mh.cost IS NOT NULL " +
                     "GROUP BY a.status " +
                     "ORDER BY a.status";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String status = rs.getString("status");
                java.math.BigDecimal totalCost = rs.getBigDecimal("total_cost");
                costMap.put(status, totalCost);
            }
        }
        return costMap;
    }

    /**
     * TỔNG CHI PHÍ BẢO TRÌ: Tổng chi phí bảo trì của tất cả tài sản.
     * @return Tổng chi phí bảo trì
     */
    public java.math.BigDecimal getTotalMaintenanceCost() throws SQLException {
        String sql = "SELECT COALESCE(SUM(cost), 0) as total_cost FROM maintenance_history WHERE cost IS NOT NULL";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getBigDecimal("total_cost");
            }
        }
        return java.math.BigDecimal.ZERO;
    }

    /**
     * TỔNG SỐ TÀI SẢN: Đếm tổng số tài sản trong hệ thống.
     * @return Tổng số tài sản
     */
    public int getTotalAssetCount() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM assets";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * TỔNG GIÁ TRỊ BAN ĐẦU: Tổng giá trị ban đầu của tất cả tài sản.
     * @return Tổng giá trị ban đầu
     */
    public java.math.BigDecimal getTotalInitialCost() throws SQLException {
        String sql = "SELECT COALESCE(SUM(initial_cost), 0) as total_cost FROM assets";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getBigDecimal("total_cost");
            }
        }
        return java.math.BigDecimal.ZERO;
    }

    public List<Asset> searchAssets(String keyword, String status) {
        List<Asset> assets = new ArrayList<>();

        // Câu lệnh SQL cơ bản
        StringBuilder sql = new StringBuilder("SELECT * FROM assets WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // 1. Nếu có từ khóa tìm kiếm (tìm theo Tên hoặc Vị trí)
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Dùng ILIKE trong PostgreSQL để không phân biệt hoa thường
            sql.append(" AND (asset_type ILIKE ? OR location ILIKE ?)");
            String searchPattern = "%" + keyword.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        // 2. Nếu có lọc theo trạng thái (và không phải là "Tất cả")
        if (status != null && !status.isEmpty() && !status.equals("Tất cả")) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY asset_id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Gán các tham số vào dấu ?
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    assets.add(mapResultSetToAsset(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assets;
    }

    /**
     * XÓA: Xóa tài sản khỏi CSDL.
     * Lưu ý: Sẽ thất bại nếu tài sản đã có dữ liệu liên quan (Lịch sử bảo trì, v.v.)
     * @param assetId ID tài sản cần xóa
     * @return true nếu xóa thành công
     */
    public boolean deleteAsset(int assetId) throws SQLException {
        String sql = "DELETE FROM assets WHERE asset_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, assetId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            // Kiểm tra lỗi ràng buộc khóa ngoại (Foreign Key Violation)
            // Mã lỗi 23503 trong PostgreSQL là foreign_key_violation
            if ("23503".equals(e.getSQLState())) {
                throw new SQLException("Không thể xóa tài sản này vì đang có dữ liệu liên quan (Lịch sử bảo trì, Yêu cầu...). Hãy thử chuyển trạng thái sang 'DISPOSED' thay vì xóa.");
            }
            throw e; // Ném các lỗi khác ra ngoài
        }
    }
}
