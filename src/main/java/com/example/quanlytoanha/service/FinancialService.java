// Vị trí: src/main/java/com/example/quanlytoanha/service/FinancialService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.FinancialDAO;
import com.example.quanlytoanha.model.ApartmentDebt;
import com.example.quanlytoanha.model.DebtReport;
import com.example.quanlytoanha.model.Role;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager; // Lớp SessionManager của bạn

import java.util.List;

public class FinancialService {

    private final FinancialDAO financialDAO;

    public FinancialService() {
        this.financialDAO = new FinancialDAO();
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
        return currentUser.getRole() == Role.ACCOUNTANT || currentUser.getRole() == Role.ADMIN;
    }

    /**
     * API để lấy báo cáo thống kê công nợ.
     */
    public DebtReport generateDebtReport() throws SecurityException {
        if (!canViewFinancials()) {
            throw new SecurityException("Không có quyền truy cập chức năng tài chính.");
        }
        return financialDAO.getDebtStatistics();
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
}
