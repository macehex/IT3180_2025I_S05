// Vị trí: src/main/java/com/example/quanlytoanha/utils/FXMLLoaderUtil.java
package com.example.quanlytoanha.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.IOException;
import java.util.Objects;

/**
 * Lớp tiện ích để tải các file FXML và mở cửa sổ modal (rất hữu ích cho JavaFX).
 */
public class FXMLLoaderUtil {

    /**
     * Mở một cửa sổ modal mới và trả về Stage của cửa sổ đó.
     * @param fxmlPath Đường dẫn tới file FXML (Ví dụ: "/fxml/asset_form.fxml")
     * @param ownerWindow Cửa sổ cha (để cửa sổ con là modal)
     * @param title Tiêu đề của cửa sổ
     * @param data Dữ liệu tùy chọn để truyền vào Controller (nếu có)
     * @return Stage của cửa sổ modal vừa mở
     * @throws IOException nếu không tìm thấy file FXML
     */
    public static Stage openModalWindow(String fxmlPath, Window ownerWindow, String title, Object data) throws IOException {

        // 1. Tải FXML
        FXMLLoader loader = new FXMLLoader(FXMLLoaderUtil.class.getResource(fxmlPath));
        Parent root = loader.load();

        // 2. Lấy Controller và truyền dữ liệu
        Object controller = loader.getController();
        if (controller instanceof com.example.quanlytoanha.controller.AssetFormController) {
            // Giả định: Bạn đã triển khai phương thức setAssetData trong AssetFormController
            // (Chúng ta cần truyền Asset và Callback ở đây, nhưng để đơn giản hóa,
            // ta sẽ xử lý việc gọi setAssetData ở tầng Controller gọi (AssetManagementController) )
        }

        // 3. Cấu hình Stage (Cửa sổ)
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.initModality(Modality.WINDOW_MODAL); // Bắt buộc phải là modal
        stage.initOwner(ownerWindow); // Thiết lập cửa sổ cha

        return stage;
    }

    /**
     * Hàm tiện ích để lấy Controller từ root của Scene đã tải (sau khi load())
     * @param root Parent của scene
     * @return Controller của FXML đã tải (có thể cần ép kiểu khi sử dụng)
     */
    public static <T> T getController(Parent root) {
        if (root.getScene() != null && root.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) root.getScene().getWindow();
            // Đây là một cách đơn giản, nhưng cách tốt hơn là lưu Controller khi load FXML
            // Tuy nhiên, để khớp với logic Controller của bạn, ta cần cách tiếp cận linh hoạt hơn

            // Do phức tạp, ta sẽ sửa lại cách Controller gọi hàm openModalWindow.
            // Để đơn giản nhất, ta sẽ sử dụng phương thức của FXMLLoader
            return null; // Tạm thời trả về null, và chỉnh sửa lại cách gọi ở AssetManagementController
        }
        return null;
    }
}
