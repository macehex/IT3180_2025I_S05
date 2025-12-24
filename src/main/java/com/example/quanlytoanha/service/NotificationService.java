// Vị trí: src/main/java/com/example/quanlytoanha/service/NotificationService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.InvoiceDAO;
import com.example.quanlytoanha.dao.NotificationDAO;
import com.example.quanlytoanha.model.*;
import com.example.quanlytoanha.session.SessionManager; // Đảm bảo đúng đường dẫn

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime; // Dùng LocalDateTime cho log time chính xác hơn
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private final InvoiceDAO invoiceDAO;
    private final NotificationDAO notificationDAO;
    private final SimpleDateFormat dateFormat;
    private final DateTimeFormatter logTimestampFormat;

    public NotificationService() {
        this.invoiceDAO = InvoiceDAO.getInstance();
        this.notificationDAO = new NotificationDAO();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        this.logTimestampFormat = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"); // Định dạng cho log
    }

    /**
     * Kiểm tra quyền: Chỉ Admin/Kế toán mới được chạy chức năng gửi thông báo.
     * @throws SecurityException Nếu không có quyền.
     */
    private void checkPermission() throws SecurityException {
        User currentUser = SessionManager.getInstance().getCurrentUser(); // Lấy user từ session
        // Kiểm tra xem user có tồn tại và có đúng vai trò không
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.ACCOUNTANT)) {
            throw new SecurityException("Chỉ Quản trị viên hoặc Kế toán mới có quyền thực hiện thao tác này.");
        }
    }

    /**
     * Gửi thông báo nhắc nợ RIÊNG cho các hóa đơn sắp đến hạn.
     * @param daysBefore Số ngày báo trước hạn.
     * @return Danh sách các log message ghi lại hoạt động gửi.
     * @throws SQLException Nếu có lỗi khi truy vấn database.
     * @throws SecurityException Nếu người dùng không có quyền.
     */
    public List<String> sendUpcomingReminders(int daysBefore) throws SQLException, SecurityException {
        checkPermission(); // Kiểm tra quyền trước khi thực hiện
        List<String> logMessages = new ArrayList<>();
        // Lấy danh sách hóa đơn sắp đến hạn (bao gồm owner_id)
        List<Invoice> upcomingInvoices = invoiceDAO.findUpcomingDueInvoices(daysBefore);

        for (Invoice invoice : upcomingInvoices) {
            int recipientUserId = invoice.getOwnerId(); // Lấy ID của chủ căn hộ

            // Tạo tiêu đề và nội dung thông báo
            String title = String.format("[Nhắc nợ] Hóa đơn #%d sắp đến hạn (%s)",
                    invoice.getInvoiceId(), dateFormat.format(invoice.getDueDate()));
            String message = String.format("Hóa đơn #%d của căn hộ %d sắp đến hạn thanh toán vào ngày %s. Tổng số tiền: %,.0f VNĐ. Vui lòng thanh toán sớm.",
                    invoice.getInvoiceId(), invoice.getApartmentId(), dateFormat.format(invoice.getDueDate()), invoice.getTotalAmount());

            // Tạo đối tượng Notification để lưu vào DB
            Notification notification = new Notification(recipientUserId, title, message, invoice.getInvoiceId());

            // Gọi DAO để tạo thông báo trong database
            if (notificationDAO.createNotification(notification)) {
                // Ghi log lại hoạt động
                logMessages.add(String.format("[%s] Đã gửi nhắc nợ HĐ #%d cho User ID %d (Căn hộ %d)",
                        LocalDateTime.now().format(logTimestampFormat), // Thời gian hiện tại
                        invoice.getInvoiceId(),
                        recipientUserId,
                        invoice.getApartmentId()));
            } else {
                // Ghi log lỗi (nếu cần)
                logMessages.add(String.format("[%s] LỖI gửi nhắc nợ HĐ #%d cho User ID %d",
                        LocalDateTime.now().format(logTimestampFormat),
                        invoice.getInvoiceId(),
                        recipientUserId));
            }
        }
        return logMessages; // Trả về danh sách log
    }

    /**
     * Gửi thông báo nợ quá hạn RIÊNG.
     * @return Danh sách các log message ghi lại hoạt động gửi.
     * @throws SQLException Nếu có lỗi khi truy vấn database.
     * @throws SecurityException Nếu người dùng không có quyền.
     */
    public List<String> sendOverdueNotifications() throws SQLException, SecurityException {
        checkPermission(); // Kiểm tra quyền
        List<String> logMessages = new ArrayList<>();
        // Lấy danh sách hóa đơn quá hạn (bao gồm owner_id)
        List<Invoice> overdueInvoices = invoiceDAO.findOverdueInvoices();

        for (Invoice invoice : overdueInvoices) {
            int recipientUserId = invoice.getOwnerId(); // ID chủ căn hộ

            // Tạo tiêu đề và nội dung thông báo
            String title = String.format("[Nợ quá hạn] Hóa đơn #%d đã quá hạn (%s)",
                    invoice.getInvoiceId(), dateFormat.format(invoice.getDueDate()));
            String message = String.format("!!! NỢ QUÁ HẠN !!! Hóa đơn #%d của căn hộ %d đã quá hạn thanh toán (Hạn: %s). Tổng tiền: %,.0f VNĐ. Vui lòng thanh toán NGAY!",
                    invoice.getInvoiceId(), invoice.getApartmentId(), dateFormat.format(invoice.getDueDate()), invoice.getTotalAmount());

            // Tạo đối tượng Notification
            Notification notification = new Notification(recipientUserId, title, message, invoice.getInvoiceId());

            // Gọi DAO để lưu thông báo
            if (notificationDAO.createNotification(notification)) {
                // Ghi log thành công
                logMessages.add(String.format("[%s] Đã gửi thông báo quá hạn HĐ #%d cho User ID %d (Căn hộ %d)",
                        LocalDateTime.now().format(logTimestampFormat),
                        invoice.getInvoiceId(),
                        recipientUserId,
                        invoice.getApartmentId()));
            } else {
                // Ghi log lỗi
                logMessages.add(String.format("[%s] LỖI gửi thông báo quá hạn HĐ #%d cho User ID %d",
                        LocalDateTime.now().format(logTimestampFormat),
                        invoice.getInvoiceId(),
                        recipientUserId));
            }
        }
        return logMessages; // Trả về danh sách log
    }

    /**
     * Lấy danh sách thông báo CHƯA ĐỌC cho người dùng hiện tại đang đăng nhập.
     * Dùng cho Cư dân xem thông báo của mình.
     * @return Danh sách các Notification chưa đọc.
     * @throws SQLException Nếu có lỗi database.
     */
    public List<Notification> getMyUnreadNotifications() throws SQLException {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        // Nếu chưa đăng nhập, trả về danh sách rỗng
        if (currentUser == null) {
            return new ArrayList<>();
        }
        // Gọi DAO để lấy thông báo của user hiện tại
        return notificationDAO.getUnreadNotificationsForUser(currentUser.getUserId());
    }

    /**
     * Đánh dấu một thông báo là đã đọc.
     * @param notificationId ID của thông báo cần đánh dấu.
     * @return true nếu đánh dấu thành công, false nếu không.
     * @throws SQLException Nếu có lỗi database.
     */
    public boolean markNotificationAsRead(int notificationId) throws SQLException {
        // (Có thể thêm kiểm tra xem user hiện tại có phải là người nhận thông báo này không)
        return notificationDAO.markAsRead(notificationId);
    }

    // --- HÀM MỚI ĐÃ THÊM VÀO ---
    /**
     * Gửi một thông báo nhắc nợ riêng lẻ.
     * @param userId ID của người nhận (chủ căn hộ).
     * @param debtInfo Thông tin công nợ để tạo nội dung.
     * @return true nếu gửi thành công, false nếu không.
     * @throws SQLException Nếu có lỗi DB.
     * @throws SecurityException Nếu không có quyền.
     */
    public boolean sendSingleReminder(int userId, ApartmentDebt debtInfo) throws SQLException, SecurityException {
        checkPermission(); // Vẫn kiểm tra quyền Kế toán/Admin

        // Tạo tiêu đề và nội dung thông báo dựa trên debtInfo
        String title = String.format("[Nhắc nợ] Vui lòng thanh toán hóa đơn căn hộ %d", debtInfo.getApartmentId());

        // --- SỬA LẠI HÀM FORMAT NÀY ---
        String message = String.format(
                "Căn hộ %d (%s) hiện đang có %d hóa đơn chưa thanh toán với tổng số tiền là %,.0f VNĐ. Hạn thanh toán sớm nhất là %s. Vui lòng thanh toán sớm.",
                debtInfo.getApartmentId(),     // Tham số cho %d đầu tiên
                debtInfo.getOwnerName(),       // Tham số cho %s đầu tiên
                debtInfo.getUnpaidCount(),     // Tham số cho %d thứ hai
                debtInfo.getTotalDue(),        // Tham số cho %,.0f (BigDecimal vẫn dùng được với %f)
                (debtInfo.getEarliestDueDate() != null ? dateFormat.format(debtInfo.getEarliestDueDate()) : "N/A") // Tham số cho %s cuối cùng
        );
        // -----------------------------

        // Tạo đối tượng Notification (không cần relatedInvoiceId cho nhắc nợ chung)
        Notification notification = new Notification(userId, title, message, null);

        // Gọi DAO để tạo thông báo
        boolean success = notificationDAO.createNotification(notification);

        // Ghi log (tùy chọn)
        if (success) {
            System.out.printf("[%s] Đã gửi nhắc nợ riêng cho User ID %d (Căn hộ %d)\n",
                    LocalDateTime.now().format(logTimestampFormat), userId, debtInfo.getApartmentId());
        } else {
            System.err.printf("[%s] LỖI gửi nhắc nợ riêng cho User ID %d\n",
                    LocalDateTime.now().format(logTimestampFormat), userId);
        }

        return success;
    }

    /**
     * HÀM MỚI: Gửi thông báo khi có hóa đơn mới được tạo.
     * Hàm này được gọi bởi InvoiceGenerationService.
     * @param userId ID của chủ căn hộ nhận thông báo.
     * @param newInvoice Đối tượng Invoice vừa được tạo.
     * @return true nếu gửi thành công, false nếu không.
     */
    public boolean sendNewInvoiceNotification(int userId, Invoice newInvoice) {
        // Không cần checkPermission() vì hàm này được gọi từ logic hệ thống (tạo hóa đơn)
        // chứ không phải trực tiếp từ UI Kế toán.

        // Tạo tiêu đề và nội dung
        String title = String.format("Hóa đơn mới #%d đã được phát hành", newInvoice.getInvoiceId());
        String message = String.format("Hóa đơn mới #%d cho căn hộ %d đã được phát hành với tổng số tiền là %,.0f VNĐ. Hạn thanh toán là %s. Vui lòng kiểm tra và thanh toán.",
                newInvoice.getInvoiceId(),
                newInvoice.getApartmentId(), // Cần đảm bảo Invoice có getApartmentId()
                newInvoice.getTotalAmount(),
                dateFormat.format(newInvoice.getDueDate())
        );

        // Tạo đối tượng Notification
        Notification notification = new Notification(userId, title, message, newInvoice.getInvoiceId());

        try {
            boolean success = notificationDAO.createNotification(notification);
            if (success) {
                System.out.printf("[%s] Đã gửi thông báo HĐ MỚI #%d cho User ID %d\n",
                        LocalDateTime.now().format(logTimestampFormat), newInvoice.getInvoiceId(), userId);
            } else {
                System.err.printf("[%s] LỖI gửi thông báo HĐ MỚI #%d cho User ID %d\n",
                        LocalDateTime.now().format(logTimestampFormat), newInvoice.getInvoiceId(), userId);
            }
            return success;
        } catch (SQLException e) {
            System.err.printf("[%s] LỖI DB khi gửi thông báo HĐ MỚI #%d cho User ID %d: %s\n",
                    LocalDateTime.now().format(logTimestampFormat), newInvoice.getInvoiceId(), userId, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy lịch sử thông báo đầy đủ cho người dùng hiện tại.
     */
    public List<Notification> getAllMyNotifications() throws SQLException {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }
        // Gọi hàm mới vừa viết bên DAO
        return notificationDAO.getAllNotificationsForUser(currentUser.getUserId());
    }

    /**
     * Gửi thông báo thanh toán thành công cho người dùng.
     * Hàm này được gọi tự động sau khi thanh toán hóa đơn thành công.
     * @param userId ID của người dùng nhận thông báo.
     * @param invoice Đối tượng Invoice đã được thanh toán.
     * @param paidAmount Số tiền đã thanh toán.
     * @return true nếu gửi thành công, false nếu không.
     */
    public boolean sendPaymentSuccessNotification(int userId, Invoice invoice, BigDecimal paidAmount) {
        // Không cần checkPermission() vì hàm này được gọi tự động từ logic hệ thống
        
        // Tạo tiêu đề và nội dung thông báo
        String title = String.format("Thanh toan thanh cong hoa don #%d", invoice.getInvoiceId());
        String message = String.format(
            "Ban da thanh toan thanh cong hoa don #%d cua can ho %d voi so tien %,.0f VND. Cam on ban da thanh toan dung han.",
            invoice.getInvoiceId(),
            invoice.getApartmentId(),
            paidAmount
        );
        
        // Tạo đối tượng Notification
        Notification notification = new Notification(userId, title, message, invoice.getInvoiceId());
        
        try {
            boolean success = notificationDAO.createNotification(notification);
            if (success) {
                System.out.printf("[%s] Da gui thong bao thanh toan thanh cong HD #%d cho User ID %d\n",
                        LocalDateTime.now().format(logTimestampFormat), invoice.getInvoiceId(), userId);
            } else {
                System.err.printf("[%s] LOI gui thong bao thanh toan thanh cong HD #%d cho User ID %d\n",
                        LocalDateTime.now().format(logTimestampFormat), invoice.getInvoiceId(), userId);
            }
            return success;
        } catch (SQLException e) {
            System.err.printf("[%s] LOI DB khi gui thong bao thanh toan thanh cong HD #%d cho User ID %d: %s\n",
                    LocalDateTime.now().format(logTimestampFormat), invoice.getInvoiceId(), userId, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HÀM MỚI: Gửi thông báo cập nhật Chiến dịch đóng góp
     */
    public void sendCampaignUpdateNotification(int userId, String campaignName, LocalDate newDueDate) {
        String title = "🔔 Cập nhật: " + campaignName;
        String content = "Ban quản lý đã cập nhật thông tin đợt đóng góp '" + campaignName + "'.\n" +
                "Hạn đóng góp mới là: " + newDueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".\n" +
                "Trân trọng thông báo.";

        // Tạo notification và lưu DB
        Notification noti = new Notification(userId, title, content, null);
        try {
            notificationDAO.createNotification(noti);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * HÀM MỚI: Gửi thông báo KẾT THÚC chiến dịch đóng góp và CẢM ƠN.
     */
    public void sendCampaignEndedNotification(int userId, String campaignName) {
        String title = "🔔 Kết thúc đợt vận động: " + campaignName;

        // Nội dung cảm ơn chân thành
        String message = String.format(
                "Ban Quản lý trân trọng thông báo: Đợt vận động đóng góp \"%s\" đã chính thức kết thúc.\n\n" +
                        "Chúng tôi xin gửi lời cảm ơn chân thành đến Quý cư dân đã nhiệt tình hưởng ứng và đóng góp. " +
                        "Sự chung tay của Quý vị là nguồn động viên to lớn cho cộng đồng.\n\n" +
                        "Trân trọng cảm ơn!",
                campaignName
        );

        Notification notification = new Notification(userId, title, message, null); // null vì không cần link tới hóa đơn cụ thể

        try {
            notificationDAO.createNotification(notification);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Lỗi gửi thông báo cảm ơn tới User ID: " + userId);
        }
    }
}

