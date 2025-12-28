package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.ApartmentDAO;
import com.example.quanlytoanha.dao.VehicleDAO;
import com.example.quanlytoanha.model.Apartment;
import com.example.quanlytoanha.model.FeeType;

import java.math.BigDecimal;

/**
 * Service xử lý logic tính toán tiền phí cho từng loại dịch vụ.
 * Đảm bảo tính chính xác cho các nghiệp vụ v1.0 (Diện tích) và v2.0 (Gửi xe).
 */
public class InvoiceCalculationService {

    private ApartmentDAO apartmentDAO;
    private VehicleDAO vehicleDAO;

    public InvoiceCalculationService() {
        this.apartmentDAO = new ApartmentDAO();
        this.vehicleDAO = new VehicleDAO();
    }

    /**
     * Phương thức chính để tính số tiền dựa trên Pricing Model của loại phí.
     * * @param fee Thông tin loại phí (chứa đơn giá và hình thức tính).
     * @param apartmentId ID của căn hộ cần tính phí.
     * @return BigDecimal Số tiền cuối cùng cần đóng.
     */
    public BigDecimal calculateFeeAmount(FeeType fee, int apartmentId) {
        // Kiểm tra đầu vào cơ bản
        if (fee == null || fee.getUnitPrice() == null || apartmentId <= 0) {
            return BigDecimal.ZERO;
        }

        switch (fee.getPricingModel()) {
            case "FIXED":
                // Áp dụng cho các khoản phí cố định hàng tháng không đổi.
                return fee.getUnitPrice();

            case "PER_SQM":
                // NGHIỆP VỤ v1.0: Phí dịch vụ chung cư & Phí quản lý.
                // Công thức: Đơn giá * Diện tích căn hộ (m2).
                Apartment apartment = apartmentDAO.getApartmentById(apartmentId);
                if (apartment == null || apartment.getArea() == null) {
                    System.err.println("[InvoiceService] Lỗi: Không tìm thấy diện tích cho căn hộ ID: " + apartmentId);
                    return BigDecimal.ZERO;
                }
                // Ví dụ: 7.000 đ/m2 * 65 m2 = 455.000 đ
                return fee.getUnitPrice().multiply(apartment.getArea());

            case "PER_VEHICLE":
                // NGHIỆP VỤ v2.0: Phí gửi xe (Ô tô/Xe máy).
                // Công thức: Đơn giá * Số lượng phương tiện đang gửi.
                String feeName = fee.getFeeName() != null ? fee.getFeeName().toLowerCase() : "";
                String vehicleType = "MOTORBIKE"; // Mặc định là xe máy

                // Tự động nhận diện loại xe để đếm dựa trên tên phí
                if (feeName.contains("ô tô") || feeName.contains("car") || feeName.contains("xe hơi")) {
                    vehicleType = "CAR";
                } else if (feeName.contains("xe máy") || feeName.contains("moto") || feeName.contains("xe điện")) {
                    vehicleType = "MOTORBIKE";
                }

                // Gọi DAO để đếm số lượng xe thực tế của hộ gia đình
                int vehicleCount = vehicleDAO.countActiveVehiclesByType(apartmentId, vehicleType);

                // Tránh trường hợp đơn giá bị âm hoặc bằng 0
                BigDecimal unitPrice = fee.getUnitPrice().max(BigDecimal.ZERO);

                // Ví dụ: 1.200.000 đ/xe * 1 xe ô tô = 1.200.000 đ
                return unitPrice.multiply(new BigDecimal(vehicleCount));

            case "VOLUNTARY":
                // Các khoản đóng góp tự nguyện (thường đơn giá khởi tạo là 0).
                return fee.getUnitPrice();

            default:
                // Các trường hợp khác (như Điện/Nước) sẽ trả về 0 cho đến khi triển khai module chỉ số.
                System.err.println("[InvoiceService] Cảnh báo: Pricing model '" + fee.getPricingModel() + "' chưa được xử lý.");
                return BigDecimal.ZERO;
        }
    }
}