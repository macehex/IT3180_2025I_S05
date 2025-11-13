package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.dao.ResidentDAO;
import com.example.quanlytoanha.model.Resident;
import javafx.beans.property.SimpleStringProperty; // Cần import
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Cần import
import javafx.fxml.Initializable;
import javafx.scene.Parent; // Cần import
import javafx.scene.Scene; // Cần import
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Modality; // Cần import
import javafx.stage.Stage; // Cần import

import java.io.IOException; // Cần import
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class ResidentListController implements Initializable {

    // FXML Components - Search Section
    @FXML private TextField txtSearchName;
    @FXML private TextField txtSearchApartment;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private Button btnSearch;
    @FXML private Button btnShowAll;
    @FXML private Label lblResultCount;
    @FXML private Text txtStatusMessage;

    // FXML Components - Table & Actions
    @FXML private TableView<Resident> tableView;
    @FXML private TableColumn<Resident, Integer> colId;
    @FXML private TableColumn<Resident, String> colFullName;
    @FXML private TableColumn<Resident, Integer> colApartment;
    @FXML private TableColumn<Resident, String> colStatus;
    @FXML private TableColumn<Resident, String> colRelationship;
    @FXML private TableColumn<Resident, String> colIdCard;
    @FXML private TableColumn<Resident, String> colPhone;
    @FXML private TableColumn<Resident, String> colEmail;
    @FXML private TableColumn<Resident, String> colMoveInDate;
    @FXML private TableColumn<Resident, Void> colActions; // Cột này không dùng nếu nút ở dưới

    // --- BỔ SUNG: Nút thao tác ---
    @FXML private Button btnEditResident;
    @FXML private Button btnViewHistory;
    @FXML private Button btnDeleteResident;

    // Data
    private ObservableList<Resident> residentList;
    private ResidentDAO residentDAO;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        residentDAO = new ResidentDAO();
        residentList = FXCollections.observableArrayList();

        setupTableColumns();
        setupStatusComboBox();

        // --- BỔ SUNG: Logic kích hoạt nút ---
        setupButtonListeners();

        loadAllResidents();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("residentId"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colApartment.setCellValueFactory(new PropertyValueFactory<>("apartmentId"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRelationship.setCellValueFactory(new PropertyValueFactory<>("relationship"));
        colIdCard.setCellValueFactory(new PropertyValueFactory<>("idCardNumber"));

        // Custom cell factories
        colPhone.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPhoneNumber() != null ? cellData.getValue().getPhoneNumber() : ""
        ));

        colEmail.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEmail() != null ? cellData.getValue().getEmail() : ""
        ));

        colMoveInDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getMoveInDate() != null) {
                return new SimpleStringProperty(
                        dateFormat.format(cellData.getValue().getMoveInDate())
                );
            }
            return new SimpleStringProperty("");
        });

        tableView.setItems(residentList);
    }

    // --- BỔ SUNG: Logic kích hoạt nút khi chọn dòng ---
    private void setupButtonListeners() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean isSelected = newSelection != null;

                    btnEditResident.setDisable(!isSelected);
                    btnViewHistory.setDisable(!isSelected); // <-- Kích hoạt nút Lịch sử
                    btnDeleteResident.setDisable(!isSelected);
                }
        );
    }

    private void setupStatusComboBox() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "Tất cả", "RESIDING", "MOVED_OUT", "TEMPORARY"
        );
        cmbStatus.setItems(statusOptions);
        cmbStatus.setValue("Tất cả");
    }

    @FXML
    private void handleSearch() {
        try {
            String name = txtSearchName.getText().trim();
            String apartmentText = txtSearchApartment.getText().trim();
            String status = cmbStatus.getValue();

            Integer apartmentId = null;
            if (!apartmentText.isEmpty()) {
                try {
                    apartmentId = Integer.parseInt(apartmentText);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số căn hộ hợp lệ.");
                    return;
                }
            }

            String searchStatus = null;
            if (status != null && !status.equals("Tất cả")) {
                searchStatus = status;
            }

            List<Resident> results = residentDAO.searchResidents(
                    name.isEmpty() ? null : name,
                    apartmentId,
                    searchStatus
            );
            residentList.clear();
            residentList.addAll(results);

            lblResultCount.setText("Kết quả: " + results.size());
            txtStatusMessage.setText("Tìm kiếm thành công");
            txtStatusMessage.setStyle("-fx-fill: green");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm cư dân: " + e.getMessage());
        }
    }

    @FXML
    private void handleShowAll() {
        loadAllResidents();
    }

    private void loadAllResidents() {
        try {
            List<Resident> residents = residentDAO.getAllResidents();
            residentList.clear();
            residentList.addAll(residents);

            lblResultCount.setText("Kết quả: " + residents.size());
            txtStatusMessage.setText("Tải dữ liệu thành công");
            txtStatusMessage.setStyle("-fx-fill: green");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách cư dân: " + e.getMessage());
            txtStatusMessage.setText("Lỗi tải dữ liệu");
            txtStatusMessage.setStyle("-fx-fill: red");
        }
    }

    // --- BỔ SUNG: Hàm mở Form Chỉnh sửa (Dùng AddResidentController) ---
    @FXML
    private void handleEditResident() {
        Resident selectedResident = tableView.getSelectionModel().getSelectedItem();
        if (selectedResident != null) {
            // Logic mở AddResidentController ở chế độ sửa
            // (Giả định bạn có thể mở AddResidentController dưới dạng modal)
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/add_resident_form.fxml"));
                Parent root = loader.load();

                AddResidentController controller = loader.getController();

                // Cần lấy Resident ĐẦY ĐỦ nhất trước khi gửi sang Form Edit
                Resident fullResidentData = residentDAO.getResidentByUserId(selectedResident.getUserId());

                controller.setResident(fullResidentData);

                Stage stage = new Stage();
                stage.setTitle("Cập nhật Hồ sơ Cư dân");
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(tableView.getScene().getWindow());
                stage.setScene(new Scene(root, 700, 600)); // Kích thước form add
                stage.showAndWait();

                // Sau khi Form đóng, tải lại dữ liệu
                loadAllResidents();

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form chỉnh sửa: " + e.getMessage());
            }
        }
    }

    // --- BỔ SUNG: Hàm mở Lịch sử Thay đổi (US1_1_1.4) ---
    @FXML
    private void handleViewHistory() {
        Resident selectedResident = tableView.getSelectionModel().getSelectedItem();
        if (selectedResident != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/resident_history_view.fxml"));
                Parent root = loader.load();

                ResidentHistoryController controller = loader.getController();

                // Lấy Resident ID (rất quan trọng)
                int residentId = selectedResident.getResidentId();
                String residentName = selectedResident.getFullName();

                // Truyền dữ liệu cho Controller Lịch sử
                controller.setResidentData(residentId, residentName);

                Stage stage = new Stage();
                stage.setTitle("Lịch sử Thay đổi Hồ sơ: " + residentName);
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(tableView.getScene().getWindow());
                stage.setScene(new Scene(root, 1100, 650)); // Kích thước màn hình lịch sử
                stage.show(); // Không cần showAndWait

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở màn hình lịch sử: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteResident() {
        // TODO: Triển khai logic xóa cư dân
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng xóa chưa được triển khai.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}