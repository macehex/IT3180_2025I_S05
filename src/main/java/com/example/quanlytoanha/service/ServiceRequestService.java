package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.ServiceRequestDAO;
import com.example.quanlytoanha.model.ServiceRequest;

import java.io.File; // Sẽ dùng để nhận file ảnh từ JavaFX
import java.sql.Date;
import java.util.List;

public class ServiceRequestService {

    private ServiceRequestDAO serviceRequestDAO;
    private FileStorageService fileStorageService; // Service để xử lý lưu file

    // Constructor: Khi Service được tạo, nó cũng tạo ra các DAO/Service cần thiết
    public ServiceRequestService() {
        this.serviceRequestDAO = new ServiceRequestDAO();
        // Giả sử bạn cũng có một FileStorageService để xử lý logic lưu file
        this.fileStorageService = new FileStorageService();
    }

    /**
     * CƯ DÂN (US3_1_1): Xử lý logic tạo một yêu cầu mới.
     * Đây là hàm mà JavaFX Controller sẽ gọi.
     *
     * @param reqUserId Id của người dùng đang đăng nhập
     * @param reqType Loại yêu cầu (ví dụ: 'SU_CO' hoặc 'PHAN_ANH')
     * @param reqTitle Tiêu đề
     * @param description Mô tả chi tiết
     * @param location Vị trí (từ User Story)
     * @param assetId ID tài sản liên quan (có thể là null)
     * @param imageFile File ảnh (có thể là null)
     */
    public void createNewRequest(int reqUserId, String reqType, String reqTitle,
                                 String description, String location, Integer assetId,
                                 File imageFile) {

        // --- 1. Xử lý các quy tắc nghiệp vụ ---

        // LƯU Ý QUAN TRỌNG (1): Xử lý Vị trí
        // Schema database 'service_requests' của bạn không có cột 'location'.
        // User Story lại có "vị trí".
        // Giải pháp: Tạm thời chúng ta sẽ nối 'location' vào cuối 'description'.
        String fullDescription = description;
        if (location != null && !location.isEmpty()) {
            fullDescription += "\n\n--- Vị trí báo cáo: " + location;
        }

        // LƯU Ý QUAN TRỌNG (2): Xử lý Ảnh
        // Schema database của bạn cũng không có cột 'image_url'.
        // User Story lại có "hình ảnh".
        // Tạm thời, chúng ta sẽ lưu file ảnh nhưng không lưu đường dẫn vào DB.
        // Khi bạn thêm cột 'image_url' (varchar) vào DB, bạn có thể gán giá trị 'imageUrl' này.
        String imageUrl = null;
        if (imageFile != null) {
            imageUrl = fileStorageService.saveImage(imageFile); // Hàm này sẽ lưu file và trả về đường dẫn
        }

        // --- 2. Chuẩn bị đối tượng Model ---
        ServiceRequest request = new ServiceRequest();
        request.setReqUserId(reqUserId);
        request.setReqType(reqType);
        request.setReqTitle(reqTitle);
        request.setDescription(fullDescription); // Dùng mô tả đã bao gồm vị trí
        request.setAssetId(assetId);

        // --- 3. Áp dụng các giá trị mặc định (Quy tắc nghiệp vụ) ---
        request.setStatus("PENDING"); // Luôn là 'PENDING' khi mới tạo
        request.setCreatedAt(new Date(System.currentTimeMillis())); // Đặt ngày tạo là hôm nay

        // (Nếu bạn đã thêm cột 'image_url'):
        // request.setImageUrl(imageUrl);

        // --- 4. Gọi DAO để lưu vào database ---
        try {
            serviceRequestDAO.createServiceRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            // Xử lý lỗi (ví dụ: thông báo cho người dùng)
        }
    }

    /**
     * CƯ DÂN (US3_1_1): Lấy danh sách các yêu cầu của một người dùng.
     * @param userId ID của người dùng
     * @return Danh sách yêu cầu
     */
    public List<ServiceRequest> getRequestsForUser(int userId) {
        // Hàm này đơn giản là gọi thẳng qua DAO, vì không có nghiệp vụ phức tạp
        return serviceRequestDAO.getRequestsByUserId(userId);
    }

    // (Các hàm khác cho Ban Quản lý, v.v. sẽ ở đây)
}
