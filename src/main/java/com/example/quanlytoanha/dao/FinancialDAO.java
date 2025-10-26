// Vị trí: src/main/java/com/example/quanlytoanha/dao/FinancialDAO.java
package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.DebtReport;
import com.example.quanlytoanha.model.ApartmentDebt;
import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.InvoiceDetail;
import com.example.quanlytoanha.utils.DatabaseConnection; // Lớp kết nối của bạn

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinancialDAO {

    /**
     * Lấy dữ liệu thống kê công nợ tổng quan.
     */
    public DebtReport getDebtStatistics() {
        // Câu SQL này dùng Common Table Expressions (CTE) để tính toán
        String sql = """
            WITH UnpaidInvoices AS (
                SELECT 
                    invoice_id, 
                    total_amount,
                    due_date,
                    CASE WHEN due_date < CURRENT_DATE THEN 1 ELSE 0 END AS is_overdue
                FROM invoices
                WHERE status = 'UNPAID'
            )
            SELECT 
                COUNT(*) AS total_unpaid_invoices,
                SUM(CASE WHEN is_overdue = 1 THEN 1 ELSE 0 END) AS total_overdue_invoices,
                SUM(total_amount) AS total_debt_amount,
                SUM(CASE WHEN is_overdue = 1 THEN total_amount ELSE 0 END) AS total_overdue_amount
            FROM UnpaidInvoices;
        """;

        DebtReport report = new DebtReport(); // Khởi tạo report rỗng

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                // (Bạn cần thêm setter cho DebtReport)
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
     * Lấy danh sách công nợ chi tiết theo từng căn hộ.
     */
    public List<ApartmentDebt> getDebtListByApartment() {
        List<ApartmentDebt> debtList = new ArrayList<>();
        String sql = """
            SELECT 
                a.apartment_id,
                u.full_name AS owner_name,
                u.phone_number,
                COUNT(i.invoice_id) AS unpaid_count,
                SUM(i.total_amount) AS total_due,
                MIN(i.due_date) AS earliest_due_date
            FROM invoices i
            JOIN apartments a ON i.apartment_id = a.apartment_id
            JOIN users u ON a.owner_id = u.user_id
            WHERE i.status = 'UNPAID'
            GROUP BY a.apartment_id, u.full_name, u.phone_number
            ORDER BY total_due DESC;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ApartmentDebt debt = new ApartmentDebt();
                // (Bạn cần thêm setter cho ApartmentDebt)
                debt.setApartmentId(rs.getInt("apartment_id"));
                debt.setOwnerName(rs.getString("owner_name"));
                debt.setPhoneNumber(rs.getString("phone_number"));
                debt.setUnpaidCount(rs.getInt("unpaid_count"));
                debt.setTotalDue(rs.getBigDecimal("total_due"));
                debt.setEarliestDueDate(rs.getDate("earliest_due_date"));
                debtList.add(debt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return debtList;
    }

    public List<Invoice> getUnpaidInvoiceDetails(int apartmentId) {
        // Map để nhóm các chi tiết vào đúng hóa đơn
        Map<Integer, Invoice> invoiceMap = new HashMap<>();

        String sql = """
        SELECT 
            i.invoice_id, i.total_amount, i.due_date,
            d.name, d.amount
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

                // Nếu chưa có hóa đơn này trong Map, tạo mới
                if (!invoiceMap.containsKey(invoiceId)) {
                    Invoice invoice = new Invoice();
                    invoice.setInvoiceId(invoiceId);
                    invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
                    invoice.setDueDate(rs.getDate("due_date").toLocalDate());
                    invoiceMap.put(invoiceId, invoice);
                }

                // Lấy hóa đơn từ Map và thêm chi tiết vào
                Invoice currentInvoice = invoiceMap.get(invoiceId);
                InvoiceDetail detail = new InvoiceDetail(
                        rs.getString("name"),
                        rs.getBigDecimal("amount")
                );
                currentInvoice.addDetail(detail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>(invoiceMap.values());
    }
}