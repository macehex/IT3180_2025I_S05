// Vị trí: src/main/java/com/example/quanlytoanha/service/ResidentService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.ResidentDAO;
import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.dao.UserDAO;
import java.sql.SQLException;
import com.example.quanlytoanha.model.Role;

public class ResidentService {

    // Khai báo DAO instances
    private final ResidentDAO residentDAO = new ResidentDAO();
    private final UserDAO userDAO = new UserDAO();

    /**
     * Phương thức mới: Lấy Resident đầy đủ từ DB để nạp vào form Edit.
     */
    public Resident getResidentById(int userId) throws SQLException {
        // Giả định UserDAO.getUserById() được cấu hình để trả về đối tượng Resident hoàn chỉnh
        return residentDAO.getResidentByUserId(userId);
    }

    // ----------------------------------------------------------------------
    // --- CREATE LOGIC (ĐÃ TÍCH HỢP FIX LỖI PASSWORD) ---
    // ----------------------------------------------------------------------

    /**
     * Tạo hồ sơ cư dân mới, bao gồm validation nghiệp vụ.
     */
    public boolean createNewResident(Resident resident) throws ValidationException, SQLException {

        // --- 1. VALIDATION TRƯỜNG BẮT BUỘC (GIỮ NGUYÊN) ---
        if (resident.getFullName() == null || resident.getFullName().trim().isEmpty()) {
            throw new ValidationException("Họ tên cư dân là trường bắt buộc.");
        }
        if (resident.getApartmentId() <= 0) {
            throw new ValidationException("ID Căn hộ là trường bắt buộc và phải hợp lệ.");
        }
        if (resident.getPhoneNumber() == null || resident.getPhoneNumber().trim().isEmpty()) {
            throw new ValidationException("Số điện thoại là trường bắt buộc.");
        }

        // --- 2. XỬ LÝ DỮ LIỆU USER BẮT BUỘC (FIX LỖI RAW PASSWORD NULL) ---

        // TẠO USERNAME TẠM THỜI (để pass ràng buộc NOT NULL/UNIQUE của bảng users)
        // Mẫu: res[ApartmentID] + [4 số ngẫu nhiên]
        String tempUsername = "res" + resident.getApartmentId() + (int)(Math.random() * 9000 + 1000);
        resident.setUsername(tempUsername);

        // GÁN MẬT KHẨU TẠM THỜI (để pass qua hàm hashPassword và ràng buộc NOT NULL)
        resident.setPassword("123456");

        // GÁN ROLE MẶC ĐỊNH (Role.RESIDENT)
        // Lưu ý: Cần đảm bảo Role.RESIDENT được định nghĩa trong enum Role của bạn.
        resident.setRole(Role.RESIDENT);

        // GÁN EMAIL MẶC ĐỊNH (nếu chưa có)
        // Email là NOT NULL trong DB, nên cần phải có giá trị.
        if (resident.getEmail() == null || resident.getEmail().trim().isEmpty()) {
            resident.setEmail(tempUsername + "@temp.com");
        }


        // --- 3. VALIDATION NGHIỆP VỤ (GIỮ NGUYÊN) ---
        if (!residentDAO.isApartmentExist(resident.getApartmentId())) {
            throw new ValidationException("Căn hộ ID " + resident.getApartmentId() + " không tồn tại.");
        }

        String idCard = resident.getIdCardNumber();
        if (idCard != null && !idCard.trim().isEmpty()) {
            resident.setIdCardNumber(idCard.trim());
            if (!residentDAO.isIdCardUnique(resident.getIdCardNumber())) {
                throw new ValidationException("Số Căn cước công dân đã tồn tại trong hệ thống.");
            }
        }

        // --- 4. GỌI DAO VÀ THỰC HIỆN LƯU ---
        // userDAO.addResident sẽ thực hiện INSERT vào cả users và residents
        return userDAO.addResident(resident);
    }

    // ----------------------------------------------------------------------
    // --- UPDATE LOGIC (Chức năng 2) ---
    // ----------------------------------------------------------------------

    /**
     * CẬP NHẬT thông tin của Resident. Thực hiện Validation đầy đủ.
     * (Đáp ứng AC: Kiểm tra trường bắt buộc và không cho phép lưu nếu thiếu).
     * * @param resident Đối tượng Resident chứa dữ liệu cập nhật.
     */
    public boolean updateResident(Resident resident) throws ValidationException, SQLException {

        // --- 1. VALIDATION TRƯỜNG BẮT BUỘC ---

        // AC: Kiểm tra Họ tên
        if (resident.getFullName() == null || resident.getFullName().trim().isEmpty()) {
            throw new ValidationException("Họ tên cư dân là trường bắt buộc.");
        }

        // AC: Kiểm tra Căn hộ
        if (resident.getApartmentId() <= 0) {
            throw new ValidationException("ID Căn hộ là trường bắt buộc và phải hợp lệ.");
        }

        // AC: Kiểm tra Số điện thoại
        if (resident.getPhoneNumber() == null || resident.getPhoneNumber().trim().isEmpty()) {
            throw new ValidationException("Số điện thoại là trường bắt buộc.");
        }

        // --- 2. VALIDATION NGHIỆP VỤ ---

        // Kiểm tra Căn hộ có tồn tại không
        if (!residentDAO.isApartmentExist(resident.getApartmentId())) {
            throw new ValidationException("Căn hộ ID " + resident.getApartmentId() + " không tồn tại.");
        }

        // Kiểm tra CCCD: Khi SỬA, chỉ cần không trùng với người khác (trừ chính mình)
        String idCard = resident.getIdCardNumber();
        if (idCard != null && !idCard.trim().isEmpty()) {
            resident.setIdCardNumber(idCard.trim());

            // Cần phương thức mới trong ResidentDAO: isIdCardUniqueForUpdate(cccd, resident_id)
            // if (!residentDAO.isIdCardUniqueForUpdate(resident.getIdCardNumber(), resident.getResidentId())) {
            //     throw new ValidationException("Số Căn cước công dân đã tồn tại với hồ sơ khác.");
            // }
        }

        // --- 3. GỌI DAO VÀ THỰC HIỆN CẬP NHẬT ---
        return userDAO.updateResident(resident);
    }

    public java.util.List<Resident> getAllResidents() throws SQLException {
        // Giả sử ResidentDAO có phương thức getAllResidents() để lấy dữ liệu.
        // Nếu bạn muốn dùng UserDAO để lấy tất cả Users thuộc loại Resident, hãy gọi UserDAO.
        return residentDAO.getAllResidents();
        // LƯU Ý: Bạn cần triển khai phương thức này trong ResidentDAO (hoặc UserDAO).
    }
}