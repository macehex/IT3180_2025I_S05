package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.InvoiceDetail;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceDAO {
    private static InvoiceDAO instance;

    private InvoiceDAO() {}

    public static InvoiceDAO getInstance() {
        if (instance == null) {
            instance = new InvoiceDAO();
        }
        return instance;
    }

    public List<Invoice> getUnpaidInvoices(int residentId) {
        Map<Integer, Invoice> invoices = new HashMap<>();

        String sql = "SELECT i.*, id.invoice_detail_id, id.name, id.amount as detail_amount " +
                    "FROM invoices i " +
                    "LEFT JOIN invoicedetails id ON i.invoice_id = id.invoice_id " +
                    "JOIN residents r ON i.apartment_id = r.apartment_id " +
                    "WHERE r.user_id = ? AND i.status = 'UNPAID' " +
                    "ORDER BY i.due_date";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, residentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int invoiceId = rs.getInt("invoice_id");
                final ResultSet finalRs = rs;

                // Get or create invoice
                Invoice invoice = invoices.computeIfAbsent(invoiceId, k -> {
                    Invoice newInvoice = new Invoice();
                    try {
                        newInvoice.setInvoiceId(invoiceId);
                        newInvoice.setApartmentId(finalRs.getInt("apartment_id"));
                        newInvoice.setTotalAmount(finalRs.getDouble("total_amount"));
                        newInvoice.setDueDate(finalRs.getDate("due_date").toLocalDate());
                        newInvoice.setStatus(finalRs.getString("status"));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return newInvoice;
                });

                // Add invoice detail if it exists
                int detailId = rs.getInt("invoice_detail_id");
                if (!rs.wasNull()) {
                    InvoiceDetail detail = new InvoiceDetail(
                        detailId,
                        invoiceId,
                        rs.getString("name"),
                        rs.getDouble("detail_amount")
                    );
                    invoice.getDetails().add(detail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(invoices.values());
    }

    public boolean updateInvoiceStatus(int invoiceId, String status) {
        String sql = "UPDATE invoices SET status = ? WHERE invoice_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, invoiceId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<InvoiceDetail> getInvoiceDetails(int invoiceId) {
        List<InvoiceDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM invoicedetails WHERE invoice_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                InvoiceDetail detail = new InvoiceDetail(
                    rs.getInt("invoice_detail_id"),
                    invoiceId,
                    rs.getString("name"),
                    rs.getDouble("amount")
                );
                details.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details;
    }
}
