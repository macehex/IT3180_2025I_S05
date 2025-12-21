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

    /**
     * Cập nhật thông tin căn hộ với validation
     * @param apartment Đối tượng Apartment chứa thông tin mới
     * @return true nếu cập nhật thành công
     * @throws ValidationException nếu dữ liệu không hợp lệ
     * @throws SQLException nếu có lỗi database
     */
    public boolean updateApartment(Apartment apartment) throws ValidationException, SQLException {
        // Validation: Diện tích phải lớn hơn 0
        if (apartment.getArea() == null || apartment.getArea().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Diện tích căn hộ phải lớn hơn 0.");
        }

        // Validation: owner_id có thể NULL hoặc phải > 0 nếu được cung cấp
        if (apartment.getOwnerId() < 0) {
            throw new ValidationException("ID chủ hộ không hợp lệ.");
        }

        // Validation: Apartment ID phải hợp lệ
        if (apartment.getApartmentId() <= 0) {
            throw new ValidationException("ID căn hộ không hợp lệ.");
        }

        // Xử lý chuyển chủ hộ: nếu owner_id thay đổi, cập nhật quan hệ của chủ hộ cũ và mới
        Apartment oldApartment = apartmentDAO.getApartmentById(apartment.getApartmentId());
        if (oldApartment != null && oldApartment.getOwnerId() > 0 && 
            apartment.getOwnerId() > 0 && oldApartment.getOwnerId() != apartment.getOwnerId()) {
            // Chuyển chủ hộ: cập nhật quan hệ của chủ hộ cũ và mới
            apartmentDAO.transferApartmentOwner(apartment.getApartmentId(), 
                                                oldApartment.getOwnerId(), 
                                                apartment.getOwnerId());
        }

        // Gọi DAO để cập nhật
        return apartmentDAO.updateApartment(apartment);
    }

    /**
     * Xóa căn hộ
     * @param apartmentId ID căn hộ cần xóa
     * @return true nếu xóa thành công
     * @throws ValidationException nếu căn hộ có người ở
     * @throws SQLException nếu có lỗi database
     */
    public boolean deleteApartment(int apartmentId) throws ValidationException, SQLException {
        if (apartmentId <= 0) {
            return false;
        }
        
        // Kiểm tra căn hộ có người ở không
        if (apartmentDAO.hasResidents(apartmentId)) {
            throw new ValidationException("Không thể xóa căn hộ đang có người ở.");
        }
        
        return apartmentDAO.deleteApartment(apartmentId);
    }
}

