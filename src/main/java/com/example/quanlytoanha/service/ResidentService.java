package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.ResidentDAO;
import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.model.Role;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý logic nghiệp vụ cho quản lý cư dân
 */
public class ResidentService {
    
    private final ResidentDAO residentDAO;
    
    public ResidentService() {
        this.residentDAO = new ResidentDAO();
    }
    
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
     * @return Danh sách tất cả cư dân
     * @throws SecurityException nếu không có quyền truy cập
     */
    public List<Resident> getAllResidents() throws SecurityException {
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
    
}
