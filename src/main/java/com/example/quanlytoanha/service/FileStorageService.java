package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.ServiceRequestDAO;
import com.example.quanlytoanha.model.ServiceRequest;

import java.io.File; // Sẽ dùng để nhận file ảnh từ JavaFX
import java.sql.Date;
import java.util.List;

class FileStorageService {
    /**
     * Lưu file ảnh vào một thư mục cố định và trả về đường dẫn.
     * @param file File nhận từ JavaFX FileChooser
     * @return Đường dẫn tương đối (ví dụ: "uploads/images/ten_file.jpg")
     */
    public String saveImage(File file) {
        // Logic (ví dụ):
        // 1. Tạo một tên file duy nhất (dùng UUID hoặc timestamp).
        // 2. Định nghĩa một thư mục lưu trữ (ví dụ: "uploads/images/").
        // 3. Sao chép file từ 'file.getPath()' đến đường dẫn mới.
        // 4. Trả về đường dẫn mới đó.

        System.out.println("Đang lưu file: " + file.getName());
        String savedPath = "uploads/images/" + System.currentTimeMillis() + "_" + file.getName();
        // ... (Viết code copy file ở đây) ...
        return savedPath; // Trả về đường dẫn để (sau này) lưu vào DB
    }
}
