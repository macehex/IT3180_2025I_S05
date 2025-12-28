package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.dao.ResidentDAO;
import com.example.quanlytoanha.service.UserAccountService;
import com.example.quanlytoanha.dao.ApartmentDAO;
import com.example.quanlytoanha.model.Resident;
import javafx.beans.property.SimpleStringProperty; // Cần import
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Cần import
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent; // Cần import
import javafx.scene.Scene; // Cần import
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
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
    @FXML private Button btnProfileChangeRequests;
    @FXML private Button btnEditResident;
    @FXML private Button btnViewHistory;
    @FXML private Button btnDeleteResident;

    @FXML private Pagination pagination;


    // Data
    private ObservableList<Resident> residentList;
    private ResidentDAO residentDAO;
    private UserAccountService userAccountService;
    private ApartmentDAO apartmentDAO;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private static final int ROWS_PER_PAGE = 50;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        residentDAO = new ResidentDAO();
        userAccountService = new UserAccountService();
        apartmentDAO = new ApartmentDAO();
        residentList = FXCollections.observableArrayList();

        setupTableColumns();
        setupStatusComboBox();

        setupButtonListeners();
        setupContextMenu();
        setupPagination();
        //loadAllResidents();

        // Tự động refresh khi cửa sổ quay lại focus (đồng bộ với thay đổi căn hộ)
        tableView.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.focusedProperty().addListener((obsFocus, wasFocused, isNowFocused) -> {
                            if (isNowFocused) {
                                loadAllResidents();
                            }
                        });
                    }
                });
            }
        });
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
        Resident selectedResident = tableView.getSelectionModel().getSelectedItem();
        if (selectedResident == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một cư dân để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa cư dân \"" + selectedResident.getFullName() + "\"?");
        confirm.setContentText("Thao tác này sẽ xóa hồ sơ cư dân và các phương tiện gắn với cư dân này.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            boolean removedResident = residentDAO.removeResident(selectedResident.getResidentId());

            // Nếu có user_id, xóa luôn tài khoản (force)
            boolean removedUser = true;
            if (removedResident && selectedResident.getUserId() > 0) {
                try {
                    apartmentDAO.clearOwnerByUserId(selectedResident.getUserId());
                    removedUser = userAccountService.deleteUserForce(selectedResident.getUserId());
                } catch (Exception e) {
                    removedUser = false;
                    e.printStackTrace();
                }
            }

            if (removedResident && removedUser) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa cư dân và tài khoản liên quan.");
                loadAllResidents();
            } else if (removedResident) {
                showAlert(Alert.AlertType.WARNING, "Đã xóa cư dân", "Đã xóa cư dân nhưng không thể xóa tài khoản. Vui lòng kiểm tra tài khoản thủ công.");
                loadAllResidents();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa cư dân (cư dân không tồn tại hoặc lỗi dữ liệu).");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa cư dân: " + e.getMessage());
        }
    }

    @FXML
    private void handleProfileChangeRequests() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/admin_profile_change_requests.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Quản lý yêu cầu thay đổi thông tin cư dân");
            stage.setScene(new Scene(root, 1200, 800));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở cửa sổ quản lý yêu cầu thay đổi thông tin!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupContextMenu() {
        // Tạo RowFactory để gán ContextMenu cho mỗi dòng
        tableView.setRowFactory(tv -> {
            TableRow<Resident> row = new TableRow<>();

            // Tạo ContextMenu
            ContextMenu contextMenu = new ContextMenu();

            // Item 1: Đang cư trú
            MenuItem itemResiding = new MenuItem("🟢 Đặt: Đang cư trú (RESIDING)");
            itemResiding.setOnAction(e -> updateResidentStatus(row.getItem(), "RESIDING"));

            // Item 2: Tạm trú
            MenuItem itemTemporary = new MenuItem("🟡 Đặt: Tạm trú (TEMPORARY)");
            itemTemporary.setOnAction(e -> updateResidentStatus(row.getItem(), "TEMPORARY"));

            // Item 3: Đã chuyển đi
            MenuItem itemMovedOut = new MenuItem("🔴 Đặt: Đã chuyển đi (MOVED_OUT)");
            itemMovedOut.setOnAction(e -> updateResidentStatus(row.getItem(), "MOVED_OUT"));

            contextMenu.getItems().addAll(itemResiding, itemTemporary, itemMovedOut);

            // Chỉ hiện menu khi dòng không rỗng
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            return row;
        });
    }

    private void updateResidentStatus(Resident resident, String newStatus) {
        if (resident == null) return;

        // Nếu trạng thái không đổi thì không làm gì
        if (newStatus.equals(resident.getStatus())) {
            return;
        }

        try {
            // Gọi DAO để cập nhật (Bạn cần thêm hàm này vào ResidentDAO nếu chưa có)
            // Ví dụ: residentDAO.updateStatus(resident.getResidentId(), newStatus);

            // Ở đây tôi giả định bạn sẽ viết thêm hàm updateStatus trong DAO
            boolean success = residentDAO.updateStatus(resident.getResidentId(), newStatus);

            if (success) {
                // Cập nhật UI ngay lập tức
                resident.setStatus(newStatus);
                tableView.refresh(); // Refresh lại bảng để hiện màu/chữ mới

                // Hiển thị thông báo nhỏ (Optional)
                txtStatusMessage.setText("Đã cập nhật trạng thái: " + newStatus);
                txtStatusMessage.setStyle("-fx-fill: green");
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật trạng thái trong CSDL.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    /**
     * Thiết lập logic cho Pagination
     */
    private void setupPagination() {
        try {
            // 1. Tính tổng số trang
            int totalItems = residentDAO.countTotalResidents();
            int pageCount = (totalItems / ROWS_PER_PAGE) + 1;
            pagination.setPageCount(pageCount);

            // 2. Cài đặt Factory để load dữ liệu khi đổi trang
            pagination.setPageFactory(this::createPage);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hàm được gọi mỗi khi người dùng chọn một trang số 'pageIndex'
     */
    private Node createPage(int pageIndex) {
        loadResidentsForPage(pageIndex);
        return new VBox(); // Trả về node rỗng vì ta cập nhật TableView trực tiếp, không thay đổi nội dung Pagination
    }

    /**
     * Tải dữ liệu cho trang cụ thể
     */
    private void loadResidentsForPage(int pageIndex) {
        try {
            int offset = pageIndex * ROWS_PER_PAGE;
            List<Resident> residents = residentDAO.getResidentsByPage(ROWS_PER_PAGE, offset);

            residentList.clear();
            residentList.addAll(residents);

            lblResultCount.setText("Hiển thị: " + residents.size() + " cư dân (Trang " + (pageIndex + 1) + ")");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải trang dữ liệu: " + e.getMessage());
        }
    }
}