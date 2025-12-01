// Vị trí: src/main/java/com/example/quanlytoanha/controller/AnnouncementController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.dao.ApartmentDAO;
import com.example.quanlytoanha.model.Apartment;
import com.example.quanlytoanha.model.Announcement;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.service.AnnouncementService;
import com.example.quanlytoanha.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnnouncementController {

    @FXML private TextField txtTitle;
    @FXML private TextArea txtContent;
    @FXML private CheckBox chkUrgent;
    @FXML private RadioButton rbAllResidents;
    @FXML private RadioButton rbSpecificGroup;
    @FXML private ListView<Integer> listViewApartments;
    @FXML private Button btnSend;
    @FXML private Button btnCancel;

    private AnnouncementService announcementService;
    private ApartmentDAO apartmentDAO;
    private ObservableList<Integer> apartmentList;
    private Map<Integer, javafx.beans.property.BooleanProperty> apartmentCheckStates;

    @FXML
    public void initialize() {
        this.announcementService = new AnnouncementService();
        this.apartmentDAO = new ApartmentDAO();

        // Tạo ToggleGroup cho RadioButton
        ToggleGroup recipientGroup = new ToggleGroup();
        rbAllResidents.setToggleGroup(recipientGroup);
        rbSpecificGroup.setToggleGroup(recipientGroup);
        rbAllResidents.setSelected(true); // Mặc định chọn "Toàn bộ"

        // Load danh sách căn hộ
        loadApartmentList();

        // Cấu hình ListView với CheckBox
        apartmentCheckStates = new HashMap<>();
        listViewApartments.setCellFactory(param -> new CheckBoxListCell<Integer>(item -> {
            if (!apartmentCheckStates.containsKey(item)) {
                apartmentCheckStates.put(item, new javafx.beans.property.SimpleBooleanProperty(false));
            }
            return apartmentCheckStates.get(item);
        }) {
            @Override
            public void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText("Căn hộ " + item);
                } else {
                    setText(null);
                }
            }
        });

        // Ẩn/hiện ListView dựa trên RadioButton
        rbAllResidents.selectedProperty().addListener((obs, oldVal, newVal) -> {
            listViewApartments.setVisible(!newVal);
            listViewApartments.setManaged(!newVal);
        });

        rbSpecificGroup.selectedProperty().addListener((obs, oldVal, newVal) -> {
            listViewApartments.setVisible(newVal);
            listViewApartments.setManaged(newVal);
        });

        listViewApartments.setVisible(false);
        listViewApartments.setManaged(false);
    }

    private void loadApartmentList() {
        try {
            List<Apartment> apartments = apartmentDAO.getAllApartments();
            apartmentList = FXCollections.observableArrayList(
                apartments.stream()
                    .map(Apartment::getApartmentId)
                    .sorted()
                    .collect(Collectors.toList())
            );
            listViewApartments.setItems(apartmentList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách căn hộ: " + e.getMessage());
        }
    }

    @FXML
    private void handleSend() {
        // Validation
        if (txtTitle.getText() == null || txtTitle.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập tiêu đề thông báo.");
            return;
        }

        if (txtContent.getText() == null || txtContent.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập nội dung thông báo.");
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xác định người dùng hiện tại.");
            return;
        }

        try {
            // Tạo đối tượng Announcement
            Announcement announcement = new Announcement();
            announcement.setAuthorId(currentUser.getUserId());
            announcement.setAnnTitle(txtTitle.getText().trim());
            announcement.setContent(txtContent.getText().trim());
            announcement.setUrgent(chkUrgent.isSelected());

            int successCount = 0;
            String message = "";

            // Gửi thông báo
            if (rbAllResidents.isSelected()) {
                // Gửi đến toàn bộ cư dân
                successCount = announcementService.sendAnnouncementToAll(announcement);
                message = String.format("Đã gửi thông báo thành công đến %d cư dân.", successCount);
            } else {
                // Gửi đến nhóm cụ thể
                List<Integer> selectedApartments = getSelectedApartments();
                if (selectedApartments.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn ít nhất một căn hộ.");
                    return;
                }
                successCount = announcementService.sendAnnouncementToGroup(announcement, selectedApartments);
                message = String.format("Đã gửi thông báo thành công đến %d cư dân trong %d căn hộ đã chọn.", 
                    successCount, selectedApartments.size());
            }

            showAlert(Alert.AlertType.INFORMATION, "Thành công", message);
            
            // Đóng cửa sổ sau khi gửi thành công
            Stage stage = (Stage) btnSend.getScene().getWindow();
            stage.close();

        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Quyền", e.getMessage());
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể gửi thông báo: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /**
     * Lấy danh sách căn hộ đã được chọn từ ListView
     */
    private List<Integer> getSelectedApartments() {
        List<Integer> selected = new ArrayList<>();
        for (Map.Entry<Integer, javafx.beans.property.BooleanProperty> entry : apartmentCheckStates.entrySet()) {
            if (entry.getValue().get()) {
                selected.add(entry.getKey());
            }
        }
        return selected;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

