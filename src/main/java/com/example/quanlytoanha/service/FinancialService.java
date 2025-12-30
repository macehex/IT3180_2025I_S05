// Vị trí: src/main/java/com/example/quanlytoanha/service/FinancialService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.FinancialDAO;
import com.example.quanlytoanha.dao.InvoiceDAO;
import com.example.quanlytoanha.model.*;
import com.example.quanlytoanha.session.SessionManager; // Lớp SessionManager của bạn

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class FinancialService {

    private final FinancialDAO financialDAO;
    private final InvoiceDAO invoiceDAO;

    public FinancialService() {
        this.financialDAO = new FinancialDAO();
        this.invoiceDAO = InvoiceDAO.getInstance();
    }

    /**
     * Kiểm tra quyền, chỉ Kế toán (Accountant) hoặc Admin mới được xem.
     */
    private boolean canViewFinancials() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        // Giả sử Role của bạn là Enum hoặc có hằng số
        return currentUser.getRole() == Role.ACCOUNTANT || currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.POLICE;
    }

    /**
     * API để lấy báo cáo thống kê công nợ.
     */
    public DebtReport generateDebtReport() throws SecurityException {
        if (!canViewFinancials()) {
            throw new SecurityException("Không có quyền truy cập chức năng tài chính.");
        }
        DebtReport report = financialDAO.getDebtStatistics();

        // 2. Lấy thống kê ĐÃ THU từ InvoiceDAO (Logic mới bổ sung)
        try {
            Map<String, Object> paidStats = invoiceDAO.getPaymentStatistics();

            // Gán dữ liệu vào report
            if (paidStats != null) {
                int paidCount = (Integer) paidStats.getOrDefault("count", 0);
                BigDecimal collectedAmount = (BigDecimal) paidStats.getOrDefault("amount", BigDecimal.ZERO);

                report.setTotalPaidInvoices(paidCount);
                report.setTotalCollectedAmount(collectedAmount);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê thanh toán: " + e.getMessage());
            e.printStackTrace();
            // Nếu lỗi, set về 0 để không null
            report.setTotalPaidInvoices(0);
            report.setTotalCollectedAmount(BigDecimal.ZERO);
        }

        return report;
    }

    /**
     * API để lấy danh sách chi tiết công nợ.
     */
    public List<ApartmentDebt> getDetailedDebtList() throws SecurityException {
        if (!canViewFinancials()) {
            throw new SecurityException("Không có quyền truy cập chức năng tài chính.");
        }
        return financialDAO.getDebtListByApartment();
    }

    public List<Invoice> getDetailedDebtForApartment(int apartmentId) throws SecurityException {
        if (!canViewFinancials()) {
            throw new SecurityException("Không có quyền truy cập chức năng tài chính.");
        }
        return financialDAO.getUnpaidInvoiceDetails(apartmentId);
    }
}
