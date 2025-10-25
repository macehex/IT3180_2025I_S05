package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.dao.ResidentDAO;
import com.example.quanlytoanha.model.ResidentPOJO;
import com.example.quanlytoanha.session.SessionManager;
import com.example.quanlytoanha.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class ResidentListController implements Initializable {

    // FXML Components - Header
    @FXML private Label lblWelcome;

    // FXML Components - Search Section
    @FXML private TextField txtSearchName;
    @FXML private TextField txtSearchApartment;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private Button btnSearch;
    @FXML private Button btnShowAll;
    @FXML private Label lblResultCount;
    @FXML private Text txtStatusMessage;

    // FXML Components - Table
    @FXML private TableView<ResidentPOJO> tableView;
    @FXML private TableColumn<ResidentPOJO, Integer> colId;
    @FXML private TableColumn<ResidentPOJO, String> colFullName;
    @FXML private TableColumn<ResidentPOJO, Integer> colApartment;
    @FXML private TableColumn<ResidentPOJO, String> colStatus;
    @FXML private TableColumn<ResidentPOJO, String> colRelationship;
    @FXML private TableColumn<ResidentPOJO, String> colIdCard;
    @FXML private TableColumn<ResidentPOJO, String> colPhone;
    @FXML private TableColumn<ResidentPOJO, String> colEmail;
    @FXML private TableColumn<ResidentPOJO, String> colMoveInDate;

    // Data
    private ObservableList<ResidentPOJO> residentList;
    private ResidentDAO residentDAO;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAO
        residentDAO = new ResidentDAO();
        
        // Initialize resident list
        residentList = FXCollections.observableArrayList();
        
        // Setup table columns
        setupTableColumns();
        
        // Setup status combo box
        setupStatusComboBox();
        
        // Load initial data
        loadAllResidents();
        
        // Setup welcome message
        setupWelcomeMessage();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("residentId"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colApartment.setCellValueFactory(new PropertyValueFactory<>("apartmentId"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRelationship.setCellValueFactory(new PropertyValueFactory<>("relationship"));
        colIdCard.setCellValueFactory(new PropertyValueFactory<>("idCardNumber"));
        
        // Custom cell factories for phone, email, and move-in date
        colPhone.setCellValueFactory(cellData -> {
            ResidentPOJO resident = cellData.getValue();
            if (resident instanceof ResidentDAO.ExtendedResidentPOJO) {
                return new javafx.beans.property.SimpleStringProperty(
                    ((ResidentDAO.ExtendedResidentPOJO) resident).getPhoneNumber()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        colEmail.setCellValueFactory(cellData -> {
            ResidentPOJO resident = cellData.getValue();
            if (resident instanceof ResidentDAO.ExtendedResidentPOJO) {
                return new javafx.beans.property.SimpleStringProperty(
                    ((ResidentDAO.ExtendedResidentPOJO) resident).getEmail()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        colMoveInDate.setCellValueFactory(cellData -> {
            ResidentPOJO resident = cellData.getValue();
            if (resident.getMoveInDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    dateFormat.format(resident.getMoveInDate())
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        // Set table data
        tableView.setItems(residentList);
    }

    private void setupStatusComboBox() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
            "Tất cả", "RESIDING", "MOVED_OUT", "TEMPORARY"
        );
        cmbStatus.setItems(statusOptions);
        cmbStatus.setValue("Tất cả");
    }

    private void setupWelcomeMessage() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblWelcome.setText("Xin chào, " + currentUser.getFullName());
        }
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
            
            List<ResidentPOJO> results = residentDAO.searchResidents(name, apartmentId, searchStatus);
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
            List<ResidentPOJO> residents = residentDAO.getAllResidents();
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
