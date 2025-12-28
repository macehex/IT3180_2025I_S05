// Vị trí: src/main/java/com/example/quanlytoanha/dao/ResidentDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.utils.DatabaseConnection; // Lớp tiện ích hiện có
import com.example.quanlytoanha.model.Role;
import java.sql.*;
import java.util.Map; // <-- BỔ SUNG: Import cho hàm Báo cáo
import java.util.HashMap; // <-- BỔ SUNG: Import cho hàm Báo cáo
import java.util.List;
import  java.util.ArrayList;

public class ResidentDAO {

    /**
     * Kiểm tra Số căn cước (idCardNumber) đã tồn tại chưa trong DB.
     * @param idCardNumber Số CCCD cần kiểm tra.
     * @return true nếu số CCCD này duy nhất (chưa tồn tại), false nếu đã tồn tại.
     */
    public boolean isIdCardUnique(String idCardNumber) throws SQLException {
        if (idCardNumber == null || idCardNumber.trim().isEmpty()) return true;

        String SQL = "SELECT resident_id FROM residents WHERE id_card_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setString(1, idCardNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                return !rs.next(); // Trả về TRUE nếu KHÔNG tìm thấy
            }
        }
    }

    /**
     * Kiểm tra ID Căn hộ có tồn tại không.
     * @param apartmentId ID Căn hộ.
     * @return true nếu căn hộ tồn tại.
     */
    public boolean isApartmentExist(int apartmentId) throws SQLException {
        // Giả định có bảng 'apartments'
        String SQL = "SELECT apartment_id FROM apartments WHERE apartment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, apartmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Thêm cư dân mới vào bảng 'residents'.
     * @param resident Đối tượng Resident chứa dữ liệu cần lưu.
     * @return true nếu thêm thành công.
     */
    public boolean addResident(Resident resident) throws SQLException {
        // Đã cập nhật để bao gồm move_in_date
        String SQL = "INSERT INTO residents (apartment_id, user_id, full_name, date_of_birth, id_card_number, relationship, move_in_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, resident.getApartmentId());

            if (resident.getUserId() > 0) {
                pstmt.setInt(2, resident.getUserId());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }

            pstmt.setString(3, resident.getFullName());

            if (resident.getDateOfBirth() != null) {
                pstmt.setDate(4, new java.sql.Date(resident.getDateOfBirth().getTime()));
            } else {
                pstmt.setNull(4, java.sql.Types.DATE);
            }

            pstmt.setString(5, resident.getIdCardNumber());
            pstmt.setString(6, resident.getRelationship());

            if (resident.getMoveInDate() != null) {
                pstmt.setDate(7, new java.sql.Date(resident.getMoveInDate().getTime()));
            } else {
                pstmt.setDate(7, new java.sql.Date(System.currentTimeMillis()));
            }

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Lấy tất cả thông tin Resident (bao gồm cả thông tin Users liên quan).
     * @return List<Resident>
     */
    public java.util.List<Resident> getAllResidents() throws SQLException {
        java.util.List<Resident> residents = new java.util.ArrayList<>();

        // Sử dụng LEFT JOIN để lấy cả cư dân có và không có tài khoản user
        String SQL = "SELECT u.user_id, u.username, u.email, u.full_name, u.role_id, u.created_at, u.last_login, u.phone_number, " +
                "r.resident_id, r.apartment_id, r.date_of_birth, r.id_card_number, r.relationship, r.status, r.move_in_date, r.move_out_date " + // <--- Đã thêm r.move_out_date
                "FROM residents r LEFT JOIN users u ON r.user_id = u.user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL);
             ResultSet rs = pstmt.executeQuery()) {

            // Lặp qua kết quả và tạo đối tượng Resident
            while (rs.next()) {
                Resident resident = mapResultSetToResident(rs);
                residents.add(resident);
            }
        }
        return residents;
    }

    /**
     * Tìm kiếm cư dân theo tên, căn hộ và trạng thái
     */
    public java.util.List<Resident> searchResidents(String name, Integer apartmentId, String status) throws SQLException {
        java.util.List<Resident> residents = new java.util.ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.user_id, u.username, u.email, u.full_name, u.role_id, u.created_at, u.last_login, u.phone_number, ");
        sql.append("r.resident_id, r.apartment_id, r.date_of_birth, r.id_card_number, r.relationship, r.status, r.move_in_date, r.move_out_date "); // <--- Đã thêm r.move_out_date
        sql.append("FROM residents r LEFT JOIN users u ON r.user_id = u.user_id WHERE 1=1");

        java.util.List<Object> parameters = new java.util.ArrayList<>();

        if (name != null && !name.trim().isEmpty()) {
            // Sử dụng ILIKE để không phân biệt hoa/thường
            // Hỗ trợ tìm kiếm không dấu bằng cách sử dụng hàm translate để loại bỏ dấu tiếng Việt
            String searchPattern = "%" + name.trim() + "%";
            
            // So sánh với tên gốc (có dấu) và tên không dấu
            // Sử dụng TRANSLATE để loại bỏ dấu tiếng Việt
            sql.append(" AND (u.full_name ILIKE ? OR r.full_name ILIKE ? OR ");
            sql.append("TRANSLATE(LOWER(COALESCE(u.full_name, '')), 'áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ', 'aaaaaaaaaaaaaaaaaeeeeeeeeeeiiiiioooooooooooooouuuuuuuuuuuyyyyyyd') ILIKE TRANSLATE(LOWER(?), 'áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ', 'aaaaaaaaaaaaaaaaaeeeeeeeeeeiiiiioooooooooooooouuuuuuuuuuuyyyyyyd') OR ");
            sql.append("TRANSLATE(LOWER(COALESCE(r.full_name, '')), 'áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ', 'aaaaaaaaaaaaaaaaaeeeeeeeeeeiiiiioooooooooooooouuuuuuuuuuuyyyyyyd') ILIKE TRANSLATE(LOWER(?), 'áàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ', 'aaaaaaaaaaaaaaaaaeeeeeeeeeeiiiiioooooooooooooouuuuuuuuuuuyyyyyyd'))");
            parameters.add(searchPattern);
            parameters.add(searchPattern);
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }

        if (apartmentId != null) {
            sql.append(" AND r.apartment_id = ?");
            parameters.add(apartmentId);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND r.status = ?");
            parameters.add(status);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Resident resident = mapResultSetToResident(rs);
                    residents.add(resident);
                }
            }
        }

        return residents;
    }

    /**
     * (Hàm này đã được sửa thành public ở bước trước)
     */
    public Resident mapResultSetToResident(ResultSet rs) throws SQLException {
        Resident resident = new Resident();

        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            resident.setUserId(userId);
            resident.setUsername(rs.getString("username"));
            resident.setEmail(rs.getString("email"));
            resident.setPhoneNumber(rs.getString("phone_number"));

            int roleId = rs.getInt("role_id");
            if (!rs.wasNull() && roleId > 0) {
                try {
                    resident.setRole(Role.fromId(roleId));
                } catch (IllegalArgumentException e) {
                    resident.setRole(Role.RESIDENT);
                }
            } else {
                resident.setRole(Role.RESIDENT);
            }

            resident.setCreatedAt(rs.getTimestamp("created_at"));
            resident.setLastLogin(rs.getTimestamp("last_login"));
        }

        resident.setResidentId(rs.getInt("resident_id"));
        resident.setApartmentId(rs.getInt("apartment_id"));
        resident.setIdCardNumber(rs.getString("id_card_number"));
        resident.setRelationship(rs.getString("relationship"));
        resident.setStatus(rs.getString("status"));

        String fullName = rs.getString("full_name");
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "Cư dân #" + resident.getResidentId();
        }
        resident.setFullName(fullName);

        resident.setDateOfBirth(rs.getDate("date_of_birth"));
        resident.setMoveInDate(rs.getDate("move_in_date"));

        return resident;
    }

    /**
     * Xóa cư dân (Giữ nguyên)
     */
    public boolean removeResident(int residentId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String checkSQL = "SELECT resident_id FROM residents WHERE resident_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
                checkStmt.setInt(1, residentId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            String deleteVehiclesSQL = "DELETE FROM vehicles WHERE resident_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteVehiclesSQL)) {
                pstmt.setInt(1, residentId);
                pstmt.executeUpdate();
            }

            String deleteResidentSQL = "DELETE FROM residents WHERE resident_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteResidentSQL)) {
                pstmt.setInt(1, residentId);
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Lỗi khi rollback transaction: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Lỗi khi đóng connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Xóa cư dân theo user ID (Giữ nguyên)
     */
    public boolean removeResidentByUserId(int userId) throws SQLException {
        String findResidentSQL = "SELECT resident_id FROM residents WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(findResidentSQL)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int residentId = rs.getInt("resident_id");
                    return removeResident(residentId);
                } else {
                    return false;
                }
            }
        }
    }

    /**
     * Lấy Resident đầy đủ từ DB để nạp vào form Edit (Giữ nguyên)
     */
    public Resident getResidentByUserId(int userId) throws SQLException {
        Resident resident = null;
        String SQL = "SELECT u.*, r.* FROM users u " +
                "JOIN residents r ON u.user_id = r.user_id " +
                "WHERE u.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    resident = mapResultSetToResident(rs);
                }
                return resident;
            }
        }
    }

    // --- BỔ SUNG: HÀM MỚI CHO BÁO CÁO (US7_2_1) ---

    /**
     * BÁO CÁO BIẾN ĐỘNG DÂN CƯ (US7_2_1):
     * Lấy thống kê số lượng cư dân chuyển vào (move_in_date)
     * và chuyển đi (move_out_date) trong một khoảng thời gian.
     * @param startDate Ngày bắt đầu (java.sql.Date)
     * @param endDate Ngày kết thúc (java.sql.Date)
     * @return Map chứa ("moveIns" -> count) và ("moveOuts" -> count)
     */
    public Map<String, Integer> getPopulationChangeStats(java.sql.Date startDate, java.sql.Date endDate) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();

        // Sử dụng Conditional Aggregation để đếm trong 1 truy vấn
        // Sửa để đảm bảo lấy đúng dữ liệu trong khoảng thời gian (bao gồm cả ngày bắt đầu và kết thúc)
        String sql = "SELECT " +
                "    COUNT(CASE WHEN move_in_date >= ? AND move_in_date <= ? THEN 1 END) AS move_ins, " +
                "    COUNT(CASE WHEN move_out_date >= ? AND move_out_date <= ? THEN 1 END) AS move_outs " +
                "FROM residents " +
                "WHERE (move_in_date >= ? AND move_in_date <= ?) OR (move_out_date >= ? AND move_out_date <= ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set tham số cho move_in_date trong COUNT
            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);

            // Set tham số cho move_out_date trong COUNT
            pstmt.setDate(3, startDate);
            pstmt.setDate(4, endDate);

            // Set tham số cho WHERE clause
            pstmt.setDate(5, startDate);
            pstmt.setDate(6, endDate);
            pstmt.setDate(7, startDate);
            pstmt.setDate(8, endDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("moveIns", rs.getInt("move_ins"));
                    stats.put("moveOuts", rs.getInt("move_outs"));
                }
            }
        }
        return stats;
    }

    public boolean updateStatus(int residentId, String newStatus) throws SQLException {
        String sql;
        // Logic thông minh: Nếu chuyển đi -> Ghi nhận ngày đi. Nếu quay lại -> Xóa ngày đi.
        if ("MOVED_OUT".equals(newStatus)) {
            sql = "UPDATE residents SET status = ?, move_out_date = CURRENT_DATE WHERE resident_id = ?";
        } else {
            sql = "UPDATE residents SET status = ?, move_out_date = NULL WHERE resident_id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, residentId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Lấy danh sách cư dân có phân trang.
     * @param limit Số lượng bản ghi mỗi trang.
     * @param offset Vị trí bắt đầu lấy.
     */
    public List<Resident> getResidentsByPage(int limit, int offset) throws SQLException {
        List<Resident> residents = new ArrayList<>();
        // Câu SQL cũ thêm LIMIT và OFFSET
        String SQL = "SELECT u.user_id, u.username, u.email, u.full_name, u.role_id, u.created_at, u.last_login, u.phone_number, " +
                "r.resident_id, r.apartment_id, r.date_of_birth, r.id_card_number, r.relationship, r.status, r.move_in_date, r.move_out_date " +
                "FROM residents r LEFT JOIN users u ON r.user_id = u.user_id " +
                "ORDER BY r.resident_id DESC LIMIT ? OFFSET ?"; // Sắp xếp để dữ liệu ổn định

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    residents.add(mapResultSetToResident(rs));
                }
            }
        }
        return residents;
    }

    /**
     * Đếm tổng số lượng cư dân (để tính số trang).
     */
    public int countTotalResidents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM residents";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}