package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Transaction;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private static TransactionDAO instance;

    private TransactionDAO() {}

    public static TransactionDAO getInstance() {
        if (instance == null) {
            instance = new TransactionDAO();
        }
        return instance;
    }

    public List<Transaction> getTransactions(int userId, LocalDate fromDate, LocalDate toDate) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE payer_user_id = ? AND transaction_date BETWEEN ? AND ? " +
                    "ORDER BY transaction_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(fromDate));
            pstmt.setDate(3, Date.valueOf(toDate));

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Transaction transaction = new Transaction(
                    rs.getInt("transaction_id"),
                    rs.getBigDecimal("amount"),
                    "Payment for invoice #" + rs.getInt("invoice_id"),
                    rs.getTimestamp("transaction_date").toLocalDateTime(),
                    "COMPLETED",
                    "Standard Payment",
                    rs.getInt("invoice_id")
                );
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public Transaction createTransaction(int payerUserId, int invoiceId, BigDecimal amount) {
        String sql = "INSERT INTO transactions (invoice_id, payer_user_id, amount, transaction_date) " +
                    "VALUES (?, ?, ?, ?) RETURNING transaction_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            LocalDate transactionDate = LocalDate.now();
            pstmt.setInt(1, invoiceId);
            pstmt.setInt(2, payerUserId);
            pstmt.setBigDecimal(3, amount);
            pstmt.setDate(4, Date.valueOf(transactionDate));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Transaction(
                    rs.getInt("transaction_id"),
                    amount,
                    "Payment for invoice #" + invoiceId,
                    Timestamp.valueOf(transactionDate.atStartOfDay()).toLocalDateTime(),
                    "COMPLETED",
                    "Standard Payment",
                    invoiceId
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getLastPaymentInfo(int userId) {
        String sql = "SELECT amount, transaction_date FROM transactions " +
                    "WHERE payer_user_id = ? " +
                    "ORDER BY transaction_date DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                BigDecimal amount = rs.getBigDecimal("amount");
                LocalDate date = rs.getDate("transaction_date").toLocalDate();
                return String.format("%.2f VND (%s)", amount, date);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "No payments yet";
    }

    public String getTodayPaymentTotal(int userId) {
        String sql = "SELECT SUM(amount) as total_amount FROM transactions " +
                    "WHERE payer_user_id = ? AND transaction_date = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(LocalDate.now()));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                BigDecimal totalAmount = rs.getBigDecimal("total_amount");
                if (totalAmount != null) {
                    return String.format("%,.0f VND", totalAmount);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0 VND";
    }
}
