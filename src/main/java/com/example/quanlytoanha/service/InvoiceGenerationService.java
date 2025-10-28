// Vị trí: src/main/java/com/example/quanlytoanha/service/InvoiceGenerationService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.ApartmentDAO;
import com.example.quanlytoanha.dao.FeeTypeDAO;
import com.example.quanlytoanha.dao.InvoiceDAO;
// --- THÊM IMPORT ---
import com.example.quanlytoanha.service.NotificationService; // Cần để gửi thông báo
// -----------------
import com.example.quanlytoanha.model.Apartment;
import com.example.quanlytoanha.model.FeeType;
import com.example.quanlytoanha.model.Invoice;

import java.math.BigDecimal;
import java.sql.SQLException; // Thêm import
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public class InvoiceGenerationService {

    private ApartmentDAO apartmentDAO;
    private FeeTypeDAO feeTypeDAO;
    private InvoiceDAO invoiceDAO;
    private InvoiceCalculationService calculationService;
    // --- THÊM SERVICE MỚI ---
    private NotificationService notificationService;
    // ----------------------

    public InvoiceGenerationService() {
        this.apartmentDAO = new ApartmentDAO();
        this.feeTypeDAO = new FeeTypeDAO();
        this.invoiceDAO = InvoiceDAO.getInstance();
        this.calculationService = new InvoiceCalculationService();
        // --- KHỞI TẠO SERVICE MỚI ---
        this.notificationService = new NotificationService();
        // ---------------------------
    }

    /**
     * Hàm chính: Tạo hóa đơn cho TẤT CẢ căn hộ trong tháng
     */
    public String generateMonthlyInvoices(LocalDate billingMonth) {
        // ... (Code lấy apartments, successCount, skippedCount, errorCount không đổi) ...
        List<Apartment> apartments = apartmentDAO.getAllApartments();
        if (apartments == null || apartments.isEmpty()) {
            return "Không tìm thấy căn hộ nào để tạo hóa đơn.";
        }
        int successCount = 0;
        int skippedCount = 0;
        int errorCount = 0;
        System.out.println("Bắt đầu tạo hóa đơn cho tháng: " + billingMonth + " cho " + apartments.size() + " căn hộ.");


        for (Apartment apartment : apartments) {
            Invoice createdInvoice = null; // Biến để lưu hóa đơn vừa tạo
            try {
                // 1. Kiểm tra tồn tại (không đổi)
                if (invoiceDAO.checkIfInvoiceExists(apartment.getApartmentId(), billingMonth)) {
                    System.out.println("Bỏ qua căn hộ " + apartment.getApartmentId() + ": Hóa đơn tháng " + billingMonth.getMonthValue() + " đã tồn tại.");
                    skippedCount++;
                    continue;
                }

                // 2. Lấy phí (không đổi)
                List<FeeType> defaultFees = feeTypeDAO.getAllDefaultFees();
                List<FeeType> optionalFees = feeTypeDAO.getOptionalFeesForApartment(apartment.getApartmentId());
                List<FeeType> allFeesToBill = Stream.concat(
                        defaultFees != null ? defaultFees.stream() : Stream.empty(),
                        optionalFees != null ? optionalFees.stream() : Stream.empty()
                ).toList();
                if (allFeesToBill.isEmpty()) {
                    System.out.println("Bỏ qua căn hộ " + apartment.getApartmentId() + ": Không có phí nào được áp dụng.");
                    continue;
                }
                System.out.println("Đang xử lý căn hộ " + apartment.getApartmentId() + " với " + allFeesToBill.size() + " loại phí...");

                BigDecimal totalAmount = BigDecimal.ZERO;

                // 3. Tạo Hóa đơn cha (không đổi)
                Invoice newInvoice = invoiceDAO.createInvoiceHeader(apartment.getApartmentId(), billingMonth);
                if (newInvoice == null) {
                    System.err.println("Lỗi: Không thể tạo hóa đơn cha cho căn hộ: " + apartment.getApartmentId());
                    errorCount++;
                    continue;
                }
                createdInvoice = newInvoice; // Lưu lại hóa đơn vừa tạo
                System.out.println(" -> Đã tạo HĐ cha #" + newInvoice.getInvoiceId() + " cho căn hộ " + apartment.getApartmentId());

                // 4. Lặp phí, tính toán, thêm chi tiết (không đổi)
                for (FeeType fee : allFeesToBill) {
                    BigDecimal amount = calculationService.calculateFeeAmount(fee, apartment.getApartmentId());
                    System.out.println("   -> Tính phí '" + fee.getFeeName() + "': " + amount);
                    if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                        invoiceDAO.addInvoiceDetail(newInvoice.getInvoiceId(), fee.getFeeId(), fee.getFeeName(), amount);
                        totalAmount = totalAmount.add(amount);
                    } else {
                        System.out.println("   -> Bỏ qua thêm chi tiết cho '" + fee.getFeeName() + "' vì số tiền là 0 hoặc null.");
                    }
                }

                // 5. Cập nhật tổng tiền (không đổi)
                System.out.println(" -> Cập nhật tổng tiền cho HĐ #" + newInvoice.getInvoiceId() + ": " + totalAmount);
                invoiceDAO.updateInvoiceTotal(newInvoice.getInvoiceId(), totalAmount);
                successCount++;

                // --- GỬI THÔNG BÁO HÓA ĐƠN MỚI (PHẦN MỚI) ---
                // GIẢ ĐỊNH: Luôn gửi vì chưa có cách đọc cấu hình
                try {
                    // Cần lấy owner_id từ ApartmentDAO hoặc InvoiceDAO
                    // Giả sử ApartmentDAO.getApartmentById trả về Apartment có ownerId
                    Apartment fullApartmentInfo = apartmentDAO.getApartmentById(apartment.getApartmentId());
                    if (fullApartmentInfo != null && fullApartmentInfo.getOwnerId() > 0) {
                        System.out.println("   -> Đang gửi thông báo hóa đơn mới cho User ID: " + fullApartmentInfo.getOwnerId());
                        // Cập nhật lại totalAmount cho đối tượng Invoice trước khi gửi
                        createdInvoice.setTotalAmount(totalAmount);
                        notificationService.sendNewInvoiceNotification(fullApartmentInfo.getOwnerId(), createdInvoice);
                    } else {
                        System.err.println("   -> Lỗi: Không tìm thấy Owner ID cho căn hộ " + apartment.getApartmentId() + " để gửi thông báo.");
                    }
                } catch (Exception notifyEx) {
                    System.err.println("   -> Lỗi khi gửi thông báo hóa đơn mới cho HĐ #" + createdInvoice.getInvoiceId() + ": " + notifyEx.getMessage());
                    // Không tăng errorCount ở đây, chỉ log lỗi gửi thông báo
                }
                // --- KẾT THÚC PHẦN GỬI THÔNG BÁO ---


            } catch (Exception e) {
                System.err.println("Lỗi nghiêm trọng khi xử lý căn hộ " + apartment.getApartmentId() + ": " + e.getMessage());
                e.printStackTrace();
                errorCount++;
            }
        }

        // ... (Code log và return không đổi) ...
        System.out.println("Kết thúc tạo hóa đơn. Thành công: " + successCount + ", Bỏ qua: " + skippedCount + ", Lỗi: " + errorCount);
        return String.format("Hoàn thành:\n- Đã tạo mới: %d hóa đơn.\n- Đã bỏ qua (đã tồn tại): %d hóa đơn.\n- Đã xảy ra lỗi: %d hóa đơn.",
                successCount, skippedCount, errorCount);
    }

    // ... (Hàm recalculateMonthlyInvoices không thay đổi) ...
    public String recalculateMonthlyInvoices(LocalDate billingMonth) {
        List<Apartment> apartments = apartmentDAO.getAllApartments();
        if (apartments == null || apartments.isEmpty()) {
            return "Không tìm thấy căn hộ nào để tính toán lại hóa đơn.";
        }
        int successCount = 0;
        int notFoundCount = 0;
        int errorCount = 0;
        System.out.println("Bắt đầu TÍNH TOÁN LẠI hóa đơn cho tháng: " + billingMonth + " cho " + apartments.size() + " căn hộ.");
        for (Apartment apartment : apartments) {
            try {
                Integer existingInvoiceId = invoiceDAO.findInvoiceIdByApartmentAndMonth(apartment.getApartmentId(), billingMonth);
                if (existingInvoiceId == null) {
                    System.out.println("Bỏ qua căn hộ " + apartment.getApartmentId() + ": Không tìm thấy hóa đơn tháng " + billingMonth.getMonthValue() + " để tính lại.");
                    notFoundCount++;
                    continue;
                }
                System.out.println("Đang tính toán lại HĐ #" + existingInvoiceId + " cho căn hộ " + apartment.getApartmentId() + "...");
                boolean deleted = invoiceDAO.deleteInvoiceDetails(existingInvoiceId);
                if (!deleted) {
                    System.err.println("Lỗi: Không thể xóa chi tiết cũ của HĐ #" + existingInvoiceId);
                    errorCount++;
                    continue;
                }
                System.out.println(" -> Đã xóa chi tiết cũ.");
                List<FeeType> defaultFees = feeTypeDAO.getAllDefaultFees();
                List<FeeType> optionalFees = feeTypeDAO.getOptionalFeesForApartment(apartment.getApartmentId());
                List<FeeType> allFeesToBill = Stream.concat(
                        defaultFees != null ? defaultFees.stream() : Stream.empty(),
                        optionalFees != null ? optionalFees.stream() : Stream.empty()
                ).toList();
                BigDecimal totalAmount = BigDecimal.ZERO;
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