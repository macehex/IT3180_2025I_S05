// Vị trí: src/main/java/com/example/quanlytoanha/service/ResidentService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.ResidentDAO;
import com.example.quanlytoanha.model.ResidentPOJO;

import java.sql.SQLException;

public class ResidentService {

    private final ResidentDAO residentDAO = new ResidentDAO();

    /**
     * Tạo hồ sơ cư dân mới, bao gồm validation nghiệp vụ.
     * @param resident POJO chứa dữ liệu cư dân.
     * @return true nếu thêm thành công.
     * @throws ValidationException Nếu dữ liệu đầu vào không hợp lệ (AC: Giả sử tôi nhập thiếu...).
     * @throws SQLException Nếu có lỗi DB.
     */
    public boolean createNewResident(ResidentPOJO resident) throws ValidationException, SQLException {

        // --- 1. VALIDATION TRƯỜNG BẮT BUỘC (AC: Giả sử tôi nhập thiếu...) ---

        // Kiểm tra Tên (AC: Họ tên bắt buộc)
        if (resident.getFullName() == null || resident.getFullName().trim().isEmpty()) {
            throw new ValidationException("Họ tên cư dân là trường bắt buộc.");
        }

        // Kiểm tra Căn hộ (AC: Căn hộ bắt buộc)
        if (resident.getApartmentId() == null || resident.getApartmentId() <= 0) {
            throw new ValidationException("ID Căn hộ là trường bắt buộc và phải hợp lệ.");
        }

        // --- 2. VALIDATION NGHIỆP VỤ (Gọi DAO để kiểm tra) ---

        // Kiểm tra Căn hộ có tồn tại không
        if (!residentDAO.isApartmentExist(resident.getApartmentId())) {
            throw new ValidationException("Căn hộ ID " + resident.getApartmentId() + " không tồn tại.");
        }

        // Kiểm tra Số CCCD đã tồn tại chưa (nếu Admin nhập vào)
        String idCard = resident.getIdCardNumber();
        if (idCard != null && !idCard.trim().isEmpty()) {
            // Định dạng lại CCCD (xóa khoảng trắng nếu có)
            resident.setIdCardNumber(idCard.trim());
            if (!residentDAO.isIdCardUnique(resident.getIdCardNumber())) {
                throw new ValidationException("Số Căn cước công dân đã tồn tại trong hệ thống.");
            }
        }

        // --- 3. GỌI DAO VÀ THỰC HIỆN LƯU ---
        return residentDAO.addResident(resident);
    }
}
