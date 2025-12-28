package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.dao.VehicleDAO;
import com.example.quanlytoanha.model.Vehicle;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ParkingManagementController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbFilterType;
    @FXML private ComboBox<String> cbFilterStatus;

    @FXML private TableView<Vehicle> tableVehicles;
    @FXML private TableColumn<Vehicle, Number> colId;
    @FXML private TableColumn<Vehicle, String> colLicensePlate;
    @FXML private TableColumn<Vehicle, String> colOwner;
    @FXML private TableColumn<Vehicle, String> colApartment;
    @FXML private TableColumn<Vehicle, String> colType;
    @FXML private TableColumn<Vehicle, String> colStatus;
    @FXML private TableColumn<Vehicle, String> colRegisterDate;

    @FXML private Pagination pagination;

    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private VehicleDAO vehicleDAO;
    private ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();
    private FilteredList<Vehicle> filteredData;

    private static final int ROWS_PER_PAGE = 25;
    private static final Pattern LICENSE_PLATE_PATTERN = Pattern.compile("^[0-9]{2}[A-Z0-9]{1,2}[-. ]?[0-9]{3,5}(?:\\.[0-9]{2})?$", Pattern.CASE_INSENSITIVE);

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();

        colId.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getVehicleId()));
        colLicensePlate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLicensePlate()));
        colOwner.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getResidentFullName()));
        colApartment.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getApartmentId())));

        colType.setCellValueFactory(cell -> {
            String type = cell.getValue().getVehicleType();
            String display = "Khác";
            if ("CAR".equalsIgnoreCase(type)) display = "Ô tô";
            else if ("MOTORBIKE".equalsIgnoreCase(type)) display = "Xe máy";
            else if ("ELECTRIC_BIKE".equalsIgnoreCase(type)) display = "Xe đạp điện";
            return new SimpleStringProperty(display);
        });

        colStatus.setCellValueFactory(cell -> {
            boolean active = cell.getValue().isActive();
            return new SimpleStringProperty(active ? "Đang gửi" : "Ngừng gửi");
        });

        colRegisterDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRegistrationDate().toString()));

        colStatus.setCellFactory(column -> new TableCell<Vehicle, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Đang gửi")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red;");
                    }
                }
            }
        });

        cbFilterType.setItems(FXCollections.observableArrayList("Tất cả", "Ô tô", "Xe máy", "Xe đạp điện"));
        cbFilterStatus.setItems(FXCollections.observableArrayList("Tất cả", "Đang gửi", "Ngừng gửi"));
        cbFilterType.getSelectionModel().selectFirst();
        cbFilterStatus.getSelectionModel().selectFirst();

        filteredData = new FilteredList<>(vehicleList, p -> true);

        txtSearch.textProperty().addListener((o, old, val) -> updatePagination());
        cbFilterType.valueProperty().addListener((o, old, val) -> updatePagination());
        cbFilterStatus.valueProperty().addListener((o, old, val) -> updatePagination());

        if (pagination != null) {
            pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
                changePage(newIndex.intValue());
            });
        }

        tableVehicles.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnEdit.setDisable(!hasSelection);

            // Nếu xe đã ngừng gửi rồi thì đổi text nút Xóa thành vô hiệu hóa hoặc thông báo
            if (newSelection != null && !newSelection.isActive()) {
                btnDelete.setText("Đã hủy");
                btnDelete.setDisable(true);
            } else {
                btnDelete.setText("Hủy đăng ký (Xóa)");
                btnDelete.setDisable(!hasSelection);
            }
        });

        loadDataFromDB();
    }

    private void loadDataFromDB() {
        vehicleList.clear();
        List<Vehicle> dbVehicles = vehicleDAO.getAllVehicles();
        vehicleList.addAll(dbVehicles);
        updatePagination();
    }

    private void updatePagination() {
        applyFilter();

        if (pagination != null) {
            int totalItems = filteredData.size();
            int pageCount = (totalItems + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE;

            pagination.setPageCount(pageCount == 0 ? 1 : pageCount);

            if (pagination.getCurrentPageIndex() > pageCount) {
                pagination.setCurrentPageIndex(0);
            }
            changePage(pagination.getCurrentPageIndex());
        } else {
            tableVehicles.setItems(filteredData);
        }
    }

    private void applyFilter() {
        String searchText = txtSearch.getText().toLowerCase();
        String selectedType = cbFilterType.getValue();
        String selectedStatus = cbFilterStatus.getValue();

        filteredData.setPredicate(vehicle -> {
            boolean matchSearch = searchText.isEmpty() ||
                    vehicle.getLicensePlate().toLowerCase().contains(searchText) ||
                    (vehicle.getResidentFullName() != null && vehicle.getResidentFullName().toLowerCase().contains(searchText)) ||
                    String.valueOf(vehicle.getApartmentId()).contains(searchText);

            boolean matchType = selectedType == null || selectedType.equals("Tất cả");
            if (!matchType) {
                if (selectedType.equals("Ô tô")) matchType = "CAR".equalsIgnoreCase(vehicle.getVehicleType());
                else if (selectedType.equals("Xe máy")) matchType = "MOTORBIKE".equalsIgnoreCase(vehicle.getVehicleType());
                else if (selectedType.equals("Xe đạp điện")) matchType = "ELECTRIC_BIKE".equalsIgnoreCase(vehicle.getVehicleType());
            }

            boolean matchStatus = selectedStatus == null || selectedStatus.equals("Tất cả");
            if (!matchStatus) {
                if (selectedStatus.equals("Đang gửi")) matchStatus = vehicle.isActive();
                else if (selectedStatus.equals("Ngừng gửi")) matchStatus = !vehicle.isActive();
            }

            return matchSearch && matchType && matchStatus;
        });
    }

    private void changePage(int pageIndex) {
        if (filteredData.isEmpty()) {
            tableVehicles.setItems(FXCollections.emptyObservableList());
            return;
        }

        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredData.size());

        if (fromIndex > toIndex) fromIndex = 0;

        List<Vehicle> pageData = filteredData.subList(fromIndex, toIndex);
        tableVehicles.setItems(FXCollections.observableArrayList(pageData));
    }

    @FXML private void handleSearch() { updatePagination(); }

    @FXML private void handleReset() {
        txtSearch.clear();
        cbFilterType.getSelectionModel().selectFirst();
        cbFilterStatus.getSelectionModel().selectFirst();
        loadDataFromDB();
    }

    @FXML private void handleAddVehicle() { showVehicleDialog(null); }

    @FXML private void handleEditVehicle() {
        Vehicle selected = tableVehicles.getSelectionModel().getSelectedItem();
        if (selected != null) showVehicleDialog(selected);
    }

    // --- LOGIC XÓA MỀM (SOFT DELETE) ---
    @FXML private void handleDeleteVehicle() {
        Vehicle selected = tableVehicles.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận hủy đăng ký");
            alert.setHeaderText("Ngừng gửi phương tiện");
            alert.setContentText("Bạn có chắc chắn muốn chuyển trạng thái xe biển số " + selected.getLicensePlate() + " sang 'Ngừng gửi' không?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Đổi trạng thái sang false (Ngừng gửi)
                selected.setActive(false);

                // Gọi hàm updateVehicle thay vì deleteVehicle
                if (vehicleDAO.updateVehicle(selected)) {
                    loadDataFromDB(); // Tải lại dữ liệu để cập nhật hiển thị
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã chuyển trạng thái xe sang ngừng gửi.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật trạng thái xe.");
                    // Rollback trạng thái trên giao diện nếu lỗi DB
                    selected.setActive(true);
                }
            }
        }
    }

    private void showVehicleDialog(Vehicle vehicle) {
        Dialog<Vehicle> dialog = new Dialog<>();
        dialog.setTitle(vehicle == null ? "Đăng ký xe mới" : "Cập nhật thông tin xe");

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtPlate = new TextField();
        txtPlate.setPromptText("VD: 30A-123.45 hoặc 29B1-12345");

        TextField txtResidentId = new TextField();
        txtResidentId.setPromptText("Nhập ID Cư dân");

        TextField txtApartmentId = new TextField();
        txtApartmentId.setPromptText("Nhập Số phòng (ID)");

        ComboBox<String> cbType = new ComboBox<>();
        cbType.setItems(FXCollections.observableArrayList("Ô tô", "Xe máy", "Xe đạp điện"));
        cbType.getSelectionModel().selectFirst();

        CheckBox chkActive = new CheckBox("Đang hoạt động");
        chkActive.setSelected(true);

        grid.add(new Label("Biển số:"), 0, 0);
        grid.add(txtPlate, 1, 0);
        grid.add(new Label("ID Cư dân:"), 0, 1);
        grid.add(txtResidentId, 1, 1);
        grid.add(new Label("Số phòng:"), 0, 2);
        grid.add(txtApartmentId, 1, 2);
        grid.add(new Label("Loại xe:"), 0, 3);
        grid.add(cbType, 1, 3);

        if (vehicle != null) {
            grid.add(new Label("Trạng thái:"), 0, 4);
            grid.add(chkActive, 1, 4);

            txtPlate.setText(vehicle.getLicensePlate());
            txtResidentId.setText(String.valueOf(vehicle.getResidentId()));
            txtApartmentId.setText(String.valueOf(vehicle.getApartmentId()));

            String type = vehicle.getVehicleType();
            if ("CAR".equals(type)) cbType.setValue("Ô tô");
            else if ("MOTORBIKE".equals(type)) cbType.setValue("Xe máy");
            else cbType.setValue("Xe đạp điện");

            chkActive.setSelected(vehicle.isActive());
        }

        dialog.getDialogPane().setContent(grid);

        Button btnSave = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        btnSave.addEventFilter(ActionEvent.ACTION, event -> {
            String plate = txtPlate.getText().trim().toUpperCase();

            if (plate.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập biển số xe!");
                event.consume();
                return;
            }

            if (!LICENSE_PLATE_PATTERN.matcher(plate).matches()) {
                showAlert(Alert.AlertType.WARNING, "Sai định dạng",
                        "Biển số xe không hợp lệ! (Phải có ít nhất 3-5 số ở cuối).\n" +
                                "Ví dụ đúng: 30A-123.45, 29B1-5678, 98K-1234");
                event.consume();
                return;
            }

            try {
                String resIdText = txtResidentId.getText().trim();
                String aptIdText = txtApartmentId.getText().trim();

                if (resIdText.isEmpty() || aptIdText.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập ID Cư dân và Số phòng!");
                    event.consume();
                    return;
                }

                int resId = Integer.parseInt(resIdText);
                int aptId = Integer.parseInt(aptIdText);

                if (resId < 0 || aptId < 0) {
                    showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", "ID Cư dân và Số phòng phải là số dương!");
                    event.consume();
                    return;
                }

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Dữ liệu không hợp lệ", "ID Cư dân và Số phòng phải là số nguyên!");
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String plate = txtPlate.getText().trim().toUpperCase();
                int resId = Integer.parseInt(txtResidentId.getText().trim());
                int aptId = Integer.parseInt(txtApartmentId.getText().trim());

                String typeStr = cbType.getValue();
                String dbType = "MOTORBIKE";
                if ("Ô tô".equals(typeStr)) dbType = "CAR";
                else if ("Xe đạp điện".equals(typeStr)) dbType = "ELECTRIC_BIKE";

                if (vehicle == null) {
                    return new Vehicle(resId, aptId, plate, dbType);
                } else {
                    vehicle.setLicensePlate(plate);
                    vehicle.setResidentId(resId);
                    vehicle.setApartmentId(aptId);
                    vehicle.setVehicleType(dbType);
                    vehicle.setActive(chkActive.isSelected());
                    return vehicle;
                }
            }
            return null;
        });

        Optional<Vehicle> result = dialog.showAndWait();

        result.ifPresent(newVehicle -> {
            try {
                if (vehicle == null) {
                    if (vehicleDAO.addVehicle(newVehicle)) {
                        loadDataFromDB();
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm phương tiện mới.");
                    }
                } else {
                    if (vehicleDAO.updateVehicle(newVehicle)) {
                        loadDataFromDB();
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật phương tiện.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                String msg = e.getMessage();
                String sqlState = e.getSQLState();

                if (msg != null) {
                    String lowerMsg = msg.toLowerCase();

                    if (lowerMsg.contains("foreign key constraint")) {
                        if (lowerMsg.contains("resident_id") || lowerMsg.contains("residents")) {
                            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "ID Cư dân (" + newVehicle.getResidentId() + ") không tồn tại!");
                        } else if (lowerMsg.contains("apartment_id") || lowerMsg.contains("apartments")) {
                            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Số phòng (ID: " + newVehicle.getApartmentId() + ") không tồn tại!");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Dữ liệu tham chiếu không hợp lệ (Foreign Key).\n" + msg);
                        }
                    }
                    else if (lowerMsg.contains("vehicles_pkey")) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống (ID Sequence)",
                                "Lỗi bộ đếm ID tự động (Sequence) bị lệch so với dữ liệu thực tế.\n" +
                                        "Cách sửa: Yêu cầu Admin chạy lệnh SQL sau để đồng bộ lại:\n" +
                                        "SELECT setval('vehicles_vehicle_id_seq', (SELECT MAX(vehicle_id) FROM vehicles) + 1);");
                    }
                    else if (lowerMsg.contains("vehicles_license_plate_key") || lowerMsg.contains("duplicate key")) {
                        showAlert(Alert.AlertType.ERROR, "Trùng Biển Số",
                                "Biển số xe " + newVehicle.getLicensePlate() + " đã tồn tại trong hệ thống!");
                    }
                    else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi Cơ Sở Dữ Liệu", "Lỗi SQL: " + msg + "\nSQLState: " + sqlState);
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Không Xác Định", "Đã xảy ra lỗi khi lưu vào CSDL.");
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}