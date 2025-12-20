package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.InvoiceDAO;
import com.example.quanlytoanha.dao.TransactionDAO;
import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.Transaction;
import com.example.quanlytoanha.utils.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InvoiceService {
    private static InvoiceService instance;
    private final InvoiceDAO invoiceDAO;
    private final TransactionDAO transactionDAO;
    private final NotificationService notificationService;

    private InvoiceService() {
        invoiceDAO = InvoiceDAO.getInstance();
        transactionDAO = TransactionDAO.getInstance();
        notificationService = new NotificationService();
    }

    public static InvoiceService getInstance() {
        if (instance == null) {
            instance = new InvoiceService();
        }
        return instance;
    }

    public List<Invoice> getUnpaidInvoices(int residentId) {
        return invoiceDAO.getUnpaidInvoices(residentId);
    }

    public double getTotalDueAmount(int residentId) {
        return getUnpaidInvoices(residentId).stream()
                .mapToDouble(invoice -> invoice.getTotalAmount().doubleValue())
                .sum();
    }

    public List<Transaction> getTransactions(int residentId, LocalDate fromDate, LocalDate toDate) {
        return transactionDAO.getTransactions(residentId, fromDate, toDate);
    }

    public Transaction processPayment(int residentId, Invoice invoice, BigDecimal amount) {
        // FIX: Wrap both operations in a single database transaction for atomicity
        Connection conn = null;
        Transaction transaction = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // 1. Create transaction record
            String insertSql = "INSERT INTO transactions (invoice_id, payer_user_id, amount, transaction_date) " +
                              "VALUES (?, ?, ?, ?) RETURNING transaction_id";
            
            LocalDate transactionDate = LocalDate.now();
            int transactionId;
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, invoice.getInvoiceId());
                pstmt.setInt(2, residentId);
                pstmt.setBigDecimal(3, amount);
                pstmt.setDate(4, Date.valueOf(transactionDate));
                
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    transactionId = rs.getInt("transaction_id");
                    transaction = new Transaction(
                        transactionId,
                        amount,
                        "Payment for invoice #" + invoice.getInvoiceId(),
                        transactionDate.atStartOfDay(),
                        "COMPLETED",
                        "Standard Payment",
                        invoice.getInvoiceId()
                    );
                } else {
                    throw new SQLException("Failed to create transaction, no ID returned");
                }
            }
            
            // 2. Update invoice status to PAID
            String updateSql = "UPDATE invoices SET status = ? WHERE invoice_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, "PAID");
                pstmt.setInt(2, invoice.getInvoiceId());
                
                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new SQLException("Failed to update invoice status, no rows affected");
                }
            }
            
            // Both operations succeeded - commit the transaction
            conn.commit();
            
            // Send notification AFTER successful commit
            notificationService.sendPaymentSuccessNotification(residentId, invoice, amount);
            
            return transaction;
            
        } catch (SQLException e) {
            // Rollback on any error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return null;
            
        } finally {
            // Restore auto-commit and close connection
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

    public String getLastPaymentInfo(int residentId) {
        return transactionDAO.getLastPaymentInfo(residentId);
    }

    public String getTodayPaymentTotal(int residentId) {
        return transactionDAO.getTodayPaymentTotal(residentId);
    }

    public int getUnpaidInvoicesCount(int residentId) {
        return getUnpaidInvoices(residentId).size();
    }
}
