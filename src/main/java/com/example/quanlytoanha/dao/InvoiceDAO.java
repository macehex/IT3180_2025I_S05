// Vị trí: src/main/java/com/example/quanlytoanha/dao/InvoiceDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.InvoiceDetail;
import com.example.quanlytoanha.model.InvoiceDetail; // Cần cho hàm create
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date; // java.util.Date

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
    /**
     * Lấy danh sách hóa đơn CHƯA THANH TOÁN sắp đến hạn, bao gồm owner_id.
     * @param daysBefore Số ngày trước hạn (ví dụ: 3)
     * @return Danh sách hóa đơn thỏa mãn.
     * @throws SQLException
     */
    public List<Invoice> findUpcomingDueInvoices(int daysBefore) throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        // SỬA SQL: Thêm JOIN apartments và lấy a.owner_id
        String sql = """
            SELECT i.invoice_id, i.apartment_id, i.total_amount, i.due_date, a.owner_id
            FROM invoices i
            JOIN apartments a ON i.apartment_id = a.apartment_id
            WHERE i.status = 'UNPAID' 
              AND i.due_date BETWEEN CURRENT_DATE AND CURRENT_DATE + interval '? days'
            ORDER BY i.due_date; 
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, residentId);
            ResultSet rs = pstmt.executeQuery();
             // Chú ý cách xử lý interval an toàn hơn
             PreparedStatement stmt = conn.prepareStatement(sql.replace("?", String.valueOf(daysBefore)))) {

            ResultSet rs = stmt.executeQuery();
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
                invoices.add(mapResultSetToInvoiceBase(rs)); // Dùng hàm map mới
            }
        }
        return invoices;
    }

    /**
     * Lấy danh sách hóa đơn CHƯA THANH TOÁN đã quá hạn, bao gồm owner_id.
     * @return Danh sách hóa đơn thỏa mãn.
     * @throws SQLException
     */
    public List<Invoice> findOverdueInvoices() throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        // SỬA SQL: Thêm JOIN apartments và lấy a.owner_id
        String sql = """
            SELECT i.invoice_id, i.apartment_id, i.total_amount, i.due_date, a.owner_id
            FROM invoices i
            JOIN apartments a ON i.apartment_id = a.apartment_id
            WHERE i.status = 'UNPAID' AND i.due_date < CURRENT_DATE
            ORDER BY i.due_date;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                invoices.add(mapResultSetToInvoiceBase(rs)); // Dùng hàm map mới
            }
        }
        return invoices;
    }

    /**
     * Tạo hóa đơn MỚI và các chi tiết của nó trong cùng 1 GIAO DỊCH (Transaction).
     * (Hàm này giữ nguyên từ trước, đã đúng)
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
            stmtInvoice.setInt(1, invoice.getApartmentId()); // Đã có trong model Invoice mới
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
        }

    }
}