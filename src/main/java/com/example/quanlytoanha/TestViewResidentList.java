package com.example.quanlytoanha;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Lớp này chỉ dùng để chạy thử (test) giao diện DashboardView.fxml
 * mà không cần phải chạy file Main.java và đăng nhập.
 */
public class TestViewResidentList extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        // QUAN TRỌNG: Đường dẫn đến file FXML của bạn.
        // Giả sử file FXML nằm trong: src/main/resources/com/example/quanlytoanha/view/DashboardView.fxml
        // (Đây là cấu trúc thư mục Maven/Gradle tiêu chuẩn)

        String fxmlPath = "view/view_resident_list.fxml"; // Đường dẫn tương đối
        URL fxmlUrl = getClass().getResource(fxmlPath);

        if (fxmlUrl == null) {
            System.err.println("--- LỖI ---");
            System.err.println("Không tìm thấy file FXML tại đường dẫn: " + fxmlPath);
            System.err.println("Hãy kiểm tra lại cấu trúc thư mục 'resources' của bạn.");
            return;
        }

        // Tải FXML.
        // FXMLLoader sẽ tự động tìm và khởi tạo DashboardController
        // do đã khai báo 'fx:controller' trong file FXML.
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);

        // Kích thước cửa sổ này khớp với prefWidth/prefHeight trong FXML
        // (Bạn có thể điều chỉnh nếu muốn)
        Scene scene = new Scene(fxmlLoader.load(), 400, 800);

        stage.setTitle("Kiểm thử - Danh sách Cư dân");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
