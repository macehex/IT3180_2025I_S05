// Vị trí: src/main/java/com/example/quanlytoanha/service/InvoiceCalculationService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.ApartmentDAO;
import com.example.quanlytoanha.model.Apartment;
import com.example.quanlytoanha.model.FeeType;

import java.math.BigDecimal;

public class InvoiceCalculationService {

    private ApartmentDAO apartmentDAO;

    public InvoiceCalculationService() {
        this.apartmentDAO = new ApartmentDAO();
    }

    /**
     * Tính toán số tiền cuối cùng cho 1 loại phí của 1 căn hộ
     */
    public BigDecimal calculateFeeAmount(FeeType fee, int apartmentId) {

        // (Chúng ta chỉ xử lý 2 trường hợp bạn yêu cầu)

        switch (fee.getPricingModel()) {
            case "FIXED":
                // 1. Lấy thẳng đơn giá
                return fee.getUnitPrice();

            case "PER_SQM":
                // 2. Lấy đơn giá * diện tích
                Apartment apartment = apartmentDAO.getApartmentById(apartmentId);
                if (apartment == null || apartment.getArea() == null) {
                    System.err.println("Lỗi: Không tìm thấy căn hộ hoặc diện tích cho apartmentId: " + apartmentId);
                    return BigDecimal.ZERO;
                }
                // (apartment.getArea() đọc từ model Apartment.java)
                return fee.getUnitPrice().multiply(apartment.getArea());

            case "VOLUNTARY":
                return fee.getUnitPrice();

            default:
                // Nếu là 'PER_UNIT' (điện, nước) mà chúng ta chưa làm
                // Tạm thời trả về 0
                System.err.println("Bỏ qua phí: " + fee.getFeeName() + " (chưa hỗ trợ pricing_model: " + fee.getPricingModel() + ")");
                return BigDecimal.ZERO;
        }
    }
}
