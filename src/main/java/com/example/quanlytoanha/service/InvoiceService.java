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

    public Transaction processPayment(int residentId, Invoice invoice, BigDecimal paymentAmount) {
        Connection conn = null;
        Transaction transaction = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction (Quan trọng)

            // --- 1. XỬ LÝ PHÍ ĐÓNG GÓP ---
            BigDecimal originalTotal = invoice.getTotalAmount();

            // Nếu khách trả nhiều hơn tổng hóa đơn gốc
            if (paymentAmount.compareTo(originalTotal) > 0) {
                // Tính phần dư ra (Tiền đóng góp)
                BigDecimal excessAmount = paymentAmount.subtract(originalTotal);

                // Tìm xem trong hóa đơn này ĐÃ CÓ dòng phí tự nguyện chưa
                Integer voluntaryFeeId = invoiceDAO.getVoluntaryFeeIdInInvoice(invoice.getInvoiceId());

                if (voluntaryFeeId != null) {
                    // TRƯỜNG HỢP A: Đã có dòng phí -> Cập nhật cộng dồn (Logic cũ)
                    String updateDetailSql = "UPDATE invoicedetails SET amount = amount + ? WHERE invoice_id = ? AND fee_id = ?";
                    try (PreparedStatement pstmtDetail = conn.prepareStatement(updateDetailSql)) {
                        pstmtDetail.setBigDecimal(1, excessAmount);
                        pstmtDetail.setInt(2, invoice.getInvoiceId());
                        pstmtDetail.setInt(3, voluntaryFeeId);
                        pstmtDetail.executeUpdate();
                    }
                } else {
                    // TRƯỜNG HỢP B: Chưa có dòng phí -> TẠO MỚI
                    int activeFeeId = -1;

                    // SỬA: Bỏ điều kiện "is_active = TRUE" để đảm bảo luôn tìm thấy loại phí VOLUNTARY
                    // Dù phí đó có bị ẩn đi chăng nữa, tiền đã thu thì phải ghi sổ!
                    String findFeeSql = "SELECT fee_id FROM fee_types WHERE pricing_model = 'VOLUNTARY' LIMIT 1";

                    try (PreparedStatement pstmtFind = conn.prepareStatement(findFeeSql);
                         ResultSet rsFind = pstmtFind.executeQuery()) {
                        if (rsFind.next()) {
                            activeFeeId = rsFind.getInt("fee_id");
                        }
                    }

                    if (activeFeeId != -1) {
                        String insertDetailSql = "INSERT INTO invoicedetails (invoice_id, fee_id, name, amount) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement pstmtInsert = conn.prepareStatement(insertDetailSql)) {
                            pstmtInsert.setInt(1, invoice.getInvoiceId());
                            pstmtInsert.setInt(2, activeFeeId);
                            pstmtInsert.setString(3, "Phí đóng góp (Tự nguyện)");
                            pstmtInsert.setBigDecimal(4, excessAmount);
                            pstmtInsert.executeUpdate();

                            // DEBUG: In ra để biết đã chèn thành công
                            System.out.println("Đã chèn phí đóng góp " + excessAmount + " vào HĐ " + invoice.getInvoiceId());
                        }
                    } else {
                        // Nếu database chưa có loại phí VOLUNTARY nào -> Bắt buộc phải báo lỗi để Admin biết đường tạo phí
                        throw new SQLException("LỖI CẤU HÌNH: Không tìm thấy loại phí có pricing_model='VOLUNTARY' trong bảng fee_types.");
                    }
                }

                // Cập nhật tổng tiền mới cho hóa đơn (trong bảng invoices)
                // (Thực hiện cho cả 2 trường hợp A và B)
                String updateTotalSql = "UPDATE invoices SET total_amount = ? WHERE invoice_id = ?";
                try (PreparedStatement pstmtTotal = conn.prepareStatement(updateTotalSql)) {
                    pstmtTotal.setBigDecimal(1, paymentAmount); // Set thành số tiền thực trả
                    pstmtTotal.setInt(2, invoice.getInvoiceId());
                    pstmtTotal.executeUpdate();
                }

                // Cập nhật lại object invoice trong bộ nhớ để Notification hiển thị đúng
                invoice.setTotalAmount(paymentAmount);
            }
            // ---------------------------------------------

            // --- 2. TẠO GIAO DỊCH (GIỮ NGUYÊN LOGIC CŨ) ---
            String insertSql = "INSERT INTO transactions (invoice_id, payer_user_id, amount, transaction_date, description, status, type) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING transaction_id";

            LocalDate transactionDate = LocalDate.now();
            int transactionId;

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, invoice.getInvoiceId());
                pstmt.setInt(2, residentId);
                pstmt.setBigDecimal(3, paymentAmount); // Dùng số tiền thực trả
                pstmt.setDate(4, Date.valueOf(transactionDate));
                pstmt.setString(5, "Thanh toán hóa đơn #" + invoice.getInvoiceId());
                pstmt.setString(6, "COMPLETED");
                pstmt.setString(7, "PAYMENT");

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    transactionId = rs.getInt("transaction_id");
                    transaction = new Transaction(
                            transactionId,
                            paymentAmount,
                            "Thanh toán hóa đơn #" + invoice.getInvoiceId(),
                            transactionDate.atStartOfDay(),
                            "COMPLETED",
                            "PAYMENT",
                            invoice.getInvoiceId()
                    );
                } else {
                    throw new SQLException("Failed to create transaction");
                }
            }

            // --- 3. CẬP NHẬT TRẠNG THÁI HÓA ĐƠN ---
            String updateSql = "UPDATE invoices SET status = ? WHERE invoice_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, "PAID");
                pstmt.setInt(2, invoice.getInvoiceId());
                pstmt.executeUpdate();
            }

            // Mọi thứ thành công -> Commit
            conn.commit();

            // Gửi thông báo sau khi commit thành công
            notificationService.sendPaymentSuccessNotification(residentId, invoice, paymentAmount);

            return transaction;

        } catch (SQLException e) {
            // Nếu có lỗi -> Rollback (Hoàn tác tất cả, kể cả việc cập nhật phí đóng góp)
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