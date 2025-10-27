package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.ApartmentDAO;
import com.example.quanlytoanha.dao.FeeTypeDAO;
import com.example.quanlytoanha.dao.InvoiceDAO;
import com.example.quanlytoanha.model.Apartment;
import com.example.quanlytoanha.model.FeeType;
import com.example.quanlytoanha.model.Invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public class InvoiceGenerationService {

    private ApartmentDAO apartmentDAO;
    private FeeTypeDAO feeTypeDAO;
    private InvoiceDAO invoiceDAO;
    private InvoiceCalculationService calculationService;

    public InvoiceGenerationService() {
        this.apartmentDAO = new ApartmentDAO();
        this.feeTypeDAO = new FeeTypeDAO();
        this.invoiceDAO = new InvoiceDAO();
        this.calculationService = new InvoiceCalculationService();
    }

    /**
     * Hàm chính: Tạo hóa đơn cho TẤT CẢ căn hộ trong tháng
     */
    public String generateMonthlyInvoices(LocalDate billingMonth) {
        List<Apartment> apartments = apartmentDAO.getAllApartments();
        int successCount = 0;
        int skippedCount = 0;

        for (Apartment apartment : apartments) {

            // 1. Kiểm tra xem đã có hóa đơn cho tháng này chưa
            if (invoiceDAO.checkIfInvoiceExists(apartment.getApartmentId(), billingMonth)) {
                skippedCount++;
                continue; // Bỏ qua nếu đã tồn tại
            }

            // 2. Lấy tất cả phí của căn hộ này
            List<FeeType> defaultFees = feeTypeDAO.getAllDefaultFees();
            List<FeeType> optionalFees = feeTypeDAO.getOptionalFeesForApartment(apartment.getApartmentId());
            // Gộp 2 danh sách lại
            List<FeeType> allFeesToBill = Stream.concat(defaultFees.stream(), optionalFees.stream()).toList();

            if (allFeesToBill.isEmpty()) {
                continue; // Căn hộ này không có gì để tính
            }

            BigDecimal totalAmount = BigDecimal.ZERO;

            // 3. Tạo Hóa đơn cha (với tổng tiền = 0)
            Invoice newInvoice = invoiceDAO.createInvoiceHeader(apartment.getApartmentId(), billingMonth);
            if (newInvoice == null) {
                System.err.println("Không thể tạo hóa đơn cha cho căn hộ: " + apartment.getApartmentId());
                continue;
            }

            // 4. Lặp qua từng phí để tính toán và tạo chi tiết
            for (FeeType fee : allFeesToBill) {
                BigDecimal amount = calculationService.calculateFeeAmount(fee, apartment.getApartmentId());

                // Thêm chi tiết (với cả ID và Tên)
                invoiceDAO.addInvoiceDetail(newInvoice.getInvoiceId(), fee.getFeeId(), fee.getFeeName(), amount);

                totalAmount = totalAmount.add(amount);
            }

            // 5. Cập nhật lại tổng tiền cho Hóa đơn cha
            invoiceDAO.updateInvoiceTotal(newInvoice.getInvoiceId(), totalAmount);
            successCount++;
        }

        return String.format("Hoàn thành:\n- Đã tạo mới: %d hóa đơn.\n- Đã bỏ qua (đã tồn tại): %d hóa đơn.", successCount, skippedCount);
    }

    /**
     * HÀM MỚI: Tính toán lại hóa đơn cho TẤT CẢ căn hộ trong tháng
     */
    public String recalculateMonthlyInvoices(LocalDate billingMonth) {
        List<Apartment> apartments = apartmentDAO.getAllApartments();
        if (apartments == null || apartments.isEmpty()) {
            return "Không tìm thấy căn hộ nào để tính toán lại hóa đơn.";
        }

        int successCount = 0;
        int notFoundCount = 0; // Đếm hóa đơn không tìm thấy
        int errorCount = 0;

        System.out.println("Bắt đầu TÍNH TOÁN LẠI hóa đơn cho tháng: " + billingMonth + " cho " + apartments.size() + " căn hộ.");

        for (Apartment apartment : apartments) {
            try {
                // 1. Tìm ID hóa đơn hiện có cho căn hộ và tháng này
                Integer existingInvoiceId = invoiceDAO.findInvoiceIdByApartmentAndMonth(apartment.getApartmentId(), billingMonth);

                if (existingInvoiceId == null) {
                    System.out.println("Bỏ qua căn hộ " + apartment.getApartmentId() + ": Không tìm thấy hóa đơn tháng " + billingMonth.getMonthValue() + " để tính lại.");
                    notFoundCount++;
                    continue; // Bỏ qua nếu không có hóa đơn để tính lại
                }

                System.out.println("Đang tính toán lại HĐ #" + existingInvoiceId + " cho căn hộ " + apartment.getApartmentId() + "...");

                // 2. XÓA tất cả chi tiết cũ của hóa đơn này
                boolean deleted = invoiceDAO.deleteInvoiceDetails(existingInvoiceId);
                if (!deleted) {
                    System.err.println("Lỗi: Không thể xóa chi tiết cũ của HĐ #" + existingInvoiceId);
                    errorCount++;
                    continue;
                }
                System.out.println(" -> Đã xóa chi tiết cũ.");

                // 3. Lấy lại danh sách phí HIỆN TẠI áp dụng cho căn hộ
                List<FeeType> defaultFees = feeTypeDAO.getAllDefaultFees();
                List<FeeType> optionalFees = feeTypeDAO.getOptionalFeesForApartment(apartment.getApartmentId());
                List<FeeType> allFeesToBill = Stream.concat(
                        defaultFees != null ? defaultFees.stream() : Stream.empty(),
                        optionalFees != null ? optionalFees.stream() : Stream.empty()
                ).toList();

                BigDecimal totalAmount = BigDecimal.ZERO;

                // 4. Lặp qua từng phí để tính toán và THÊM LẠI chi tiết MỚI
                for (FeeType fee : allFeesToBill) {
                    BigDecimal amount = calculationService.calculateFeeAmount(fee, apartment.getApartmentId());
                    System.out.println("   -> Tính phí '" + fee.getFeeName() + "': " + amount);

                    if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                        invoiceDAO.addInvoiceDetail(existingInvoiceId, fee.getFeeId(), fee.getFeeName(), amount);
                        totalAmount = totalAmount.add(amount);
                    } else {
                        System.out.println("   -> Bỏ qua thêm chi tiết cho '" + fee.getFeeName() + "' vì số tiền là 0 hoặc null.");
                    }
                }

                // 5. Cập nhật lại tổng tiền MỚI cho hóa đơn
                System.out.println(" -> Cập nhật tổng tiền MỚI cho HĐ #" + existingInvoiceId + ": " + totalAmount);
                invoiceDAO.updateInvoiceTotal(existingInvoiceId, totalAmount);
                successCount++;

            } catch (Exception e) {
                System.err.println("Lỗi nghiêm trọng khi tính toán lại cho căn hộ " + apartment.getApartmentId() + ": " + e.getMessage());
                e.printStackTrace();
                errorCount++;
            }
        }

        System.out.println("Kết thúc tính toán lại. Thành công: " + successCount + ", Không tìm thấy: " + notFoundCount + ", Lỗi: " + errorCount);

        return String.format("Hoàn thành:\n- Đã tính toán lại: %d hóa đơn.\n- Không tìm thấy hóa đơn gốc: %d hóa đơn.\n- Đã xảy ra lỗi: %d hóa đơn.",
                successCount, notFoundCount, errorCount);
    }
}
