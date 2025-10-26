// Vị trí: src/main/java/com/example/quanlytoanha/service/NotificationService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.InvoiceDAO;
import com.example.quanlytoanha.dao.NotificationDAO;
import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.Notification;
import com.example.quanlytoanha.model.Role; // Đảm bảo bạn có Enum/Class Role này
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager; // Đảm bảo đúng đường dẫn

import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
        this.invoiceDAO = new InvoiceDAO();
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

    // --- (Có thể thêm các hàm khác nếu cần, ví dụ: lấy tất cả thông báo, xóa thông báo...) ---
}