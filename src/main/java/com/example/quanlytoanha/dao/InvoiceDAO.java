// Vị trí: src/main/java/com/example/quanlytoanha/dao/InvoiceDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.InvoiceDetail;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LƯU Ý QUAN TRỌNG:
 * File này giả định rằng bạn đã sửa file model 'Invoice.java'
 * để bao gồm các trường/hàm sau:
 * - public Invoice() { ... } (Constructor rỗng)
 * - private int apartmentId; (và getter/setter)
 * - private int ownerId; (và getter/setter)
 * - private String status; (và getter/setter)
 */
public class InvoiceDAO {
    private static InvoiceDAO instance;

    public InvoiceDAO() {}

    public static InvoiceDAO getInstance() {
        if (instance == null) {
            instance = new InvoiceDAO();
        }
        return instance;
    }

    // -----------------------------------------------------------------
    // --- CÁC HÀM MỚI CHO NGHIỆP VỤ "TẠO HÓA ĐƠN HÀNG LOẠT" ---
    // (Dùng cho InvoiceGenerationService)
    // -----------------------------------------------------------------

    /**
     * KIỂM TRA: Hóa đơn cho căn hộ/kỳ thanh toán này đã tồn tại chưa?
     * (Kiểm tra dựa trên due_date)
     */
    public boolean checkIfInvoiceExists(int apartmentId, LocalDate billingMonth) {
        // Tính toán tháng/năm của ngày hết hạn dự kiến
        LocalDate expectedDueDate = billingMonth.plusMonths(1).withDayOfMonth(15);
        int dueMonth = expectedDueDate.getMonthValue();
        int dueYear = expectedDueDate.getYear();

        // SQL kiểm tra tháng/năm của due_date
        String sql = """
            SELECT 1 FROM invoices
            WHERE apartment_id = ?
              AND EXTRACT(MONTH FROM due_date) = ? 
              AND EXTRACT(YEAR FROM due_date) = ? 
            LIMIT 1
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, apartmentId);
            stmt.setInt(2, dueMonth);
            stmt.setInt(3, dueYear);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Trả về true nếu tìm thấy (đã tồn tại)
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true; // An toàn là trên hết
        }
    }
    /**
     * TẠO HÓA ĐƠN CHA: Tạo 1 hóa đơn mới với tổng tiền = 0
     * (ĐÃ SỬA: Bỏ cột issued_date)
     * @return Đối tượng Invoice chứa ID mới
     */
    public Invoice createInvoiceHeader(int apartmentId, LocalDate billingMonth) {
        // (Giả sử Hạn thanh toán là ngày 15 của tháng SAU tháng tạo hóa đơn)
        // Ví dụ: Tạo hóa đơn tháng 10 (billingMonth=10/2025) -> Hạn là 15/11/2025
        LocalDate dueDate = billingMonth.plusMonths(1).withDayOfMonth(15);

        // Sửa SQL: Bỏ issued_date
        String sql = "INSERT INTO invoices (apartment_id, due_date, status, total_amount) VALUES (?, ?, 'UNPAID', 0)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, apartmentId);
            stmt.setDate(2, java.sql.Date.valueOf(dueDate)); // Chỉ set due_date

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) return null;

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newInvoiceId = generatedKeys.getInt(1);
                    // Trả về Invoice với các thông tin đã có
                    return new Invoice(newInvoiceId, BigDecimal.ZERO, java.sql.Date.valueOf(dueDate));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu thất bại
    }

    /**
     * THÊM CHI TIẾT: Thêm 1 dòng chi tiết phí vào hóa đơn cha
     */
    public void addInvoiceDetail(int invoiceId, int feeId, String feeName, BigDecimal amount) {
        String sql = "INSERT INTO invoicedetails (invoice_id, fee_id, name, amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, invoiceId);
            stmt.setInt(2, feeId);
            stmt.setString(3, feeName);
            stmt.setBigDecimal(4, amount);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * CẬP NHẬT TỔNG TIỀN: Cập nhật tổng tiền cho Hóa đơn cha sau khi đã thêm hết chi tiết
     */
    public void updateInvoiceTotal(int invoiceId, BigDecimal totalAmount) {
        String sql = "UPDATE invoices SET total_amount = ? WHERE invoice_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, totalAmount);
            stmt.setInt(2, invoiceId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // -----------------------------------------------------------------
    // --- CÁC HÀM CŨ CỦA BẠN (CHO NGHIỆP VỤ CƯ DÂN/NHẮC NỢ) ---
    // (Đã sửa lỗi)
    // -----------------------------------------------------------------

    /**
     * Lấy các hóa đơn chưa thanh toán cho 1 Cư dân (ĐÃ SỬA LỖI)
     */
    public List<Invoice> getUnpaidInvoices(int residentId) {
        Map<Integer, Invoice> invoices = new HashMap<>();

        // FIXED: Join directly through apartments.owner_id instead of residents table
        String sql = "SELECT i.*, id.fee_id, id.name, id.amount as detail_amount " +
                "FROM invoices i " +
                "LEFT JOIN invoicedetails id ON i.invoice_id = id.invoice_id " +
                "JOIN apartments a ON i.apartment_id = a.apartment_id " +
                "WHERE a.owner_id = ? AND i.status = 'UNPAID' " +
                "ORDER BY i.due_date";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, residentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int invoiceId = rs.getInt("invoice_id");
                final ResultSet finalRs = rs;

                // Giả định Model Invoice.java đã được sửa
                Invoice invoice = invoices.computeIfAbsent(invoiceId, k -> {
                    Invoice newInvoice = new Invoice(); // Giả định constructor rỗng
                    try {
                        newInvoice.setInvoiceId(invoiceId);
                        newInvoice.setApartmentId(finalRs.getInt("apartment_id"));
                        newInvoice.setTotalAmount(finalRs.getBigDecimal("total_amount")); // Sửa: getBigDecimal
                        newInvoice.setDueDate(finalRs.getDate("due_date"));
                        newInvoice.setStatus(finalRs.getString("status"));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return newInvoice;
                });

                // Add invoice detail if it exists
                int feeId = rs.getInt("fee_id");
                if (!rs.wasNull()) {
                    // SỬA LỖI 2: Gọi đúng constructor 3 tham số của InvoiceDetail
                    InvoiceDetail detail = new InvoiceDetail(
                            feeId,
                            rs.getString("name"),
                            rs.getBigDecimal("detail_amount") // Sửa: getBigDecimal
                    );
                    invoice.addDetail(detail); // (Giả định hàm addDetail tồn tại)
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(invoices.values());
    }

    /**
     * Lấy danh sách hóa đơn CHƯA THANH TOÁN sắp đến hạn (ĐÃ SỬA LỖI SQL)
     */
    public List<Invoice> findUpcomingDueInvoices(int daysBefore) throws SQLException {
        List<Invoice> invoices = new ArrayList<>();

        // SỬA LỖI SQL: Dùng 'make_interval' cho PostgreSQL
        String sql = "SELECT i.invoice_id, i.apartment_id, i.total_amount, i.due_date, a.owner_id " +
                "FROM invoices i " +
                "JOIN apartments a ON i.apartment_id = a.apartment_id " +
                "WHERE i.status = 'UNPAID' " +
                "AND i.due_date BETWEEN CURRENT_DATE AND (CURRENT_DATE + make_interval(days => ?)) " +
                "ORDER BY i.due_date";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, daysBefore);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                invoices.add(mapResultSetToInvoiceBase(rs));
            }
        }
        return invoices;
    }

    /**
     * Lấy danh sách hóa đơn CHƯA THANH TOÁN đã quá hạn (Không lỗi)
     */
    public List<Invoice> findOverdueInvoices() throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
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
                invoices.add(mapResultSetToInvoiceBase(rs));
            }
        }
        return invoices;
    }

    /**
     * Tạo hóa đơn MỚI và các chi tiết của nó (ĐÃ SỬA LỖI SQL)
     */
    public boolean createInvoiceWithDetails(Invoice invoice, List<InvoiceDetail> details) throws SQLException {
        Connection conn = null;
        PreparedStatement stmtInvoice = null;
        PreparedStatement stmtDetail = null;

        String sqlInvoice = "INSERT INTO invoices (apartment_id, total_amount, due_date, status) VALUES (?, ?, ?, 'UNPAID') RETURNING invoice_id";

        // SỬA LỖI SQL: Thêm cột 'fee_id'
        String sqlDetail = "INSERT INTO invoicedetails (invoice_id, fee_id, name, amount) VALUES (?, ?, ?, ?)";

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Tạo Hóa đơn (Invoice)
            stmtInvoice = conn.prepareStatement(sqlInvoice);
            stmtInvoice.setInt(1, invoice.getApartmentId()); // Giả định Invoice.java có hàm này
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
                stmtDetail.setInt(2, detail.getFeeId()); // SỬA LỖI: Thêm fee_id
                stmtDetail.setString(3, detail.getName());
                stmtDetail.setBigDecimal(4, detail.getAmount());
                stmtDetail.addBatch(); // Thêm vào lô
            }

            stmtDetail.executeBatch(); // Thực thi lô

            conn.commit(); // Hoàn tất Transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            try {
                if (stmtDetail != null) stmtDetail.close();
                if (stmtInvoice != null) stmtInvoice.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cập nhật trạng thái hóa đơn (Không lỗi)
     */
    public boolean updateInvoiceStatus(int invoiceId, String status) {
        String sql = "UPDATE invoices SET status = ? WHERE invoice_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, invoiceId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hàm tiện ích: Ánh xạ ResultSet thành Invoice
     * (Giả định Model Invoice.java đã được sửa)
     */
    private Invoice mapResultSetToInvoiceBase(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice(); // Giả định constructor rỗng
        invoice.setInvoiceId(rs.getInt("invoice_id"));
        invoice.setApartmentId(rs.getInt("apartment_id"));
        invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
        invoice.setDueDate(rs.getDate("due_date"));
        invoice.setOwnerId(rs.getInt("owner_id"));
        return invoice;
    }

    /**
     * Lấy chi tiết của một hóa đơn (ĐÃ SỬA LỖI)
     */
    public List<InvoiceDetail> getInvoiceDetails(int invoiceId) {
        List<InvoiceDetail> details = new ArrayList<>();
        // SỬA LỖI SQL: Lấy các cột cần thiết
        String sql = "SELECT fee_id, name, amount FROM invoicedetails WHERE invoice_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // SỬA LỖI: Gọi đúng constructor (3 tham số)
                InvoiceDetail detail = new InvoiceDetail(
                        rs.getInt("fee_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("amount") // Sửa: getBigDecimal
                );
                details.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details;
    }

    /**
     * TÌM ID HÓA ĐƠN: Tìm ID của hóa đơn dựa trên căn hộ và tháng (due_date)
     * @return ID hóa đơn hoặc null nếu không tìm thấy
     */
    public Integer findInvoiceIdByApartmentAndMonth(int apartmentId, LocalDate billingMonth) {
        LocalDate expectedDueDate = billingMonth.plusMonths(1).withDayOfMonth(15);
        int dueMonth = expectedDueDate.getMonthValue();
        int dueYear = expectedDueDate.getYear();

        String sql = """
            SELECT invoice_id FROM invoices
            WHERE apartment_id = ?
              AND EXTRACT(MONTH FROM due_date) = ?
              AND EXTRACT(YEAR FROM due_date) = ?
            LIMIT 1
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, apartmentId);
            stmt.setInt(2, dueMonth);
            stmt.setInt(3, dueYear);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("invoice_id"); // Trả về ID nếu tìm thấy
                } else {
                    return null; // Trả về null nếu không có hóa đơn cho tháng đó
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Trả về null nếu có lỗi
        }
    }

    /**
     * XÓA CHI TIẾT: Xóa tất cả các dòng chi tiết của một hóa đơn
     * @return true nếu xóa thành công (hoặc không có gì để xóa), false nếu lỗi
     */
    public boolean deleteInvoiceDetails(int invoiceId) {
        String sql = "DELETE FROM invoicedetails WHERE invoice_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, invoiceId);
            stmt.executeUpdate(); // Chạy lệnh DELETE
            return true; // Giả sử thành công nếu không có exception
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}