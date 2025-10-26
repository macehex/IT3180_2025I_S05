package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardDAO {

    // Lấy tổng số cư dân đang ở (status='RESIDING')
    public int getTotalActiveResidents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM residents WHERE status = 'RESIDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // Lấy tổng số căn hộ
    public int getTotalApartments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM apartments";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // Lấy tổng số tiền công nợ (hóa đơn chưa trả)
    public BigDecimal getTotalUnpaidDebt() throws SQLException {
        // COALESCE để trả về 0 nếu không có hóa đơn nào
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM invoices WHERE status = 'UNPAID'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO; // Trả về 0 nếu có lỗi
    }

    // Lấy tổng số hóa đơn chưa trả
    public int getTotalUnpaidInvoices() throws SQLException {
        String sql = "SELECT COUNT(*) FROM invoices WHERE status = 'UNPAID'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}