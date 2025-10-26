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
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class AdminDashboardController {

    // Khai báo các thành phần FXML của Dashboard (Admin Dashboard FXML)
    @FXML private Button btnThemCuDan;
    @FXML private Label lblWelcome;
    @FXML private Button btnQuanLyTaiKhoan;
    @FXML private Button btnQuanLyHoaDon;
    @FXML private Button btnTaoThongBao;
    @FXML private Button btnXemYeuCauDichVu;
    @FXML private Button btnXemDanhSachCuDan;
    @FXML private Button btnLogout;

    /**
     * Phương thức được gọi tự động sau khi FXML được tải.
     * Dùng để khởi tạo giao diện, kiểm tra quyền VÀ BỔ SUNG LISTENER.
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

            // TODO: (Nếu cần) Thêm logic kiểm tra quyền cho các nút khác ở đây
        }
    }

    /**
     * Xử lý sự kiện khi Ban Quản Trị nhấn nút "Thêm Cư Dân".
     * LƯU Ý: Đã bỏ @FXML vì nó được gọi từ Listener trong initialize().
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
    // PHƯƠNG THỨC MẪU (STUBS) AN TOÀN CHO CÁC NÚT CHƯA LÀM
    // LƯU Ý: Đã bỏ @FXML khỏi tất cả các hàm này
    // ====================================================================

    /**
     * Xử lý sự kiện khi nhấn nút "Quản Lý Tài Khoản" (Mở màn hình UserManagement).
     */
    private void handleQuanLyTaiKhoan() {
        try {
            // *** ĐẢM BẢO TÊN FILE FXML NÀY ĐÚNG VỚI TÊN FILE CỦA BẠN (ví dụ: UserManagement.fxml) ***
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/UserManagement.fxml"));
            Parent root = loader.load();

            Stage quanLyStage = new Stage();
            quanLyStage.setTitle("Quản Lý Hồ Sơ Cư Dân");
            quanLyStage.initModality(Modality.WINDOW_MODAL);

            // Thiết lập cửa sổ cha là chủ sở hữu
            Stage currentStage = (Stage) btnQuanLyTaiKhoan.getScene().getWindow();
            quanLyStage.initOwner(currentStage);

            quanLyStage.setScene(new Scene(root));
            quanLyStage.show(); // Dùng show() để cửa sổ Dashboard vẫn mở

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao diện", "Không thể tải màn hình Quản lý Tài khoản. Vui lòng kiểm tra file FXML.");
        }
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
     * Xử lý sự kiện khi Ban Quản Trị nhấn nút "Xem Danh Sách Cư Dân".
     * Mở cửa sổ resident_list.fxml.
     */
    @FXML
    private void handleOpenResidentList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/resident_list.fxml"));
            Parent root = loader.load();

            Stage residentListStage = new Stage();
            residentListStage.setTitle("Quản lý cư dân - Hệ thống quản lý tòa nhà");
            residentListStage.initModality(Modality.WINDOW_MODAL);

            // Thiết lập cửa sổ cha là chủ sở hữu
            Stage currentStage = (Stage) btnXemDanhSachCuDan.getScene().getWindow();
            residentListStage.initOwner(currentStage);

            residentListStage.setScene(new Scene(root, 1000, 700));
            residentListStage.setResizable(true);
            residentListStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao diện", "Không thể tải màn hình danh sách cư dân. Vui lòng kiểm tra lại file FXML.");
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút "Đăng xuất".
     */
    @FXML
    private void handleLogout() {
        try {
            // Xóa session
            SessionManager.getInstance().logout();
            
            // Đóng cửa sổ hiện tại
            Stage currentStage = (Stage) btnLogout.getScene().getWindow();
            currentStage.close();
            
            // Mở lại màn hình login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/login.fxml"));
            Parent root = loader.load();
            
            Stage loginStage = new Stage();
            loginStage.setTitle("Quản lý Tòa nhà - Đăng nhập");
            loginStage.setScene(new Scene(root, 400, 300));
            loginStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể quay lại màn hình đăng nhập.");
        }
    }

    // Phương thức tiện ích (Đảm bảo chỉ có một hàm này ở đây)
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}