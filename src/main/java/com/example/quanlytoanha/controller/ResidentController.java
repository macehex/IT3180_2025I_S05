package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.service.ResidentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý các request liên quan đến quản lý cư dân
 * Chỉ dành cho Ban quản trị
 */
public class ResidentController {
    
    private final ResidentService residentService;
    
    public ResidentController() {
        this.residentService = new ResidentService();
    }
    
    /**
     * Xử lý request lấy danh sách tất cả cư dân
     * @return Map chứa kết quả và thông báo
     */
    public Map<String, Object> getAllResidents() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Resident> residents = residentService.getAllResidents();
            
            response.put("success", true);
            response.put("message", "Lấy danh sách cư dân thành công");
            response.put("data", residents);
            response.put("totalCount", residents.size());
            
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("errorCode", "ACCESS_DENIED");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            response.put("errorCode", "SYSTEM_ERROR");
        }
        
        return response;
    }
    
    /**
     * Xử lý request tìm kiếm cư dân với các tham số đơn giản
     * @param fullName Tên cư dân (có thể null)
     * @param apartmentId ID căn hộ (có thể null)
     * @param status Trạng thái (có thể null)
     * @return Map chứa kết quả và thông báo
     */
    public Map<String, Object> searchResidentsSimple(String fullName, Integer apartmentId, String status) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Resident> residents = residentService.searchResidents(fullName, apartmentId, status);
            
            response.put("success", true);
            response.put("message", "Tìm kiếm cư dân thành công");
            response.put("data", residents);
            response.put("totalCount", residents.size());
            
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("errorCode", "ACCESS_DENIED");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            response.put("errorCode", "SYSTEM_ERROR");
        }
        
        return response;
    }
    
    /**
     * Xử lý request lấy thông tin chi tiết của một cư dân
     * @param residentId ID của cư dân
     * @return Map chứa kết quả và thông báo
     */
    public Map<String, Object> getResidentById(int residentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (residentId <= 0) {
                response.put("success", false);
                response.put("message", "ID cư dân không hợp lệ");
                response.put("errorCode", "INVALID_ID");
                return response;
            }
            
            Resident resident = residentService.getResidentById(residentId);
            
            if (resident == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy cư dân với ID: " + residentId);
                response.put("errorCode", "NOT_FOUND");
            } else {
                response.put("success", true);
                response.put("message", "Lấy thông tin cư dân thành công");
                response.put("data", resident);
            }
            
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("errorCode", "ACCESS_DENIED");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            response.put("errorCode", "SYSTEM_ERROR");
        }
        
        return response;
    }
    
    /**
     * Xử lý request lấy thống kê tổng quan về cư dân
     * @return Map chứa kết quả và thông báo
     */
    public Map<String, Object> getResidentStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> statistics = residentService.getResidentStatistics();
            
            response.put("success", true);
            response.put("message", "Lấy thống kê cư dân thành công");
            response.put("data", statistics);
            
        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("errorCode", "ACCESS_DENIED");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            response.put("errorCode", "SYSTEM_ERROR");
        }
        
        return response;
    }
}