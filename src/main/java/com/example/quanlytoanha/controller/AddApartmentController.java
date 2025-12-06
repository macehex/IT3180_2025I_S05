// Vị trí: src/main/java/com/example/quanlytoanha/controller/AddApartmentController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Apartment;
import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.service.ApartmentService;
import com.example.quanlytoanha.service.ApartmentService.ValidationException;
import com.example.quanlytoanha.dao.ResidentDAO;
import com.example.quanlytoanha.dao.ApartmentDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.sql.SQLException;

public class AddApartmentController {

    // --- KHAI BÁO CÁC THÀNH PHẦN FXML CỦA FORM ---
    @FXML private TextField txtArea;
    @FXML private TextField txtApartmentId;
    @FXML private ComboBox<Resident> cbOwner;
    @FXML private Button btnSave;
    @FXML private Label titleLabel;
    @FXML private TableView<Apartment> tableViewApartments;
    @FXML private TableColumn<Apartment, Integer> colApartmentId;
    @FXML private TableColumn<Apartment, BigDecimal> colArea;
    @FXML private TableColumn<Apartment, String> colOwnerName;

    // --- KHAI BÁO SERVICE ---
    private final ApartmentService apartmentService = new ApartmentService();
    private final ResidentDAO residentDAO = new ResidentDAO();
    private final ApartmentDAO apartmentDAO = new ApartmentDAO();

    /**
     * Phương thức khởi tạo logic cho các ComboBox (Chạy sau khi FXML load)
     */
    @FXML
    public void initialize() {
        try {
            // Load danh sách cư dân để chọn chủ hộ
            loadOwnerList();
            
            // Load danh sách căn hộ hiện có
            loadApartmentList();
            
            // Load ID gợi ý tiếp theo
            loadSuggestedApartmentId();

            // Mặc định tiêu đề
            if (titleLabel != null) {
                titleLabel.setText("THÊM CĂN HỘ MỚI");
            }
        } catch (Exception e) {
            System.err.println("LỖI KHỞI TẠO FORM THÊM CĂN HỘ:");
            e.printStackTrace();
        }
    }

    /**
     * Load danh sách cư dân vào ComboBox chủ hộ
     * Chỉ hiển thị những cư dân có tài khoản (có user_id)
     */
    private void loadOwnerList() {
        try {
            java.util.List<Resident> allResidents = residentDAO.getAllResidents();
            
            // Lọc chỉ lấy những cư dân có user_id (có tài khoản)
            java.util.List<Resident> residentsWithAccount = new java.util.ArrayList<>();
            for (Resident resident : allResidents) {
                if (resident.getUserId() > 0) {
                    residentsWithAccount.add(resident);
                }
            }
            
            ObservableList<Resident> ownerList = FXCollections.observableArrayList(residentsWithAccount);
            
            // Thêm null vào đầu danh sách để có option "Căn hộ trống"
            ownerList.add(0, null);
            cbOwner.setItems(ownerList);
            
            // Cấu hình hiển thị tên cư dân trong ComboBox
            cbOwner.setConverter(new StringConverter<Resident>() {
                @Override
                public String toString(Resident resident) {
                    if (resident == null) {
                        return "Căn hộ trống";
                    }
                    String name = resident.getFullName() != null ? resident.getFullName() : "Chưa có tên";
                    String relationship = resident.getRelationship() != null ? " - " + resident.getRelationship() : "";
                    return name + relationship + " (" + resident.getUsername() + ")";
                }

                @Override
                public Resident fromString(String string) {
                    return null;
                }
            });
            
            // Mặc định chọn "Căn hộ trống" (null)
            cbOwner.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            System.err.println("Lỗi khi load danh sách cư dân: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không thể tải danh sách cư dân. Vui lòng thử lại.");
        }
    }

    /**
     * Load danh sách căn hộ hiện có vào TableView
     */
    private void loadApartmentList() {
        try {
            java.util.List<Apartment> apartments = apartmentDAO.getAllApartments();
            ObservableList<Apartment> apartmentList = FXCollections.observableArrayList(apartments);
            
            // Cấu hình các cột trong TableView
            if (colApartmentId != null) {
                colApartmentId.setCellValueFactory(new PropertyValueFactory<>("apartmentId"));
            }
            if (colArea != null) {
                colArea.setCellValueFactory(new PropertyValueFactory<>("area"));
                colArea.setCellFactory(column -> new TableCell<Apartment, BigDecimal>() {
                    @Override
                    protected void updateItem(BigDecimal area, boolean empty) {
                        super.updateItem(area, empty);
                        if (empty || area == null) {
                            setText(null);
                        } else {
                            setText(String.format("%.2f m²", area));
                        }
                    }
                });
            }
            if (colOwnerName != null) {
                colOwnerName.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
                colOwnerName.setCellFactory(column -> new TableCell<Apartment, String>() {
                    @Override
                    protected void updateItem(String ownerName, boolean empty) {
                        super.updateItem(ownerName, empty);
                        if (empty || ownerName == null || ownerName.isEmpty()) {
                            setText("Căn hộ trống");
                            setStyle("-fx-text-fill: #999;");
                        } else {
                            setText(ownerName);
                            setStyle("-fx-text-fill: #000;");
                        }
                    }
                });
            }
            
            if (tableViewApartments != null) {
                tableViewApartments.setItems(apartmentList);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi load danh sách căn hộ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load ID gợi ý tiếp theo cho căn hộ mới
     */
    private void loadSuggestedApartmentId() {
        try {
            int suggestedId = apartmentDAO.getNextSuggestedApartmentId();
            if (txtApartmentId != null) {
                txtApartmentId.setText(String.valueOf(suggestedId));
                txtApartmentId.setEditable(true); // Cho phép tự điền ID
                txtApartmentId.setPromptText("Để trống = tự động, hoặc nhập ID");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi load ID gợi ý: " + e.getMessage());
            e.printStackTrace();
            if (txtApartmentId != null) {
                txtApartmentId.setText("");
                txtApartmentId.setPromptText("Tự động");
            }
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút LƯU
     */
    @FXML
    private void handleSaveButtonAction() {
        try {
            // 1. Lấy dữ liệu từ form
            String areaText = txtArea.getText().trim();
            
            if (areaText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập diện tích căn hộ.");
                return;
            }

            BigDecimal area;
            try {
                area = new BigDecimal(areaText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", "Diện tích phải là số hợp lệ.");
                return;
            }

            // 2. Lấy apartment_id (nếu người dùng nhập)
            String apartmentIdText = txtApartmentId.getText().trim();
            int apartmentId = 0;
            if (!apartmentIdText.isEmpty()) {
                try {
                    apartmentId = Integer.parseInt(apartmentIdText);
                    // Kiểm tra xem ID này đã tồn tại chưa
                    if (apartmentDAO.apartmentExists(apartmentId)) {
                        showAlert(Alert.AlertType.WARNING, "ID đã tồn tại", 
                                "Căn hộ với ID " + apartmentId + " đã tồn tại. Vui lòng chọn ID khác hoặc để trống để tự động.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", "ID căn hộ phải là số nguyên.");
                    return;
                }
            }

            // 3. Lấy owner_id (có thể null)
            Resident selectedOwner = cbOwner.getSelectionModel().getSelectedItem();
            int ownerId = (selectedOwner != null && selectedOwner.getUserId() > 0) ? selectedOwner.getUserId() : 0;

            // 4. Tạo đối tượng Apartment
            Apartment newApartment = new Apartment();
            newApartment.setApartmentId(apartmentId); // 0 = tự động, >0 = chỉ định
            newApartment.setArea(area);
            newApartment.setOwnerId(ownerId);

            // 5. Gọi Service
            if (apartmentService.addApartment(newApartment)) {
                String successMsg = apartmentId > 0 
                    ? "Thêm căn hộ ID " + apartmentId + " thành công!" 
                    : "Thêm căn hộ mới thành công!";
                showAlert(Alert.AlertType.INFORMATION, "Thành công", successMsg);
                
                // Reload danh sách căn hộ và ID gợi ý
                loadApartmentList();
                loadSuggestedApartmentId();
                
                // Xóa form để thêm tiếp
                txtArea.clear();
                cbOwner.getSelectionModel().selectFirst();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm căn hộ (Lỗi không xác định).");
            }
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Lỗi DB: Không thể lưu căn hộ. " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút HỦY
     */
    @FXML
    private void handleCancelButtonAction() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    /**
     * Hiển thị thông báo
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

