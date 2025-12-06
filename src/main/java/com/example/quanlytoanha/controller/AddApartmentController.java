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
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private Label titleLabel;
    @FXML private TableView<Apartment> tableViewApartments;
    @FXML private TableColumn<Apartment, Integer> colApartmentId;
    @FXML private TableColumn<Apartment, BigDecimal> colArea;
    @FXML private TableColumn<Apartment, String> colOwnerName;

    // --- KHAI BÁO SERVICE ---
    private final ApartmentService apartmentService = new ApartmentService();
    private final ResidentDAO residentDAO = new ResidentDAO();
    private final ApartmentDAO apartmentDAO = new ApartmentDAO();
    
    // Biến lưu căn hộ đang được chọn để sửa/xóa
    private Apartment selectedApartment = null;

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
                titleLabel.setText("QUẢN LÝ CĂN HỘ");
            }
            
            // Thêm listener cho TableView để chọn căn hộ
            if (tableViewApartments != null) {
                tableViewApartments.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            loadApartmentToForm(newValue);
                        }
                    }
                );
            }
            
            // Ẩn nút Delete ban đầu (chỉ hiện khi chọn căn hộ)
            if (btnDelete != null) {
                btnDelete.setVisible(false);
                btnDelete.setManaged(false);
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
                    return name + " (" + resident.getUsername() + ")";
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
                    
                    // CHỈ KIỂM TRA TRÙNG KHI ĐANG THÊM MỚI (không phải cập nhật)
                    if (selectedApartment == null && apartmentDAO.apartmentExists(apartmentId)) {
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
            Apartment apartment = new Apartment();
            apartment.setApartmentId(apartmentId); // 0 = tự động, >0 = chỉ định
            apartment.setArea(area);
            apartment.setOwnerId(ownerId);

            // 5. Gọi Service (Thêm mới hoặc Cập nhật)
            if (selectedApartment == null) {
                // CHẾ ĐỘ THÊM MỚI
                if (apartmentService.addApartment(apartment)) {
                    String successMsg = apartmentId > 0 
                        ? "Thêm căn hộ ID " + apartmentId + " thành công!" 
                        : "Thêm căn hộ mới thành công!";
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", successMsg);
                    
                    // Reload danh sách căn hộ và reset form
                    loadApartmentList();
                    handleClearForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm căn hộ (Lỗi không xác định).");
                }
            } else {
                // CHẾ ĐỘ CẬP NHẬT
                apartment.setApartmentId(selectedApartment.getApartmentId()); // Dùng ID của căn hộ đang sửa
                if (apartmentService.updateApartment(apartment)) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                            "Cập nhật thông tin căn hộ ID " + apartment.getApartmentId() + " thành công!");
                    
                    // Reload danh sách và reset form
                    loadApartmentList();
                    handleClearForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật căn hộ (Lỗi không xác định).");
                }
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
     * Load thông tin căn hộ vào form để sửa
     */
    private void loadApartmentToForm(Apartment apartment) {
        selectedApartment = apartment;
        
        // Điền thông tin vào form
        if (txtApartmentId != null) {
            txtApartmentId.setText(String.valueOf(apartment.getApartmentId()));
            txtApartmentId.setEditable(false); // Không cho sửa ID
            txtApartmentId.setStyle("-fx-background-color: #e9ecef;");
        }
        
        if (txtArea != null) {
            txtArea.setText(apartment.getArea().toString());
        }
        
        // Chọn chủ hộ tương ứng
        if (cbOwner != null) {
            if (apartment.getOwnerId() > 0) {
                // Tìm resident có userId = apartment.ownerId
                for (Resident resident : cbOwner.getItems()) {
                    if (resident != null && resident.getUserId() == apartment.getOwnerId()) {
                        cbOwner.getSelectionModel().select(resident);
                        break;
                    }
                }
            } else {
                // Chọn "Căn hộ trống"
                cbOwner.getSelectionModel().selectFirst();
            }
        }
        
        // Đổi text nút và hiện nút Delete
        if (btnSave != null) {
            btnSave.setText("💾 Cập Nhật");
        }
        if (btnDelete != null) {
            btnDelete.setVisible(true);
            btnDelete.setManaged(true);
        }
        if (titleLabel != null) {
            titleLabel.setText("SỬA THÔNG TIN CĂN HỘ");
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút XÓA
     */
    @FXML
    private void handleDeleteButtonAction() {
        if (selectedApartment == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn căn hộ cần xóa.");
            return;
        }
        
        // Xác nhận xóa
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa căn hộ này?");
        confirmAlert.setContentText("Căn hộ ID: " + selectedApartment.getApartmentId() + 
                                    "\nDiện tích: " + selectedApartment.getArea() + " m²");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (apartmentService.deleteApartment(selectedApartment.getApartmentId())) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                                "Đã xóa căn hộ ID " + selectedApartment.getApartmentId());
                        
                        // Reload danh sách và reset form
                        loadApartmentList();
                        handleClearForm();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa căn hộ.");
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Database", 
                            "Lỗi: " + e.getMessage() + 
                            "\n\nLưu ý: Không thể xóa căn hộ có cư dân hoặc hóa đơn liên quan.");
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Xử lý sự kiện khi nhấn nút LÀM MỚI FORM
     */
    @FXML
    private void handleClearForm() {
        selectedApartment = null;
        
        // Reset form
        if (txtArea != null) {
            txtArea.clear();
        }
        if (cbOwner != null) {
            cbOwner.getSelectionModel().selectFirst();
        }
        
        // Reset ID và cho phép tự động
        loadSuggestedApartmentId();
        if (txtApartmentId != null) {
            txtApartmentId.setEditable(true);
            txtApartmentId.setStyle("");
        }
        
        // Đổi text nút về Thêm mới
        if (btnSave != null) {
            btnSave.setText("💾 Lưu Căn Hộ");
        }
        if (btnDelete != null) {
            btnDelete.setVisible(false);
            btnDelete.setManaged(false);
        }
        if (titleLabel != null) {
            titleLabel.setText("QUẢN LÝ CĂN HỘ");
        }
        
        // Bỏ chọn trong TableView
        if (tableViewApartments != null) {
            tableViewApartments.getSelectionModel().clearSelection();
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

