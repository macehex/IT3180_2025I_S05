// Vị trí: src/main/java/com/example/quanlytoanha/service/ResidentService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.ResidentDAO;
import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.dao.UserDAO;
import com.example.quanlytoanha.dao.ApartmentDAO;
import com.example.quanlytoanha.model.Apartment;
import com.example.quanlytoanha.model.Role;
import com.google.gson.Gson; // <-- BỔ SUNG: Import Gson
import com.google.gson.GsonBuilder; // <-- BỔ SUNG: Import GsonBuilder (cho định dạng đẹp)

import java.sql.SQLException;

public class ResidentService {

    // Khai báo DAO instances
    private final ResidentDAO residentDAO = new ResidentDAO();
    private final UserDAO userDAO = new UserDAO();

    // --- BỔ SUNG: Khởi tạo Gson cho việc chuyển đổi JSON ---
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    /**
     * Phương thức mới: Lấy Resident đầy đủ từ DB để nạp vào form Edit.
     */
    public Resident getResidentById(int userId) throws SQLException {
        // Lưu ý: ResidentDAO.getResidentByUserId(userId) thực chất là userDAO.getResidentByUserId
        // Hàm này có vẻ bị lỗi định danh (nên là userDAO), nhưng ta giữ nguyên cách gọi của bạn
        return residentDAO.getResidentByUserId(userId);
    }

    // ----------------------------------------------------------------------
    // --- CREATE LOGIC (GIỮ NGUYÊN) ---
    // ----------------------------------------------------------------------

    /**
     * Tạo hồ sơ cư dân mới, bao gồm validation nghiệp vụ.
     */
    public boolean createNewResident(Resident resident, boolean forceTransferOwnership) throws ValidationException, SQLException {
        String idCard = resident.getIdCardNumber();

        // 1. Validation cơ bản (Giữ nguyên)
        if (resident.getFullName() == null || resident.getFullName().trim().isEmpty()) {
            throw new ValidationException("Họ tên cư dân là trường bắt buộc.");
        }
        if (resident.getApartmentId() <= 0) {
            throw new ValidationException("ID Căn hộ là trường bắt buộc và phải hợp lệ.");
        }
        if (resident.getPhoneNumber() == null || resident.getPhoneNumber().trim().isEmpty()) {
            throw new ValidationException("Số điện thoại là trường bắt buộc.");
        }

        // 2. Tạo username/password tạm (Giữ nguyên)
        String tempUsername;
        if (idCard != null && idCard.trim().length() >= 6) {
            String lastSixDigits = idCard.trim().substring(idCard.trim().length() - 6);
            tempUsername = "res" + resident.getApartmentId() + lastSixDigits;
        } else {
            tempUsername = "res" + resident.getApartmentId() + (int)(Math.random() * 9000 + 1000);
        }
        resident.setUsername(tempUsername);
        resident.setPassword("123456");
        resident.setRole(Role.RESIDENT);
        if (resident.getEmail() == null || resident.getEmail().trim().isEmpty()) {
            resident.setEmail(tempUsername + "@temp.com");
        }

        // 3. Validation Nghiệp vụ
        ApartmentDAO apartmentDAO = new ApartmentDAO(); // Khởi tạo tại đây để dùng
        if (!residentDAO.isApartmentExist(resident.getApartmentId())) {
            throw new ValidationException("Căn hộ ID " + resident.getApartmentId() + " không tồn tại.");
        }

        boolean isNewOwner = resident.getRelationship() != null &&
                (resident.getRelationship().equalsIgnoreCase("Chủ hộ"));

        if (isNewOwner) {
            Apartment existingApartment = apartmentDAO.getApartmentById(resident.getApartmentId());

            // Nếu căn hộ đã có chủ hộ và KHÔNG PHẢI chế độ ép buộc chuyển đổi
            if (existingApartment != null && existingApartment.getOwnerId() > 0 && !forceTransferOwnership) {
                // Ném ngoại lệ đặc biệt để Controller bắt được và hiện hộp thoại xác nhận
                throw new ValidationException("APARTMENT_HAS_OWNER");
            }
        }

        if (idCard != null && !idCard.trim().isEmpty()) {
            resident.setIdCardNumber(idCard.trim());
            if (!residentDAO.isIdCardUnique(resident.getIdCardNumber())) {
                throw new ValidationException("Số Căn cước công dân đã tồn tại trong hệ thống.");
            }
        }

        // 4. Lưu User & Resident
        boolean success = userDAO.addResident(resident);

        // 5. Cập nhật Chủ hộ (Nếu thành công và là Chủ hộ)
        if (success && isNewOwner) {
            try {
                Apartment existingApartment = apartmentDAO.getApartmentById(resident.getApartmentId());

                // Trường hợp 1: Có chủ hộ cũ và forceTransfer = true -> Dùng hàm chuyển chủ hộ
                if (existingApartment.getOwnerId() > 0 && forceTransferOwnership) {
                    apartmentDAO.transferApartmentOwner(
                            resident.getApartmentId(),
                            existingApartment.getOwnerId(), // Chủ cũ
                            resident.getUserId()            // Chủ mới (vừa tạo xong)
                    );
                }
                // Trường hợp 2: Chưa có chủ hộ -> Cập nhật bình thường
                else {
                    Apartment apartmentToUpdate = new Apartment();
                    apartmentToUpdate.setApartmentId(resident.getApartmentId());
                    apartmentToUpdate.setOwnerId(resident.getUserId());
                    apartmentToUpdate.setArea(existingApartment.getArea());
                    apartmentDAO.updateApartment(apartmentToUpdate);
                }
            } catch (SQLException e) {
                System.err.println("Cảnh báo: Lỗi cập nhật chủ hộ: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return success;
    }

    // ----------------------------------------------------------------------
    // --- UPDATE LOGIC (TÍCH HỢP AUDIT LOG - US1_1_1.4) ---
    // ----------------------------------------------------------------------

    /**
     * CẬP NHẬT thông tin của Resident và GHI LOG lịch sử.
     * HÀM NÀY THAY THẾ cho hàm updateResident cũ của bạn.
     * @param residentNewData Đối tượng Resident chứa dữ liệu cập nhật.
     * @param changedByUserId ID của Ban Quản trị thực hiện thay đổi.
     */
    public boolean updateResidentAndLog(Resident residentNewData, int changedByUserId) throws ValidationException, SQLException {

        // --- 1. VALIDATION TRƯỜNG BẮT BUỘC (GIỮ NGUYÊN) ---
        if (residentNewData.getFullName() == null || residentNewData.getFullName().trim().isEmpty()) {
            throw new ValidationException("Họ tên cư dân là trường bắt buộc.");
        }
        if (residentNewData.getApartmentId() <= 0) {
            throw new ValidationException("ID Căn hộ là trường bắt buộc và phải hợp lệ.");
        }
        if (residentNewData.getPhoneNumber() == null || residentNewData.getPhoneNumber().trim().isEmpty()) {
            throw new ValidationException("Số điện thoại là trường bắt buộc.");
        }

        // --- 2. LẤY DỮ LIỆU CŨ VÀ KIỂM TRA NGHIỆP VỤ ---

        // Lấy dữ liệu CŨ (Old Data) từ DB dựa trên residentId
        Resident residentOldData = userDAO.getResidentByResidentId(residentNewData.getResidentId());

        if (residentOldData == null) {
            throw new SQLException("Không tìm thấy hồ sơ cư dân cũ để cập nhật.");
        }

        // Kiểm tra Căn hộ có tồn tại không
        if (!residentDAO.isApartmentExist(residentNewData.getApartmentId())) {
            throw new ValidationException("Căn hộ ID " + residentNewData.getApartmentId() + " không tồn tại.");
        }

        // TODO: Kiểm tra CCCD (cần triển khai isIdCardUniqueForUpdate trong ResidentDAO)
        // ...

        // --- 3. CHUYỂN ĐỔI SANG JSON STRING ---

        // Chuyển dữ liệu CŨ và MỚI sang JSON String
        // Xử lý an toàn khi residentOldData có thể là null
        String oldDataJson = (residentOldData != null) ? gson.toJson(residentOldData) : null;
        String newDataJson = gson.toJson(residentNewData);

        // --- 4. GỌI DAO VÀ THỰC HIỆN CẬP NHẬT VÀ GHI LOG ---

        // userDAO.updateResidentAndLog sẽ xử lý Transaction và gọi ResidentHistoryDAO.addHistory()
        return userDAO.updateResidentAndLog(
                residentNewData,
                changedByUserId,
                oldDataJson,
                newDataJson
        );
    }

    public java.util.List<Resident> getAllResidents() throws SQLException {
        // Giữ nguyên logic của bạn
        return residentDAO.getAllResidents();
    }

    // --- LỚP TRỪU TƯỢNG CHO NGOẠI LỆ NGHIỆP VỤ ---
    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }
}