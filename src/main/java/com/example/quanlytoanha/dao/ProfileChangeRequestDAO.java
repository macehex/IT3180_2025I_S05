package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.ProfileChangeRequest;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileChangeRequestDAO {

    /**
     * Create a new profile change request
     */
    public boolean createProfileChangeRequest(ProfileChangeRequest request) {
        String sql = """
            INSERT INTO profile_change_requests (
                user_id, request_type, status, created_at,
                current_username, current_phone_number, current_email, current_full_name,
                current_relationship, current_date_of_birth, current_id_card_number,
                new_username, new_phone_number, new_email, new_full_name,
                new_relationship, new_date_of_birth, new_id_card_number
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, request.getUserId());
            pstmt.setString(2, request.getRequestType());
            pstmt.setString(3, request.getStatus());
            pstmt.setTimestamp(4, request.getCreatedAt());
            
            // Current values
            pstmt.setString(5, request.getCurrentUsername());
            pstmt.setString(6, request.getCurrentPhoneNumber());
            pstmt.setString(7, request.getCurrentEmail());
            pstmt.setString(8, request.getCurrentFullName());
            pstmt.setString(9, request.getCurrentRelationship());
            pstmt.setDate(10, request.getCurrentDateOfBirth());
            pstmt.setString(11, request.getCurrentIdCardNumber());
            
            // New values
            pstmt.setString(12, request.getNewUsername());
            pstmt.setString(13, request.getNewPhoneNumber());
            pstmt.setString(14, request.getNewEmail());
            pstmt.setString(15, request.getNewFullName());
            pstmt.setString(16, request.getNewRelationship());
            pstmt.setDate(17, request.getNewDateOfBirth());
            pstmt.setString(18, request.getNewIdCardNumber());

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error creating profile change request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all pending profile change requests for admin review
     */
    public List<ProfileChangeRequest> getAllPendingRequests() {
        List<ProfileChangeRequest> requests = new ArrayList<>();
        String sql = """
            SELECT pcr.*, u.full_name as requester_full_name, r.apartment_id
            FROM profile_change_requests pcr
            JOIN users u ON pcr.user_id = u.user_id
            LEFT JOIN residents r ON u.user_id = r.user_id
            WHERE pcr.status = 'PENDING'
            ORDER BY pcr.created_at DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ProfileChangeRequest request = mapResultSetToRequest(rs);
                request.setRequesterFullName(rs.getString("requester_full_name"));
                request.setApartmentId(rs.getInt("apartment_id"));
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Get all profile change requests (for admin to see history)
     */
    public List<ProfileChangeRequest> getAllRequests() {
        List<ProfileChangeRequest> requests = new ArrayList<>();
        String sql = """
            SELECT pcr.*, u.full_name as requester_full_name, r.apartment_id
            FROM profile_change_requests pcr
            JOIN users u ON pcr.user_id = u.user_id
            LEFT JOIN residents r ON u.user_id = r.user_id
            ORDER BY pcr.created_at DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ProfileChangeRequest request = mapResultSetToRequest(rs);
                request.setRequesterFullName(rs.getString("requester_full_name"));
                request.setApartmentId(rs.getInt("apartment_id"));
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Get profile change requests by user ID
     */
    public List<ProfileChangeRequest> getRequestsByUserId(int userId) {
        List<ProfileChangeRequest> requests = new ArrayList<>();
        String sql = """
            SELECT pcr.*, u.full_name as requester_full_name, r.apartment_id
            FROM profile_change_requests pcr
            JOIN users u ON pcr.user_id = u.user_id
            LEFT JOIN residents r ON u.user_id = r.user_id
            WHERE pcr.user_id = ?
            ORDER BY pcr.created_at DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ProfileChangeRequest request = mapResultSetToRequest(rs);
                request.setRequesterFullName(rs.getString("requester_full_name"));
                request.setApartmentId(rs.getInt("apartment_id"));
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Approve a profile change request and apply the changes
     */
    public boolean approveRequest(int requestId, int adminUserId, String adminComment) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // First, get the request details
            ProfileChangeRequest request = getRequestById(requestId);
            if (request == null || !"PENDING".equals(request.getStatus())) {
                conn.rollback();
                return false;
            }

            // Update the request status
            String updateRequestSql = """
                UPDATE profile_change_requests 
                SET status = 'APPROVED', processed_at = ?, processed_by = ?, admin_comment = ?
                WHERE request_id = ?
                """;

            try (PreparedStatement pstmt = conn.prepareStatement(updateRequestSql)) {
                pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                pstmt.setInt(2, adminUserId);
                pstmt.setString(3, adminComment);
                pstmt.setInt(4, requestId);
                pstmt.executeUpdate();
            }

            // Apply changes to users table
            String updateUserSql = """
                UPDATE users 
                SET username = ?, phone_number = ?, email = ?, full_name = ?
                WHERE user_id = ?
                """;

            try (PreparedStatement pstmt = conn.prepareStatement(updateUserSql)) {
                pstmt.setString(1, request.getNewUsername());
                pstmt.setString(2, request.getNewPhoneNumber());
                pstmt.setString(3, request.getNewEmail());
                pstmt.setString(4, request.getNewFullName());
                pstmt.setInt(5, request.getUserId());
                pstmt.executeUpdate();
            }

            // Apply changes to residents table
            String updateResidentSql = """
                UPDATE residents 
                SET relationship = ?, date_of_birth = ?, id_card_number = ?, full_name = ?
                WHERE user_id = ?
                """;

            try (PreparedStatement pstmt = conn.prepareStatement(updateResidentSql)) {
                pstmt.setString(1, request.getNewRelationship());
                pstmt.setDate(2, request.getNewDateOfBirth());
                pstmt.setString(3, request.getNewIdCardNumber());
                pstmt.setString(4, request.getNewFullName());
                pstmt.setInt(5, request.getUserId());
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Reject a profile change request
     */
    public boolean rejectRequest(int requestId, int adminUserId, String adminComment) {
        String sql = """
            UPDATE profile_change_requests 
            SET status = 'REJECTED', processed_at = ?, processed_by = ?, admin_comment = ?
            WHERE request_id = ? AND status = 'PENDING'
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(2, adminUserId);
            pstmt.setString(3, adminComment);
            pstmt.setInt(4, requestId);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get a specific request by ID
     */
    public ProfileChangeRequest getRequestById(int requestId) {
        String sql = """
            SELECT pcr.*, u.full_name as requester_full_name, r.apartment_id
            FROM profile_change_requests pcr
            JOIN users u ON pcr.user_id = u.user_id
            LEFT JOIN residents r ON u.user_id = r.user_id
            WHERE pcr.request_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, requestId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                ProfileChangeRequest request = mapResultSetToRequest(rs);
                request.setRequesterFullName(rs.getString("requester_full_name"));
                request.setApartmentId(rs.getInt("apartment_id"));
                return request;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Helper method to map ResultSet to ProfileChangeRequest
     */
    private ProfileChangeRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        ProfileChangeRequest request = new ProfileChangeRequest();
        
        request.setRequestId(rs.getInt("request_id"));
        request.setUserId(rs.getInt("user_id"));
        request.setRequestType(rs.getString("request_type"));
        
        // Debug the status field
        String status = rs.getString("status");
        System.out.println("DEBUG: Mapping request ID " + rs.getInt("request_id") + " with status: " + status);
        request.setStatus(status);
        
        request.setCreatedAt(rs.getTimestamp("created_at"));
        request.setProcessedAt(rs.getTimestamp("processed_at"));
        request.setProcessedBy(rs.getInt("processed_by"));
        request.setAdminComment(rs.getString("admin_comment"));
        
        // Current values
        request.setCurrentUsername(rs.getString("current_username"));
        request.setCurrentPhoneNumber(rs.getString("current_phone_number"));
        request.setCurrentEmail(rs.getString("current_email"));
        request.setCurrentFullName(rs.getString("current_full_name"));
        request.setCurrentRelationship(rs.getString("current_relationship"));
        request.setCurrentDateOfBirth(rs.getDate("current_date_of_birth"));
        request.setCurrentIdCardNumber(rs.getString("current_id_card_number"));
        
        // New values
        request.setNewUsername(rs.getString("new_username"));
        request.setNewPhoneNumber(rs.getString("new_phone_number"));
        request.setNewEmail(rs.getString("new_email"));
        request.setNewFullName(rs.getString("new_full_name"));
        request.setNewRelationship(rs.getString("new_relationship"));
        request.setNewDateOfBirth(rs.getDate("new_date_of_birth"));
        request.setNewIdCardNumber(rs.getString("new_id_card_number"));
        
        return request;
    }

    /**
     * Check if user has any pending profile change request
     */
    public boolean hasPendingRequest(int userId) {
        String sql = "SELECT COUNT(*) FROM profile_change_requests WHERE user_id = ? AND status = 'PENDING'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
}