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
import java.sql.Date;

public class FinancialDAO {

    /**
     * BÁO CÁO TỔNG QUAN (US7): Thống kê tổng nợ, tổng quá hạn.
     * CẬP NHẬT: Loại bỏ hoàn toàn hóa đơn VOLUNTARY khỏi thống kê nợ.
     */
    public DebtReport getDebtStatistics() {
        String sql = """
            WITH UnpaidInvoices AS (
                SELECT i.invoice_id, i.total_amount, i.due_date,
                       CASE WHEN i.due_date < CURRENT_DATE THEN 1 ELSE 0 END AS is_overdue
                FROM invoices i
                WHERE i.status = 'UNPAID'
                AND i.total_amount > 0
                -- LỌC BỎ CÁC HÓA ĐƠN TỪ THIỆN/ĐÓNG GÓP
                AND NOT EXISTS (
                    SELECT 1 FROM invoicedetails d 
                    JOIN fee_types f ON d.fee_id = f.fee_id 
                    WHERE d.invoice_id = i.invoice_id 
                    AND f.pricing_model = 'VOLUNTARY'
                )
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
     * DANH SÁCH CÔNG NỢ CHI TIẾT THEO CĂN HỘ (US7_2)
     * CẬP NHẬT: Loại bỏ hoàn toàn hóa đơn VOLUNTARY.
     */
    public List<ApartmentDebt> getDebtListByApartment() {
        List<ApartmentDebt> debtList = new ArrayList<>();
        String sql = """
            SELECT
                a.apartment_id,
                u.full_name AS owner_name,
                u.phone_number,
                u.user_id AS owner_user_id,
                COUNT(i.invoice_id) AS unpaid_count,
                SUM(i.total_amount) AS total_due,
                MIN(i.due_date) AS earliest_due_date
            FROM invoices i
            JOIN apartments a ON i.apartment_id = a.apartment_id
            JOIN users u ON a.owner_id = u.user_id
            WHERE i.status = 'UNPAID'
            AND i.total_amount > 0
            -- LỌC BỎ CÁC HÓA ĐƠN TỪ THIỆN/ĐÓNG GÓP
            AND NOT EXISTS (
                SELECT 1 FROM invoicedetails d 
                JOIN fee_types f ON d.fee_id = f.fee_id 
                WHERE d.invoice_id = i.invoice_id 
                AND f.pricing_model = 'VOLUNTARY'
            )
            GROUP BY a.apartment_id, u.user_id, u.full_name, u.phone_number
            ORDER BY total_due DESC;
        """;

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
                debt.setOwnerUserId(rs.getInt("owner_user_id"));
                debtList.add(debt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return debtList;
    }

    /**
     * CHI TIẾT CÁC HÓA ĐƠN NỢ CỦA 1 CĂN HỘ (Dùng cho Popup xem chi tiết)
     * CẬP NHẬT: Thêm điều kiện total_amount > 0 và lọc VOLUNTARY
     */
    public List<Invoice> getUnpaidInvoiceDetails(int apartmentId) {
        Map<Integer, Invoice> invoiceMap = new HashMap<>();
        String sql = """
            SELECT i.invoice_id, i.total_amount, i.due_date,
                   d.fee_id, d.name, d.amount
            FROM invoices i
            LEFT JOIN invoicedetails d ON i.invoice_id = d.invoice_id
            WHERE i.apartment_id = ? 
            AND i.status = 'UNPAID'
            AND i.total_amount > 0  -- Ẩn hóa đơn 0 đồng
            -- LỌC BỎ HÓA ĐƠN ĐÓNG GÓP
            AND NOT EXISTS (
                SELECT 1 FROM invoicedetails id2 
                JOIN fee_types ft ON id2.fee_id = ft.fee_id 
                WHERE id2.invoice_id = i.invoice_id 
                AND ft.pricing_model = 'VOLUNTARY'
            )
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
                int feeId = rs.getInt("fee_id");
                if (!rs.wasNull()) {
                    InvoiceDetail detail = new InvoiceDetail(
                            feeId,
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

    /**
     * BÁO CÁO THEO KHOẢNG THỜI GIAN
     * CẬP NHẬT: Loại bỏ hóa đơn VOLUNTARY.
     */
    public List<ApartmentDebt> getDebtReportByDateRange(Date startDate, Date endDate) {
        List<ApartmentDebt> debtList = new ArrayList<>();
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
            AND i.total_amount > 0
            -- LỌC BỎ HÓA ĐƠN ĐÓNG GÓP
            AND NOT EXISTS (
                SELECT 1 FROM invoicedetails d 
                JOIN fee_types f ON d.fee_id = f.fee_id 
                WHERE d.invoice_id = i.invoice_id 
                AND f.pricing_model = 'VOLUNTARY'
            )
            ORDER BY a.apartment_id, i.due_date DESC;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ApartmentDebt debt = new ApartmentDebt();
                debt.setApartmentId(rs.getInt("apartment_id"));
                debt.setOwnerName(rs.getString("owner_name"));
                debt.setPhoneNumber(rs.getString("phone_number"));
                debt.setOwnerUserId(rs.getInt("owner_user_id"));
                debt.setTotalDue(rs.getBigDecimal("total_amount"));
                debt.setEarliestDueDate(rs.getDate("due_date"));
                debtList.add(debt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return debtList;
    }
}