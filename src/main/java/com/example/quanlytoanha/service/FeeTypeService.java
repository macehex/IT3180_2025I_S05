package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.FeeTypeDAO;
import com.example.quanlytoanha.dao.InvoiceDAO;
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

    // Import InvoiceDAO ở đầu file nếu chưa có
    // import com.example.quanlytoanha.dao.InvoiceDAO;

    public boolean deactivateFee(int feeId) throws SecurityException {
        if (!canManageFees()) {
            throw new SecurityException("Không có quyền xóa phí.");
        }

        // 1. Ẩn phí khỏi danh sách (Logic cũ)
        boolean deactivated = feeTypeDAO.deactivateFee(feeId);

        // 2. NẾU ẨN THÀNH CÔNG -> XÓA LUÔN CÁC HÓA ĐƠN UNPAID CỦA NÓ (Logic mới)
        if (deactivated) {
            InvoiceDAO.getInstance().cleanupUnpaidFeeDetails(feeId);
        }

        return deactivated;
    }

    /**
     * HÀM MỚI: Gọi DAO lấy phí mới nhất
     */
    public FeeType getLatestFee() {
        return feeTypeDAO.getLatestFee();
    }

    /**
     * HÀM MỚI: Gọi DAO để lấy phí theo tên
     */
    public FeeType getFeeByName(String feeName) {
        // Có thể tái sử dụng quyền canManageFees() hoặc cho phép rộng hơn tùy bạn
        if (!canManageFees()) {
            // throw new SecurityException("Không có quyền xem thông tin phí.");
            // Tạm thời return null hoặc cho phép nếu cần thiết cho luồng tạo campaign
        }
        return feeTypeDAO.getFeeByName(feeName);
    }
}