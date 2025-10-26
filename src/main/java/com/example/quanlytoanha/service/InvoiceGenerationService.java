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
}
