// Vị trí: src/main/java/com/example/quanlytoanha/controller/AdminDashboardController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType; // Import còn thiếu từ branch 'feature/view-filter'
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class AdminDashboardController {

    // --- LẤY TỪ BRANCH 'topic/login-logout' (Vì FXML phức tạp hơn) ---
    @FXML private Button btnThemCuDan;
    @FXML private Label lblWelcome;
    @FXML private Button btnQuanLyTaiKhoan;
    @FXML private Button btnQuanLyHoaDon;
    @FXML private Button btnTaoThongBao;
    @FXML private Button btnXemYeuCauDichVu;
    @FXML private Button btnLogout;
    
    /**
     * Phương thức được gọi tự động sau khi FXML được tải.
     * GIỮ PHIÊN BẢN TỪ 'topic/login-logout' VÌ CÓ KIỂM TRA QUYỀN
     */
    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            lblWelcome.setText("Xin chào, " + currentUser.getFullName() + " (Ban Quản Trị)");

            // Ẩn/hiện nút "Thêm Cư Dân" dựa trên quyền 'CREATE_RESIDENT'
            if (btnThemCuDan != null) {
                boolean hasPermission = currentUser.hasPermission("CREATE_RESIDENT");
                btnThemCuDan.setVisible(hasPermission);
                btnThemCuDan.setManaged(hasPermission);

                // --- BỔ SUNG EXPLICIT LISTENER (setOnAction) CHO NÚT CÓ QUYỀN ---
                if (hasPermission) {
                    // Dùng lambda expression để gọi hàm xử lý khi nút được nhấn
                    btnThemCuDan.setOnAction(event -> handleOpenAddResidentForm());
                }
            }

            // --- BỔ SUNG LISTENER CHO CÁC NÚT KHÁC (Chưa làm) ---
            if (btnQuanLyTaiKhoan != null) {
                btnQuanLyTaiKhoan.setOnAction(event -> handleQuanLyTaiKhoan());
            }
            if (btnQuanLyHoaDon != null) {
                btnQuanLyHoaDon.setOnAction(event -> handleQuanLyHoaDon());
            }
            if (btnTaoThongBao != null) {
                btnTaoThongBao.setOnAction(event -> handleTaoThongBao());
            }
            if (btnXemYeuCauDichVu != null) {
                btnXemYeuCauDichVu.setOnAction(event -> handleXemYeuCauDichVu());
            }

        }
    }

    /**
     * GIỮ PHIÊN BẢN TỪ 'topic/login-logout'
     */
    private void handleOpenAddResidentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/add_resident_form.fxml"));
            Parent root = loader.load();

            Stage addResidentStage = new Stage();
            addResidentStage.setTitle("Tạo Hồ Sơ Cư Dân Mới");
            addResidentStage.initModality(Modality.WINDOW_MODAL);

            // Thiết lập cửa sổ cha là chủ sở hữu
            Stage currentStage = (Stage) btnThemCuDan.getScene().getWindow();
            addResidentStage.initOwner(currentStage);

            addResidentStage.setScene(new Scene(root));
            addResidentStage.showAndWait();

            // TODO: refreshResidentTable(); // Logic làm mới bảng sau khi cửa sổ con đóng
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao diện", "Không thể tải form tạo cư dân. Vui lòng kiểm tra lại file FXML.");
        }
    }

    // ====================================================================
    // --- PHẦN LOGOUT: GIỮ PHIÊN BẢN TỪ 'feature/view-filter' VÌ TỐT HƠN ---
    // ====================================================================

    /**
     * Xử lý đăng xuất.
     * (LẤY TỪ 'feature/view-filter' VÌ CÓ HỘP THOẠI XÁC NHẬN VÀ TÁI SỬ DỤNG STAGE)
     */
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            SessionManager.getInstance().logout();
            
            try {
                // Quay về màn hình đăng nhập
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/login.fxml"));
                Parent root = loader.load();
                
                // Tái sử dụng Stage hiện tại (tốt hơn là đóng/mở)
                Stage stage = (Stage) btnLogout.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Đăng nhập - Hệ thống quản lý tòa nhà");
                
            } catch (IOException e) {
                e.printStackTrace();
                // Sửa lại cú pháp gọi showAlert cho đúng với phiên bản của 'topic/login-logout'
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể quay về màn hình đăng nhập!");
            }
        }
    }

    // ====================================================================
    // --- CÁC PHƯƠNG THỨC MẪU (STUBS) TỪ 'topic/login-logout' ---
    // ====================================================================

    private void handleQuanLyTaiKhoan() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Quản lý Tài khoản chưa được triển khai.");
    }

    private void handleQuanLyHoaDon() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Quản lý Hóa đơn chưa được triển khai.");
    }

    private void handleTaoThongBao() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Tạo Thông báo chưa được triển khai.");
    }

    private void handleXemYeuCauDichVu() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Xem Yêu cầu Dịch vụ chưa được triển khai.");
    }

    /**
     * Hiển thị thông báo.
     * GIỮ PHIÊN BẢN TỪ 'topic/login-logout'
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}