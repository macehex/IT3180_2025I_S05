package com.example.quanlytoanha.utils;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Lớp tiện ích để xử lý việc băm và kiểm tra mật khẩu
 * sử dụng thuật toán BCrypt.
 */
public class PasswordUtil {

    // 1. Tạo một đối tượng BCryptPasswordEncoder
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Băm một mật khẩu dạng văn bản thô (plain text).
     * Bạn sẽ dùng hàm này khi ĐĂNG KÝ user mới hoặc TẠO MỚI mật khẩu.
     *
     * @param plainPassword Mật khẩu thô (ví dụ: "123456")
     * @return Một chuỗi hash dài (ví dụ: "$2a$10$N9qo8uLOickGsS....")
     */
    public static String hashPassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    /**
     * So sánh một mật khẩu thô với một mật khẩu đã được băm (lấy từ DB).
     * Bạn sẽ dùng hàm này khi LOGIN.
     *
     * @param plainPassword Mật khẩu người dùng nhập vào (ví dụ: "123456")
     * @param hashedPassword Mật khẩu đã băm lưu trong DB (ví dụ: "$2a$10$N9qo8uLOickGsS....")
     * @return true nếu mật khẩu khớp, false nếu sai
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }
}