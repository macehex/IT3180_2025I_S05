// Vị trí: src/main/java/com/example/quanlytoanha/service/ApartmentService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.ApartmentDAO;
import com.example.quanlytoanha.model.Apartment;

import java.math.BigDecimal;
import java.sql.SQLException;

public class ApartmentService {

    private final ApartmentDAO apartmentDAO = new ApartmentDAO();

    /**
     * Exception cho validation
     */
    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    /**
     * Thêm căn hộ mới với validation
     * @param apartment Đối tượng Apartment chứa thông tin căn hộ
     * @return true nếu thêm thành công
     * @throws ValidationException nếu dữ liệu không hợp lệ
     * @throws SQLException nếu có lỗi database
     */
    public boolean addApartment(Apartment apartment) throws ValidationException, SQLException {
        // Validation: Diện tích phải lớn hơn 0
        if (apartment.getArea() == null || apartment.getArea().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Diện tích căn hộ phải lớn hơn 0.");
        }

        // Validation: owner_id có thể NULL hoặc phải > 0 nếu được cung cấp
        if (apartment.getOwnerId() < 0) {
            throw new ValidationException("ID chủ hộ không hợp lệ.");
        }

        // Gọi DAO để thêm vào database
        return apartmentDAO.addApartment(apartment);
    }
}

