package com.example.quanlytoanha;

// Vị trí: src/main/java/com/example/quanlytoanha/TestLogin.java

import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.service.AuthService;
import com.example.quanlytoanha.session.SessionManager; // (Thêm cái này để test luôn Session)

/**
 * Lớp này chỉ dùng để chạy thử (test) logic đăng nhập
 * mà không cần giao diện đồ họa (GUI).
 */
public class TestLogin {

    public static void main(String[] args) {
        // --- Chú ý quan trọng! ---
        // Đảm bảo bạn đã có một user trong database với mật khẩu ĐÃ ĐƯỢC BĂM (hashed)
        // Ví dụ: user: 'admin', pass: '123456'
        // Bạn phải chạy PasswordUtil.hashPassword("123456") -> "$2a$10$N9qo8u..."
        // Và lưu cái chuỗi "$2a$10$N9qo8u..." đó vào cột password của 'admin'

        // 1. Khởi tạo AuthService
        AuthService authService = new AuthService();

        // 2. Định nghĩa thông tin đăng nhập để test
        String usernameToTest = "admin"; // (Thay bằng username bạn có trong DB)
        String passwordToTest = "admin123"; // (Đây là mật khẩu thô)

        System.out.println("Đang thử đăng nhập với user: " + usernameToTest);

        // 3. Gọi phương thức login
        User user = authService.login(usernameToTest, passwordToTest);

        // 4. Kiểm tra kết quả
        if (user != null) {
            System.out.println("--- ĐĂNG NHẬP THÀNH CÔNG! ---");
            System.out.println("Xin chào, " + user.getFullName());
            System.out.println("Vai trò: " + user.getRole().getRoleName());

            /*
            // 4a. Kiểm tra xem quyền (permissions) đã được nạp đúng chưa
            System.out.println("Kiểm tra quyền 'MANAGE_USERS': " + user.hasPermission("MANAGE_USERS"));
            System.out.println("Kiểm tra quyền 'CREATE_INVOICE': " + user.hasPermission("CREATE_INVOICE"));
            System.out.println("Danh sách quyền: " + user.getPermissions());
            */
            // 4b. Test luôn SessionManager
            SessionManager.getInstance().login(user);
            System.out.println("Đã lưu user vào Session.");
            System.out.println("User trong session là: " + SessionManager.getInstance().getCurrentUser().getUsername());


        } else {
            System.out.println("--- ĐĂNG NHẬP THẤT BẠI ---");
            System.out.println("Kiểm tra lại username hoặc mật khẩu.");
        }
    }
}