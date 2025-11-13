// Vị trí: src/main/java/com/example/quanlytoanha/service/AssetService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.AssetDAO;
import com.example.quanlytoanha.model.Asset;
import java.math.BigDecimal;
import java.util.List;
import java.sql.SQLException;

public class AssetService {

    private AssetDAO assetDAO = new AssetDAO();

    /**
     * TẠO HOẶC CẬP NHẬT: Xử lý việc lưu trữ và cập nhật thông tin tài sản.
     * Đây là hàm chính để xử lý US2_1_1, đảm bảo logic ghi log trạng thái
     * được gọi khi cần thiết.
     * * @param asset Đối tượng Asset chứa dữ liệu mới từ Form.
     * @param currentUserId ID người dùng (Ban quản trị) đang thao tác.
     * @return true nếu lưu/cập nhật thành công.
     * @throws SQLException nếu có lỗi CSDL hoặc Transaction.
     */
    public boolean saveOrUpdateAsset(Asset asset, int currentUserId) throws SQLException {
        // 1. Xác thực dữ liệu cơ bản (Business Validation)
        if (asset.getAssetType() == null || asset.getAssetType().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên Tài sản không được để trống.");
        }
        if (asset.getLocation() == null || asset.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Vị trí không được để trống.");
        }
        if (asset.getInitialCost() == null || asset.getInitialCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Chi phí ban đầu phải lớn hơn hoặc bằng 0.");
        }
        if (asset.getStatus() == null || asset.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Tình trạng tài sản không được để trống.");
        }


        if (asset.getAssetId() == 0) {
            // --- TRƯỜNG HỢP 1: TẠO MỚI ---
            int newId = assetDAO.createAsset(asset);
            return newId > 0;

        } else {
            // --- TRƯỜNG HỢP 2: CẬP NHẬT ---

            // 2.1. Lấy Asset hiện tại từ DB để so sánh trạng thái
            Asset existingAsset = assetDAO.getAssetById(asset.getAssetId());
            if (existingAsset == null) {
                throw new SQLException("Không tìm thấy Asset ID: " + asset.getAssetId() + " để cập nhật.");
            }

            // 2.2. Cập nhật thông tin chi tiết (trừ status)
            // Lưu ý: updateAsset() trong AssetDAO không thay đổi status, nhưng nó 
            // cập nhật các trường như Type, Cost, Date, Description.
            boolean infoUpdated = assetDAO.updateAsset(asset);

            // 2.3. Logic GHI LOG (Cốt lõi của US2_1_1): Kiểm tra Status có thay đổi không
            boolean statusUpdated = false;
            if (!existingAsset.getStatus().equals(asset.getStatus())) {

                // Nếu trạng thái thay đổi, gọi hàm DAO đã có logic Transaction và Audit Log.
                // Hàm này sẽ đảm bảo status được update VÀ ghi log.
                statusUpdated = assetDAO.updateAssetStatusAndLog(
                        asset.getAssetId(),
                        asset.getStatus(),
                        currentUserId,
                        "Cập nhật trạng thái thủ công qua Form chỉnh sửa chi tiết."
                );
            }

            // Trả về true nếu ít nhất một trong hai hành động (info hoặc status) thành công
            return infoUpdated || statusUpdated;
        }
    }

    // --- CÁC HÀM CƠ BẢN KHÁC ---

    public List<Asset> getAllAssets() {
        return assetDAO.getAllAssets();
    }

    public Asset getAssetById(int assetId) {
        return assetDAO.getAssetById(assetId);
    }

    /**
     * Hàm dành riêng cho các quy trình tự động/bảo trì để thay đổi Status 
     * (Không cần dùng trong Form Edit, nhưng hữu ích cho US2_2_1)
     */
    public boolean updateAssetStatusOnly(int assetId, String newStatus, int currentUserId, String notes) throws SQLException {
        // Đảm bảo chỉ trạng thái được thay đổi
        return assetDAO.updateAssetStatusAndLog(assetId, newStatus, currentUserId, notes);
    }
    /**
     * Lấy số lượng tài sản có vấn đề cho Dashboard.
     */
    public int countTroubleAssets() {
        // Chúng ta có thể thêm logic phức tạp hơn ở đây nếu cần
        return assetDAO.countTroubleAssets();
    }
}