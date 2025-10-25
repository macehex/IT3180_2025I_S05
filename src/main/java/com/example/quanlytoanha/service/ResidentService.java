// Vị trí: src/main/java/com/example/quanlytoanha/service/ResidentService.java
package com.example.quanlytoanha.service;

// GỘP TẤT CẢ IMPORT TỪ CẢ 2 BRANCH
import com.example.quanlytoanha.dao.ResidentDAO;
import com.example.quanlytoanha.dao.UserDAO;
import com.example.quanlytoanha.exception.ValidationException; // Thêm exception từ branch login
import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.model.Role;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý logic nghiệp vụ cho quản lý cư dân
 * (ĐÃ GỘP TÍNH NĂNG TỪ 2 BRANCH)
 */
public class ResidentService {

    // GỘP TẤT CẢ DAO
    private final ResidentDAO residentDAO;
    private final UserDAO userDAO;

    // GỘP CONSTRUCTOR
    public ResidentService() {
        this.residentDAO = new ResidentDAO();
        this.userDAO = new UserDAO();
    }

    // ====================================================================
    // --- PHẦN TỪ BRANCH 'feature/view-filter' (Admin View & Security) ---
    // ====================================================================

    /**
     * Kiểm tra quyền truy cập - chỉ Ban quản trị mới được sử dụng các chức năng này
     * @return true nếu có quyền, false nếu không
     */
    public boolean hasAdminPermission() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // Kiểm tra role có phải là ADMIN không
        return currentUser.getRole() == Role.ADMIN;
    }

    /**
     * Lấy danh sách tất cả cư dân (chỉ dành cho Ban quản trị)
     * (Giữ phiên bản này vì nó có kiểm tra bảo mật)
     * @return Danh sách tất cả cư dân
     * @throws SecurityException nếu không có quyền truy cập
     */
    public List<Resident> getAllResidents() throws SecurityException, SQLException {
        if (!hasAdminPermission()) {
            throw new SecurityException("Chỉ Ban quản trị mới có quyền xem danh sách cư dân");
        }

        return residentDAO.getAllResidents();
    }

    /**
     * Tìm kiếm cư dân theo tiêu chí đơn giản (chỉ dành cho Ban quản trị)
     * @param fullName Tên cư dân (có thể null)
     * @param apartmentId ID căn hộ (có thể null)
     * @param status Trạng thái (có thể null)
     * @return Danh sách cư dân thỏa mãn tiêu chí
     * @throws SecurityException nếu không có quyền truy cập
     */
    public List<Resident> searchResidents(String fullName, Integer apartmentId, String status) throws SecurityException {
        if (!hasAdminPermission()) {
            throw new SecurityException("Chỉ Ban quản trị mới có quyền tìm kiếm cư dân");
        }

        return residentDAO.searchResidents(fullName, apartmentId, status);
    }

    /**
     * Lấy thông tin chi tiết của một cư dân (chỉ dành cho Ban quản trị)
     * LƯU Ý: Lấy bằng residentId
     * @param residentId ID của cư dân
     * @return Thông tin cư dân hoặc null nếu không tìm thấy
     * @throws SecurityException nếu không có quyền truy cập
     */
    public Resident getResidentById(int residentId) throws SecurityException {
        if (!hasAdminPermission()) {
            throw new SecurityException("Chỉ Ban quản trị mới có quyền xem thông tin cư dân");
        }

        return residentDAO.getResidentById(residentId);
    }

    /**
     * Lấy thống kê tổng quan về cư dân (chỉ dành cho Ban quản trị)
     * @return Map chứa các thống kê
     * @throws SecurityException nếu không có quyền truy cập
     */
    public Map<String, Object> getResidentStatistics() throws SecurityException {
        if (!hasAdminPermission()) {
            throw new SecurityException("Chỉ Ban quản trị mới có quyền xem thống kê cư dân");
        }

        Map<String, Object> statistics = new HashMap<>();

        // Tổng số cư dân
        int totalResidents = residentDAO.getTotalResidentCount();
        statistics.put("totalResidents", totalResidents);

        // Số cư dân đang ở
        int residingCount = residentDAO.getResidentCountByStatus("RESIDING");
        statistics.put("residingCount", residingCount);

        // Số cư dân đã chuyển đi
        int movedOutCount = residentDAO.getResidentCountByStatus("MOVED_OUT");
        statistics.put("movedOutCount", movedOutCount);

        // Tỷ lệ cư dân đang ở
        double residingPercentage = totalResidents > 0 ?
                (double) residingCount / totalResidents * 100 : 0;
        statistics.put("residingPercentage", Math.round(residingPercentage * 100.0) / 100.0);

        return statistics;
    }

    /**
     * Lấy danh sách các trạng thái cư dân có thể có
     * @return Danh sách các trạng thái
     */
    public List<String> getAvailableStatuses() {
        return List.of("RESIDING", "MOVED_OUT", "TEMPORARY_ABSENCE", "PENDING_APPROVAL");
    }

    /**
     * Lấy danh sách các quan hệ với chủ hộ có thể có
     * @return Danh sách các quan hệ
     */
    public List<String> getAvailableRelationships() {
        return List.of("OWNER", "SPOUSE", "CHILD", "PARENT", "SIBLING", "RELATIVE", "TENANT");
    }

    // ====================================================================
    // --- PHẦN TỪ BRANCH 'topic/login-logout' (Create, Update, Validation) ---
    // ====================================================================

    /**
     * Phương thức mới: Lấy Resident đầy đủ từ DB để nạp vào form Edit.
     * LƯU Ý: Lấy bằng userId
     * @param userId ID của User (Resident)
     * @return Đối tượng Resident hoàn chỉnh
     * @throws SQLException Nếu có lỗi DB
     */
    public Resident getResidentByUserId(int userId) throws SQLException {
        // (Đổi tên phương thức để tránh trùng lặp, giữ logic của branch login)
        return (Resident) userDAO.getUserById(userId);
    }

    // ----------------------------------------------------------------------
    // --- CREATE LOGIC ---
    // ----------------------------------------------------------------------

    /**
     * Tạo hồ sơ cư dân mới, bao gồm validation nghiệp vụ.
     */
    public boolean createNewResident(Resident resident) throws ValidationException, SQLException {
        // --- 1. VALIDATION TRƯỜNG BẮT BUỘC ---
        if (resident.getFullName() == null || resident.getFullName().trim().isEmpty()) {
            throw new ValidationException("Họ tên cư dân là trường bắt buộc.");
        }
        if (resident.getApartmentId() <= 0) {
            throw new ValidationException("ID Căn hộ là trường bắt buộc và phải hợp lệ.");
        }

        // Giả sử Số điện thoại cũng bắt buộc khi tạo
        if (resident.getPhoneNumber() == null || resident.getPhoneNumber().trim().isEmpty()) {
            throw new ValidationException("Số điện thoại là trường bắt buộc.");
        }

        // --- 2. VALIDATION NGHIỆP VỤ ---
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

        // --- 3. GỌI DAO VÀ THỰC HIỆN LƯU ---
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
}