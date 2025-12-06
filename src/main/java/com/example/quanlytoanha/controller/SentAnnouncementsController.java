package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Announcement;
import com.example.quanlytoanha.service.AnnouncementService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;


import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class SentAnnouncementsController implements Initializable {

    @FXML private TableView<Announcement> tableAnnouncements;
    @FXML private TableColumn<Announcement, Integer> colId;
    @FXML private TableColumn<Announcement, String> colTitle;
    @FXML private TableColumn<Announcement, String> colDate; // Để String cho dễ format
    @FXML private TableColumn<Announcement, String> colUrgent;
    @FXML private TableColumn<Announcement, String> colContent;

    private AnnouncementService announcementService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        announcementService = new AnnouncementService();

        setupTableColumns();
        loadData();
        setupDoubleClickHandler();
    }

    // Trong SentAnnouncementsController.java

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("annId"));
        // Căn giữa ID
        colId.setStyle("-fx-alignment: CENTER;");

        colTitle.setCellValueFactory(new PropertyValueFactory<>("annTitle"));

        // Format ngày tháng và căn giữa
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(dateFormat.format(cellData.getValue().getCreatedAt()));
            }
            return new SimpleStringProperty("");
        });
        colDate.setStyle("-fx-alignment: CENTER;");

        // --- PHẦN QUAN TRỌNG: Custom Cell cho Badge ---
        // Chúng ta không dùng PropertyValueFactory mặc định nữa để dễ tùy biến
        colUrgent.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isUrgent() ? "Khẩn cấp" : "Thông thường"));

        colUrgent.setCellFactory(column -> new TableCell<Announcement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Tạo một Label để làm Badge
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge-label"); // Class chung

                    if (item.equals("Khẩn cấp")) {
                        badge.getStyleClass().add("badge-urgent"); // Class đỏ
                        badge.setText("🔥 Khẩn cấp"); // Thêm icon cho sinh động
                    } else {
                        badge.getStyleClass().add("badge-normal"); // Class xanh
                        badge.setText("Thông thường");
                    }

                    // Đặt Label vào trong ô
                    setGraphic(badge);
                    setText(null); // Xóa text gốc của ô
                    setAlignment(Pos.CENTER); // Căn giữa huy hiệu trong ô
                }
            }
        });

        colContent.setCellValueFactory(new PropertyValueFactory<>("content"));
    }

    private void loadData() {
        try {
            // Gọi hàm có sẵn ở backend
            List<Announcement> list = announcementService.getAllAnnouncements();
            ObservableList<Announcement> observableList = FXCollections.observableArrayList(list);
            tableAnnouncements.setItems(observableList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải danh sách thông báo: " + e.getMessage());
        }
    }

    // Xử lý click đúp để xem chi tiết
    private void setupDoubleClickHandler() {
        tableAnnouncements.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableAnnouncements.getSelectionModel().getSelectedItem() != null) {
                Announcement selected = tableAnnouncements.getSelectionModel().getSelectedItem();
                showDetailPopup(selected);
            }
        });
    }

    // Hiển thị popup chi tiết nội dung
    private void showDetailPopup(Announcement ann) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/announcement_detail.fxml"));
            Parent root = loader.load();

            // Lấy controller của popup để truyền dữ liệu vào
            AnnouncementDetailController controller = loader.getController();
            controller.setAnnouncement(ann);

            Stage stage = new Stage();
            stage.setTitle("Chi tiết thông báo #" + ann.getAnnId());
            stage.initModality(Modality.WINDOW_MODAL); // Chặn cửa sổ cha
            stage.initOwner(tableAnnouncements.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.setResizable(false); // Popup thì thường không cần resize
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở chi tiết thông báo: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}