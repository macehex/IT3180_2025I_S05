package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.ContributionHistoryDTO;
import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.InvoiceDetail;
import com.example.quanlytoanha.utils.DatabaseConnection;
import com.example.quanlytoanha.model.FeeType;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceDAO {
    private static InvoiceDAO instance;

    public InvoiceDAO() {}

    public static InvoiceDAO getInstance() {
        if (instance == null) {
            instance = new InvoiceDAO();
        }
        return instance;
    }

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
    public Invoice createInvoiceHeader(int apartmentId, LocalDate billingMonth, LocalDate dueDate) {

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
                    InvoiceDetail detail = new InvoiceDetail(
                            feeId,
                            rs.getString("name"),
                            rs.getBigDecimal("detail_amount")
                    );
                    invoice.addDetail(detail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(invoices.values());
    }

    /**
     * Lấy danh sách hóa đơn CHƯA THANH TOÁN sắp đến hạn
     */
    public List<Invoice> findUpcomingDueInvoices(int daysBefore) throws SQLException {
        List<Invoice> invoices = new ArrayList<>();

        String sql = "SELECT i.invoice_id, i.apartment_id, i.total_amount, i.due_date, a.owner_id " +
                "FROM invoices i " +
                "JOIN apartments a ON i.apartment_id = a.apartment_id " +
                "WHERE i.status = 'UNPAID' " + "AND i.total_amount > 0 " +
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
     * Lấy danh sách hóa đơn CHƯA THANH TOÁN đã quá hạn
     */
    public List<Invoice> findOverdueInvoices() throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        String sql = """
            SELECT i.invoice_id, i.apartment_id, i.total_amount, i.due_date, a.owner_id
            FROM invoices i
            JOIN apartments a ON i.apartment_id = a.apartment_id
            WHERE i.status = 'UNPAID' AND i.due_date < CURRENT_DATE
            AND i.total_amount > 0
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
     * Tạo hóa đơn MỚI và các chi tiết của nó
     */
    public boolean createInvoiceWithDetails(Invoice invoice, List<InvoiceDetail> details) throws SQLException {
        Connection conn = null;
        PreparedStatement stmtInvoice = null;
        PreparedStatement stmtDetail = null;

        String sqlInvoice = "INSERT INTO invoices (apartment_id, total_amount, due_date, status) VALUES (?, ?, ?, 'UNPAID') RETURNING invoice_id";
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
     * Cập nhật trạng thái hóa đơn
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
     * Lấy chi tiết của một hóa đơn
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

    /**
     * Lấy tổng số tiền nợ (hóa đơn chưa thanh toán) của một căn hộ
     * @param apartmentId ID của căn hộ
     * @return Tổng số tiền nợ
     */
    public BigDecimal getTotalDebtForApartment(int apartmentId) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total_debt FROM invoices WHERE apartment_id = ? AND status = 'UNPAID' AND total_amount > 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, apartmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("total_debt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    /**
     * --- MỚI ---
     * Tìm ID của loại phí VOLUNTARY trong hóa đơn (nếu có)
     */
    public Integer getVoluntaryFeeIdInInvoice(int invoiceId) {
        String sql = """
            SELECT id.fee_id 
            FROM invoicedetails id
            JOIN fee_types ft ON id.fee_id = ft.fee_id
            WHERE id.invoice_id = ? 
              AND ft.pricing_model = 'VOLUNTARY'
              AND ft.is_active = TRUE  -- <--- DÒNG NÀY LÀ CHỐT CHẶN QUAN TRỌNG
            LIMIT 1
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, invoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("fee_id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Trả về NULL nếu phí không tồn tại HOẶC ĐÃ BỊ HỦY -> Không tái tạo nữa.
    }

    /**
     * REPORT 1: Lấy lịch sử tất cả hóa đơn ĐÃ THANH TOÁN
     */
    public List<Invoice> getPaidInvoicesHistory() {
        List<Invoice> list = new ArrayList<>();

        // Câu lệnh SQL giữ nguyên: Tính "original_amount" bằng cách lấy Tổng - Phí Tự Nguyện
        String sql = """
        SELECT 
            i.invoice_id, 
            a.apartment_id,
            t.transaction_date,
            (i.total_amount - COALESCE(
                (SELECT SUM(id.amount) 
                 FROM invoicedetails id 
                 JOIN fee_types ft ON id.fee_id = ft.fee_id 
                 WHERE id.invoice_id = i.invoice_id AND ft.pricing_model = 'VOLUNTARY')
            , 0)) AS original_amount
        FROM invoices i
        JOIN transactions t ON i.invoice_id = t.invoice_id
        JOIN apartments a ON i.apartment_id = a.apartment_id
        WHERE i.status = 'PAID'
        ORDER BY t.transaction_date DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // 1. Lấy số tiền "gốc" (sau khi đã trừ đi phần đóng góp)
                BigDecimal originalAmount = rs.getBigDecimal("original_amount");

                // Nếu kết quả <= 0, nghĩa là hóa đơn này hoàn toàn là đóng góp tự nguyện
                // -> BỎ QUA (continue), không thêm vào danh sách hiển thị
                if (originalAmount == null || originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                // ----------------------------------------

                Invoice inv = new Invoice();
                inv.setInvoiceId(rs.getInt("invoice_id"));
                inv.setApartmentId(rs.getInt("apartment_id"));
                inv.setDueDate(rs.getDate("transaction_date")); // Lấy ngày giao dịch làm ngày hiển thị
                inv.setTotalAmount(originalAmount); // Chỉ hiển thị số tiền điện/nước thực thu
                list.add(inv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy danh sách các loại phí BẮT BUỘC (Điện, Nước, Dịch vụ...)
     * Pricing Model là: FIXED hoặc PER_SQM
     */
    public List<FeeType> getUtilityFeeTypes() {
        List<FeeType> list = new ArrayList<>();
        // Chỉ lấy FIXED và PER_SQM
        String sql = "SELECT * FROM fee_types WHERE pricing_model IN ('FIXED', 'PER_SQM')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToFeeType(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy danh sách các loại phí ĐÓNG GÓP (Tự nguyện)
     * Pricing Model là: VOLUNTARY
     */
    public List<FeeType> getVoluntaryFeeTypes() {
        List<FeeType> list = new ArrayList<>();
        String sql = "SELECT * FROM fee_types WHERE pricing_model = 'VOLUNTARY'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToFeeType(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Hàm phụ trợ để map dữ liệu từ SQL sang Object FeeType
    private FeeType mapResultSetToFeeType(ResultSet rs) throws SQLException {
        FeeType fee = new FeeType();
        fee.setFeeId(rs.getInt("fee_id"));
        fee.setFeeName(rs.getString("fee_name"));
        fee.setUnitPrice(rs.getBigDecimal("unit_price"));
        fee.setUnit(rs.getString("unit"));
        fee.setDescription(rs.getString("description"));
        fee.setDefault(rs.getBoolean("is_default"));
        fee.setPricingModel(rs.getString("pricing_model"));
        return fee;
    }

    // Trong file InvoiceDAO.java

    /**
     * HÀM MỚI: Lấy hạn thanh toán gần nhất của một loại phí (để hiển thị lên form sửa)
     */
    public LocalDate getLatestDueDateForFee(int feeId) {
        String sql = """
            SELECT i.due_date 
            FROM invoices i
            JOIN invoicedetails id ON i.invoice_id = id.invoice_id
            WHERE id.fee_id = ? AND i.status = 'UNPAID'
            ORDER BY i.due_date DESC 
            LIMIT 1
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, feeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    java.sql.Date date = rs.getDate("due_date");
                    return (date != null) ? date.toLocalDate() : null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * HÀM MỚI: Cập nhật hạn thanh toán cho chiến dịch (Update các hóa đơn chưa trả)
     */
    public boolean updateCampaignDueDate(int feeId, LocalDate newDate) {
        String sql = """
            UPDATE invoices i
            SET due_date = ?
            FROM invoicedetails id
            WHERE i.invoice_id = id.invoice_id 
              AND id.fee_id = ? 
              AND i.status = 'UNPAID'
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(newDate));
            stmt.setInt(2, feeId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HÀM MỚI: Kiểm tra xem đã có hóa đơn ĐIỆN/NƯỚC cho tháng này chưa?
     * Logic: Tìm xem có hóa đơn nào trong tháng đáo hạn có chứa phí FIXED hoặc PER_SQM không.
     * Nếu chỉ có hóa đơn VOLUNTARY (đóng góp) thì coi như chưa có hóa đơn điện nước -> Cho phép tạo mới.
     */
    public boolean isUtilityInvoiceCreated(int apartmentId, LocalDate billingMonth) {
        // Hóa đơn tháng 3 thì hạn là tháng 4
        LocalDate expectedDueDate = billingMonth.plusMonths(1).withDayOfMonth(15);
        int dueMonth = expectedDueDate.getMonthValue();
        int dueYear = expectedDueDate.getYear();

        String sql = """
            SELECT 1 
            FROM invoices i
            JOIN invoicedetails id ON i.invoice_id = id.invoice_id
            JOIN fee_types ft ON id.fee_id = ft.fee_id
            WHERE i.apartment_id = ?
              AND EXTRACT(MONTH FROM i.due_date) = ?
              AND EXTRACT(YEAR FROM i.due_date) = ?
              AND ft.pricing_model IN ('FIXED', 'PER_SQM') -- Chỉ tìm phí bắt buộc
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, apartmentId);
            stmt.setInt(2, dueMonth);
            stmt.setInt(3, dueYear);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Trả về true nếu đã có hóa đơn điện nước
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Nếu lỗi thì chặn cho an toàn
        }
    }

    /**
     * HÀM MỚI: Xóa các khoản phí chưa thanh toán của một loại phí cụ thể.
     * Dùng khi Kế toán hủy một loại phí (ví dụ: hủy đợt quyên góp).
     */
    public void cleanupUnpaidFeeDetails(int feeId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Dùng Transaction để an toàn

            // 1. Xóa chi tiết phí (InvoiceDetail) trong các hóa đơn chưa trả
            String sqlDeleteDetails = """
                DELETE FROM invoicedetails 
                WHERE fee_id = ? 
                AND invoice_id IN (SELECT invoice_id FROM invoices WHERE status = 'UNPAID')
            """;
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteDetails)) {
                stmt.setInt(1, feeId);
                int rows = stmt.executeUpdate();
                System.out.println("Đã xóa " + rows + " dòng chi tiết của phí ID " + feeId);
            }

            // 2. Xóa các hóa đơn rỗng (Header) nếu không còn chi tiết nào
            // (Ví dụ: Hóa đơn quyên góp chỉ có 1 dòng, xóa dòng đó đi thì hóa đơn vô nghĩa -> Xóa luôn)
            String sqlDeleteEmptyInvoices = """
                DELETE FROM invoices 
                WHERE status = 'UNPAID' 
                AND invoice_id NOT IN (SELECT DISTINCT invoice_id FROM invoicedetails)
            """;
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteEmptyInvoices)) {
                int rows = stmt.executeUpdate();
                System.out.println("Đã dọn dẹp " + rows + " hóa đơn rỗng.");
            }

            // 3. Cập nhật lại tổng tiền cho các hóa đơn còn lại (nếu hóa đơn đó có nhiều phí khác nhau)
            String sqlUpdateTotal = """
                UPDATE invoices i
                SET total_amount = (
                    SELECT COALESCE(SUM(amount), 0)
                    FROM invoicedetails id
                    WHERE id.invoice_id = i.invoice_id
                )
                WHERE status = 'UNPAID'
            """;
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateTotal)) {
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Lấy thông tin cơ bản của 1 hóa đơn (Dùng để clone hóa đơn đóng góp)
     */
    public Invoice getInvoiceById(int invoiceId) {
        String sql = "SELECT * FROM invoices WHERE invoice_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Invoice inv = new Invoice();
                    inv.setInvoiceId(rs.getInt("invoice_id"));
                    inv.setApartmentId(rs.getInt("apartment_id"));
                    inv.setDueDate(rs.getDate("due_date"));
                    inv.setTotalAmount(rs.getBigDecimal("total_amount"));
                    return inv;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * REPORT 2: Lấy lịch sử đóng góp tự nguyện (Chi tiết từng lần đóng)
     * Hàm này phục vụ cho tab "Quỹ Đóng góp Tự nguyện" trên Dashboard Kế toán.
     */
    public List<ContributionHistoryDTO> getContributionHistory() {
        List<ContributionHistoryDTO> list = new ArrayList<>();
        String sql = """
            SELECT 
                t.transaction_date,
                a.apartment_id,
                u.full_name,
                id.name AS fee_name,
                id.amount
            FROM transactions t
            JOIN invoices i ON t.invoice_id = i.invoice_id
            JOIN invoicedetails id ON i.invoice_id = id.invoice_id
            JOIN fee_types ft ON id.fee_id = ft.fee_id
            JOIN apartments a ON i.apartment_id = a.apartment_id
            LEFT JOIN users u ON a.owner_id = u.user_id
            WHERE ft.pricing_model = 'VOLUNTARY'
            AND id.amount > 0
            ORDER BY t.transaction_date DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Lấy dữ liệu từ DB
                java.sql.Timestamp ts = rs.getTimestamp("transaction_date");
                java.time.LocalDateTime date = (ts != null) ? ts.toLocalDateTime() : null;

                String room = String.valueOf(rs.getInt("apartment_id"));
                String name = rs.getString("full_name");
                if (name == null) name = "Chưa cập nhật"; // Fallback nếu chưa có tên chủ hộ

                String fee = rs.getString("fee_name");
                BigDecimal amount = rs.getBigDecimal("amount");

                // Tạo DTO và thêm vào list
                // (Đảm bảo class ContributionHistoryDTO của bạn có constructor tương ứng này)
                list.add(new ContributionHistoryDTO(date, room, name, fee, amount));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}

