package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.InvoiceDetail;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class InvoiceDAO {
    // ... (các hàm khác)

    /**
     * Đây là hàm quan trọng:
     * Tạo 1 hóa đơn MỚI và các chi tiết của nó trong cùng 1 GIAO DỊCH (Transaction).
     */
    public boolean createInvoiceWithDetails(Invoice invoice, List<InvoiceDetail> details) throws SQLException {
        Connection conn = null;
        PreparedStatement stmtInvoice = null;
        PreparedStatement stmtDetail = null;

        String sqlInvoice = "INSERT INTO invoices (apartment_id, total_amount, due_date, status) VALUES (?, ?, ?, 'UNPAID') RETURNING invoice_id";
        String sqlDetail = "INSERT INTO invoicedetails (invoice_id, name, amount) VALUES (?, ?, ?)";

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Tạo Hóa đơn (Invoice)
            stmtInvoice = conn.prepareStatement(sqlInvoice);
            stmtInvoice.setInt(1, invoice.getApartmentId()); // Cần thêm .setApartmentId() vào model Invoice
            stmtInvoice.setBigDecimal(2, invoice.getTotalAmount());
            stmtInvoice.setDate(3, new java.sql.Date(invoice.getDueDate().getTime()));

            ResultSet rs = stmtInvoice.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Tạo hóa đơn thất bại, không nhận được ID.");
            }
            int newInvoiceId = rs.getInt(1); // Lấy ID vừa tạo

            // 2. Tạo các Chi tiết (InvoiceDetail)
            stmtDetail = conn.prepareStatement(sqlDetail);
            for (InvoiceDetail detail : details) {
                stmtDetail.setInt(1, newInvoiceId);
                stmtDetail.setString(2, detail.getName());
                stmtDetail.setBigDecimal(3, detail.getAmount());
                stmtDetail.addBatch(); // Thêm vào lô
            }

            stmtDetail.executeBatch(); // Thực thi lô

            conn.commit(); // Hoàn tất Transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) conn.rollback(); // Hoàn tác nếu có lỗi
            throw e; // Ném lỗi ra ngoài
        } finally {
            if (stmtInvoice != null) stmtInvoice.close();
            if (stmtDetail != null) stmtDetail.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
