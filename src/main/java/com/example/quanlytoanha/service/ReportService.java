// Vị trí: src/main/java/com/example/quanlytoanha/service/ReportService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.ResidentDAO;
import com.example.quanlytoanha.dao.FinancialDAO; // <-- BỔ SUNG: Import
import com.example.quanlytoanha.model.ApartmentDebt; // <-- BỔ SUNG: Import
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List; // <-- BỔ SUNG: Import
import java.util.Map;

public class ReportService {

    private final ResidentDAO residentDAO = new ResidentDAO();
    private final FinancialDAO financialDAO = new FinancialDAO(); // <-- BỔ SUNG: Khởi tạo

    /**
     * BÁO CÁO BIẾN ĐỘNG DÂN CƯ (US7_2_1)
     * Xử lý logic nghiệp vụ và chuyển đổi kiểu Date.
     * @param startDate Ngày bắt đầu (từ DatePicker)
     * @param endDate Ngày kết thúc (từ DatePicker)
     * @return Map chứa ("moveIns" -> count) và ("moveOuts" -> count)
     */
    public Map<String, Integer> getPopulationReport(LocalDate startDate, LocalDate endDate) throws SQLException {

        // 1. Validation (Nghiệp vụ)
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc là bắt buộc.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc.");
        }

        // 2. Chuyển đổi kiểu dữ liệu
        // Từ java.time.LocalDate (JavaFX) sang java.sql.Date (PostgreSQL)
        java.sql.Date sqlStartDate = java.sql.Date.valueOf(startDate);
        java.sql.Date sqlEndDate = java.sql.Date.valueOf(endDate);

        // 3. Gọi DAO
        return residentDAO.getPopulationChangeStats(sqlStartDate, sqlEndDate);
    }
    // --- BỔ SUNG: HÀM MỚI CHO BÁO CÁO CÔNG NỢ (US7_2_1) ---

    /**
     * BÁO CÁO CÔNG NỢ CHI TIẾT (US7_2_1)
     * Lấy danh sách hóa đơn theo khoảng thời gian.
     */
    public List<ApartmentDebt> getDebtReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc là bắt buộc.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc.");
        }

        java.sql.Date sqlStartDate = java.sql.Date.valueOf(startDate);
        java.sql.Date sqlEndDate = java.sql.Date.valueOf(endDate);

        return financialDAO.getDebtReportByDateRange(sqlStartDate, sqlEndDate);
    }
}