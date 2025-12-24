package com.example.quanlytoanha.dao;


import com.example.quanlytoanha.model.DebtReport;
import com.example.quanlytoanha.model.ApartmentDebt;
import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.InvoiceDetail;
import com.example.quanlytoanha.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Date; // <-- BỔ SUNG: Import java.sql.Date cho hàm mới


public class FinancialDAO {

    public DebtReport getDebtStatistics() {
        String sql = """
            WITH UnpaidInvoices AS (
                SELECT invoice_id, total_amount, due_date,
                       CASE WHEN due_date < CURRENT_DATE THEN 1 ELSE 0 END AS is_overdue
                FROM invoices WHERE status = 'UNPAID'
                AND total_amount > 0
            )
            SELECT COUNT(*) AS total_unpaid_invoices,
                   SUM(CASE WHEN is_overdue = 1 THEN 1 ELSE 0 END) AS total_overdue_invoices,
                   SUM(total_amount) AS total_debt_amount,
                   SUM(CASE WHEN is_overdue = 1 THEN total_amount ELSE 0 END) AS total_overdue_amount
            FROM UnpaidInvoices;
        """;
        DebtReport report = new DebtReport();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                report.setTotalUnpaidInvoices(rs.getInt("total_unpaid_invoices"));
                report.setTotalOverdueInvoices(rs.getInt("total_overdue_invoices"));
                report.setTotalDebtAmount(rs.getBigDecimal("total_debt_amount"));
                report.setTotalOverdueAmount(rs.getBigDecimal("total_overdue_amount"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return report;
    }


    /**
     * Lấy danh sách công nợ chi tiết theo từng căn hộ. (ĐÃ SỬA)
     */
    public List<ApartmentDebt> getDebtListByApartment() {
        List<ApartmentDebt> debtList = new ArrayList<>();
        // --- SỬA CÂU SQL: Thêm u.user_id AS owner_user_id ---
        String sql = """
            SELECT
                a.apartment_id,
                u.full_name AS owner_name,
                u.phone_number,
                u.user_id AS owner_user_id, -- Lấy user_id để gửi thông báo
                COUNT(i.invoice_id) AS unpaid_count,
                SUM(i.total_amount) AS total_due,
                MIN(i.due_date) AS earliest_due_date
            FROM invoices i
            JOIN apartments a ON i.apartment_id = a.apartment_id
            JOIN users u ON a.owner_id = u.user_id
            WHERE i.status = 'UNPAID'
            AND i.total_amount > 0
            GROUP BY a.apartment_id, u.user_id, u.full_name, u.phone_number -- Thêm u.user_id vào GROUP BY
            ORDER BY total_due DESC;
        """;
        // ----------------------------------------------------

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ApartmentDebt debt = new ApartmentDebt();
                debt.setApartmentId(rs.getInt("apartment_id"));
                debt.setOwnerName(rs.getString("owner_name"));
                debt.setPhoneNumber(rs.getString("phone_number"));
                debt.setUnpaidCount(rs.getInt("unpaid_count"));
                debt.setTotalDue(rs.getBigDecimal("total_due"));
                debt.setEarliestDueDate(rs.getDate("earliest_due_date"));

                // --- SỬA CODE: Gán giá trị cho trường mới ---
                debt.setOwnerUserId(rs.getInt("owner_user_id"));
                // ------------------------------------------

                debtList.add(debt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return debtList;
    }

    // ... (Hàm getUnpaidInvoiceDetails() giữ nguyên) ...
    public List<Invoice> getUnpaidInvoiceDetails(int apartmentId) {
        Map<Integer, Invoice> invoiceMap = new HashMap<>();
        String sql = """
        SELECT i.invoice_id, i.total_amount, i.due_date,
               d.fee_id, d.name, d.amount
        FROM invoices i
        LEFT JOIN invoicedetails d ON i.invoice_id = d.invoice_id
        WHERE i.apartment_id = ? AND i.status = 'UNPAID'
        ORDER BY i.due_date, i.invoice_id;
    """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, apartmentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int invoiceId = rs.getInt("invoice_id");
                if (!invoiceMap.containsKey(invoiceId)) {
                    Invoice invoice = new Invoice(
                            invoiceId,
                            rs.getBigDecimal("total_amount"),
                            rs.getDate("due_date")
                    );
                    invoiceMap.put(invoiceId, invoice);
                }
                Invoice currentInvoice = invoiceMap.get(invoiceId);
                int feeId = rs.getInt("fee_id"); // Lấy fee_id từ ResultSet
                if (!rs.wasNull()) { // Kiểm tra xem fee_id có null không (tránh lỗi nếu LEFT JOIN không khớp)
                    InvoiceDetail detail = new InvoiceDetail(
                            feeId, // Truyền fee_id vào constructor
                            rs.getString("name"),
                            rs.getBigDecimal("amount")
                    );
                    currentInvoice.addDetail(detail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(invoiceMap.values());
    }


    // --- BỔ SUNG: HÀM MỚI CHO BÁO CÁO CÔNG NỢ (US7_2_1) ---

    /**
     * BÁO CÁO CÔNG NỢ (US7_2_1):
     * Lấy danh sách HÓA ĐƠN CHI TIẾT theo căn hộ DỰA TRÊN KHOẢNG THỜI GIAN
     * (lọc theo ngày đến hạn - due_date).
     * @param startDate Ngày bắt đầu (java.sql.Date)
     * @param endDate Ngày kết thúc (java.sql.Date)
     * @return Danh sách các mục công nợ (ApartmentDebt)
     */
    public List<ApartmentDebt> getDebtReportByDateRange(Date startDate, Date endDate) {
        List<ApartmentDebt> debtList = new ArrayList<>();
        // SQL này lấy CÁC HÓA ĐƠN RIÊNG LẺ (không gộp)
        // và lọc theo 'due_date' (ngày đến hạn)
        String sql = """
            SELECT
                a.apartment_id,
                u.full_name AS owner_name,
                u.phone_number,
                u.user_id AS owner_user_id,
                i.invoice_id,
                i.due_date,
                i.total_amount,
                i.status
            FROM invoices i
            JOIN apartments a ON i.apartment_id = a.apartment_id
            JOIN users u ON a.owner_id = u.user_id
            WHERE i.due_date BETWEEN ? AND ?
            ORDER BY a.apartment_id, i.due_date DESC;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ApartmentDebt debt = new ApartmentDebt();
                // Tái sử dụng model ApartmentDebt
                debt.setApartmentId(rs.getInt("apartment_id"));
                debt.setOwnerName(rs.getString("owner_name"));
                debt.setPhoneNumber(rs.getString("phone_number"));
                debt.setOwnerUserId(rs.getInt("owner_user_id"));

                // Gán TotalDue = total_amount của 1 hóa đơn
                debt.setTotalDue(rs.getBigDecimal("total_amount"));
                // Gán EarliestDueDate = due_date của 1 hóa đơn
                debt.setEarliestDueDate(rs.getDate("due_date"));

                // (Bạn có thể thêm trường status và invoice_id vào model ApartmentDebt
                // để hiển thị trạng thái PAID/UNPAID nếu muốn)

                debtList.add(debt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return debtList;
    }
}