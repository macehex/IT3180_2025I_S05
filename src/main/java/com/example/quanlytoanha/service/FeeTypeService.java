package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.FeeTypeDAO;
import com.example.quanlytoanha.model.FeeType;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.model.Role; // Giả sử bạn có Role enum
import com.example.quanlytoanha.session.SessionManager;
import java.util.List;

public class FeeTypeService {

    private final FeeTypeDAO feeTypeDAO;

    public FeeTypeService() {
        this.feeTypeDAO = new FeeTypeDAO();
    }

    /**
     * Copy y hệt từ FinancialService
     */
    private boolean canManageFees() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return currentUser.getRole() == Role.ACCOUNTANT || currentUser.getRole() == Role.ADMIN;
    }

    public List<FeeType> getAllFees() throws SecurityException {
        if (!canManageFees()) {
            throw new SecurityException("Không có quyền quản lý phí.");
        }
        return feeTypeDAO.getAllActiveFeeTypes();
    }

    public boolean addFee(FeeType fee) throws SecurityException {
        if (!canManageFees()) {
            throw new SecurityException("Không có quyền thêm phí.");
        }
        // (Thêm logic validation nếu cần, ví dụ: không được trùng tên)
        return feeTypeDAO.addFee(fee);
    }

    public boolean updateFee(FeeType fee) throws SecurityException {
        if (!canManageFees()) {
            throw new SecurityException("Không có quyền sửa phí.");
        }
        return feeTypeDAO.updateFee(fee);
    }

    public boolean deactivateFee(int feeId) throws SecurityException {
        if (!canManageFees()) {
            throw new SecurityException("Không có quyền xóa phí.");
        }
        return feeTypeDAO.deactivateFee(feeId);
    }
}