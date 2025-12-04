package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.dao.AnnouncementDAO;
import com.example.quanlytoanha.model.Announcement;
import com.example.quanlytoanha.model.Notification;
import com.example.quanlytoanha.model.Role;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.service.NotificationService;
import com.example.quanlytoanha.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class NotificationViewController implements Initializable {

    // --- CÁC BIẾN FXML (Cần khớp fx:id trong file view) ---
    @FXML private TabPane tabPane;
    @FXML private Tab tabNews;          // Tab 1: Tin tức chung
    @FXML private Tab tabPersonal;      // Tab 2: Cá nhân & Thanh toán
    @FXML private Tab tabSentHistory;   // Tab 3: Đã gửi (Admin)

    @FXML private ListView<Announcement> announcementListView; // List tin tức chung
    @FXML private ListView<Notification> notificationListView; // List thông báo cá nhân
    @FXML private ListView<Announcement> sentListView;         // List lịch sử gửi

    // --- SERVICES & DAO ---
    private AnnouncementDAO announcementDAO;
    private NotificationService notificationService;

    private final SimpleDateFormat personalDateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.announcementDAO = new AnnouncementDAO();
        this.notificationService = new NotificationService();

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // --- PHÂN QUYỀN HIỂN THỊ TAB ---
        if (currentUser.getRole() == Role.ADMIN) {
            setupForAdmin(currentUser.getUserId());
        } else {
            setupForResident();
        }
    }

    // --- CHẾ ĐỘ 1: CƯ DÂN (Chỉ xem Tin tức & Cá nhân) ---
    private void setupForResident() {
        // Xóa tab của Admin đi để cư dân không thấy
        if (tabPane != null && tabSentHistory != null) {
            tabPane.getTabs().remove(tabSentHistory);
        }

        // Load dữ liệu cho 2 tab còn lại
        setupAnnouncementTab();
        setupPersonalNotificationTab();
    }

    // --- CHẾ ĐỘ 2: ADMIN (Chỉ xem Lịch sử gửi) ---
    private void setupForAdmin(int adminId) {
        // Xóa 2 tab của cư dân đi (Theo yêu cầu: "Chỉ có 1 mục")
        if (tabPane != null) {
            if (tabNews != null) tabPane.getTabs().remove(tabNews);
            if (tabPersonal != null) tabPane.getTabs().remove(tabPersonal);
        }

        // Load dữ liệu cho tab Đã gửi
        setupSentHistoryTab(adminId);
    }

    // --- LOGIC LOAD DỮ LIỆU ---

    // 1. Tab Lịch sử gửi (Admin)
    private void setupSentHistoryTab(int adminId) {
        List<Announcement> mySentList = announcementDAO.getAnnouncementsByAuthor(adminId);

        if (sentListView != null) {
            sentListView.setItems(FXCollections.observableArrayList(mySentList));

            sentListView.setCellFactory(param -> new ListCell<Announcement>() {
                @Override
                protected void updateItem(Announcement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/announcement_item.fxml"));
                            HBox graphic = loader.load();
                            AnnouncementItemController controller = loader.getController();

                            // [SỬA LỖI LẶP CHỮ]: Không sửa trực tiếp item, chỉ sửa hiển thị
                            controller.setData(item); // Set dữ liệu gốc trước

                            // Tìm Label tiêu đề và thêm chữ "[Đã gửi]" vào hiển thị (không lưu vào object)
                            Label titleLabel = (Label) graphic.lookup("#lblTitle");
                            if (titleLabel != null) {
                                titleLabel.setText("[Đã gửi] " + item.getAnnTitle());
                            }

                            setGraphic(graphic);
                            setText(null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    // 2. Tab Tin tức chung (Cư dân)
    private void setupAnnouncementTab() {
        List<Announcement> list = announcementDAO.getAllAnnouncements();
        if (announcementListView != null) {
            announcementListView.setItems(FXCollections.observableArrayList(list));

            announcementListView.setCellFactory(param -> new ListCell<Announcement>() {
                @Override
                protected void updateItem(Announcement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null); setText(null);
                    } else {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/announcement_item.fxml"));
                            HBox graphic = loader.load();
                            AnnouncementItemController controller = loader.getController();
                            controller.setData(item);
                            setGraphic(graphic); setText(null);
                            setPrefWidth(0); // Fix lỗi layout
                        } catch (IOException e) { e.printStackTrace(); }
                    }
                }
            });
        }
    }

    // 3. Tab Thông báo cá nhân (Cư dân)
    private void setupPersonalNotificationTab() {
        if (notificationListView == null) return;

        try {
            notificationListView.setItems(FXCollections.observableArrayList(
                    notificationService.getAllMyNotifications()
            ));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        notificationListView.setCellFactory(param -> new ListCell<Notification>() {
            private Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setTooltip(null);
                    setStyle("");
                } else {
                    String displayText = String.format("[%s] %s",
                            personalDateFormat.format(item.getCreatedAt()),
                            item.getTitle());
                    setText(displayText);

                    tooltip.setText(item.getMessage());
                    setTooltip(tooltip);

                    if (!item.isRead()) {
                        setStyle("-fx-font-weight: bold; -fx-background-color: #e3f2fd; -fx-text-fill: #000;");
                    } else {
                        setStyle("-fx-font-weight: normal; -fx-background-color: transparent; -fx-text-fill: #555;");
                    }
                }
            }
        });

        notificationListView.setOnMouseClicked(event -> {
            Notification selected = notificationListView.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isRead()) {
                markAsRead(selected);
            }
        });
    }

    private void markAsRead(Notification notification) {
        try {
            boolean success = notificationService.markNotificationAsRead(notification.getNotificationId());
            if (success) {
                notification.setRead(true);
                notificationListView.refresh();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void switchToSentTab() {
        // Hàm này dành cho Admin khi bấm từ Dashboard
        if (tabPane != null && tabSentHistory != null) {
            // Đảm bảo tab Đã gửi đang hiện hữu trước khi select
            if (!tabPane.getTabs().contains(tabSentHistory)) {
                tabPane.getTabs().add(tabSentHistory);
            }
            tabPane.getSelectionModel().select(tabSentHistory);
        }
    }
}