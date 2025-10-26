// Vị trí: src/main/java/com/example/quanlytoanha/service/FeeTypeService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.FeeTypeDAO;
import com.example.quanlytoanha.service.ValidationException;
import com.example.quanlytoanha.model.FeeType;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class FeeTypeService {

    private final FeeTypeDAO feeTypeDAO = new FeeTypeDAO();

    public List<FeeType> getAllActiveFees() throws SQLException {
        return feeTypeDAO.getAllActiveFees();
    }

    public boolean addFee(FeeType fee) throws ValidationException, SQLException {
        validateFee(fee);
        return feeTypeDAO.addFee(fee);
    }

    public boolean updateFee(FeeType fee) throws ValidationException, SQLException {
        validateFee(fee);
        return feeTypeDAO.updateFee(fee);
    }

    public boolean deactivateFee(int feeId) throws SQLException {
        return feeTypeDAO.deactivateFee(feeId);
    }

    private void validateFee(FeeType fee) throws ValidationException {
        if (fee.getFeeName() == null || fee.getFeeName().trim().isEmpty()) {
            throw new ValidationException("Tên phí không được để trống.");
        }
        if (fee.getUnitPrice() == null || fee.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Đơn giá phải là số không âm.");
        }
        if (fee.getUnit() == null || fee.getUnit().trim().isEmpty()) {
            throw new ValidationException("Đơn vị (m2, xe,...) không được để trống.");
        }
    }
}