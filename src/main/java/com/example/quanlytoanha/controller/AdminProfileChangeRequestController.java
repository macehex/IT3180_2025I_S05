package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.dao.ProfileChangeRequestDAO;
import com.example.quanlytoanha.model.ProfileChangeRequest;
import com.example.quanlytoanha.session.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminProfileChangeRequestController implements Initializable {

    @FXML private TableView<ProfileChangeRequest> tableView;
    @FXML private TableColumn<ProfileChangeRequest, Integer> colRequestId;
    @FXML private TableColumn<ProfileChangeRequest, String> colRequester;
    @FXML private TableColumn<ProfileChangeRequest, Integer> colApartment;
    @FXML private TableColumn<ProfileChangeRequest, String> colStatus;
    @FXML private TableColumn<ProfileChangeRequest, String> colCreatedAt;
    
    @FXML private Label lblCurrentUsername;
    @FXML private Label lblCurrentPhoneNumber;
    @FXML private Label lblCurrentEmail;
    @FXML private Label lblCurrentFullName;
    @FXML private Label lblCurrentRelationship;
    @FXML private Label lblCurrentDateOfBirth;
    @FXML private Label lblCurrentIdCardNumber;
    
    @FXML private Label lblNewUsername;
    @FXML private Label lblNewPhoneNumber;
    @FXML private Label lblNewEmail;
    @FXML private Label lblNewFullName;
    @FXML private Label lblNewRelationship;
    @FXML private Label lblNewDateOfBirth;
    @FXML private Label lblNewIdCardNumber;
    
    @FXML private TextArea txtAdminComment;
    @FXML private Button btnApprove;
    @FXML private Button btnReject;
    @FXML private Button btnRefresh;
    @FXML private Button btnClose;
    @FXML private Label lblResultCount;

    private ProfileChangeRequestDAO profileChangeRequestDAO;
    private ObservableList<ProfileChangeRequest> requestList;
    private ProfileChangeRequest selectedRequest;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("DEBUG: AdminProfileChangeRequestController initializing...");
        profileChangeRequestDAO = new ProfileChangeRequestDAO();
        requestList = FXCollections.observableArrayList();
        
        setupTable();
        setupSelectionListener();
        
        // Test database table existence
        testDatabaseConnection();
        loadAllRequests();
    }
    
    private void testDatabaseConnection() {
        try {
            System.out.println("DEBUG: Testing database connection and table...");
            List<ProfileChangeRequest> testRequests = profileChangeRequestDAO.getAllRequests();
            System.out.println("DEBUG: Database test successful. Found " + testRequests.size() + " requests.");
        } catch (Exception e) {
            System.err.println("DEBUG: Database test failed: " + e.getMessage());
            e.printStackTrace();
            showAlert("Cảnh báo", 
                     "Không thể kết nối đến cơ sở dữ liệu hoặc bảng 'profile_change_requests' chưa được tạo.\n\n" +
                     "Vui lòng tạo bảng bằng câu lệnh SQL sau:\n" +
                     "CREATE TABLE profile_change_requests (...)\n\n" +
                     "Lỗi: " + e.getMessage(), 
                     Alert.AlertType.WARNING);
        }
    }

    private void setupTable() {
        colRequestId.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        colRequester.setCellValueFactory(new PropertyValueFactory<>("requesterFullName"));
        colApartment.setCellValueFactory(new PropertyValueFactory<>("apartmentId"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCreatedAt.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(
                cellData.getValue().getCreatedAt() != null ? 
                sdf.format(cellData.getValue().getCreatedAt()) : ""
            );
        });

        // Style status column
        colStatus.setCellFactory(column -> new TableCell<ProfileChangeRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "PENDING":
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                            break;
                        case "APPROVED":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                            break;
                        case "REJECTED":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        tableView.setItems(requestList);
    }

    private void setupSelectionListener() {
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            System.out.println("DEBUG: Selection changed from " + 
                             (oldSelection != null ? oldSelection.getRequestId() : "null") + 
                             " to " + (newSelection != null ? newSelection.getRequestId() : "null"));
            selectedRequest = newSelection;
            updateDetailsView();
            updateButtonStates();
        });
    }

    private void updateDetailsView() {
        if (selectedRequest == null) {
            clearDetailsView();
            return;
        }

        // Current values
        lblCurrentUsername.setText(selectedRequest.getCurrentUsername() != null ? selectedRequest.getCurrentUsername() : "");
        lblCurrentPhoneNumber.setText(selectedRequest.getCurrentPhoneNumber() != null ? selectedRequest.getCurrentPhoneNumber() : "");
        lblCurrentEmail.setText(selectedRequest.getCurrentEmail() != null ? selectedRequest.getCurrentEmail() : "");
        lblCurrentFullName.setText(selectedRequest.getCurrentFullName() != null ? selectedRequest.getCurrentFullName() : "");
        lblCurrentRelationship.setText(selectedRequest.getCurrentRelationship() != null ? selectedRequest.getCurrentRelationship() : "");
        lblCurrentDateOfBirth.setText(selectedRequest.getCurrentDateOfBirth() != null ? selectedRequest.getCurrentDateOfBirth().toString() : "");
        lblCurrentIdCardNumber.setText(selectedRequest.getCurrentIdCardNumber() != null ? selectedRequest.getCurrentIdCardNumber() : "");

        // New values
        lblNewUsername.setText(selectedRequest.getNewUsername() != null ? selectedRequest.getNewUsername() : "");
        lblNewPhoneNumber.setText(selectedRequest.getNewPhoneNumber() != null ? selectedRequest.getNewPhoneNumber() : "");
        lblNewEmail.setText(selectedRequest.getNewEmail() != null ? selectedRequest.getNewEmail() : "");
        lblNewFullName.setText(selectedRequest.getNewFullName() != null ? selectedRequest.getNewFullName() : "");
        lblNewRelationship.setText(selectedRequest.getNewRelationship() != null ? selectedRequest.getNewRelationship() : "");
        lblNewDateOfBirth.setText(selectedRequest.getNewDateOfBirth() != null ? selectedRequest.getNewDateOfBirth().toString() : "");
        lblNewIdCardNumber.setText(selectedRequest.getNewIdCardNumber() != null ? selectedRequest.getNewIdCardNumber() : "");

        // Highlight changes
        highlightChanges();

        // Show admin comment if exists
        if (selectedRequest.getAdminComment() != null) {
            txtAdminComment.setText(selectedRequest.getAdminComment());
        } else {
            txtAdminComment.setText("");
        }
    }

    private void highlightChanges() {
        // Reset all styles first
        resetLabelStyles();

        // Highlight changed fields
        if (!equals(selectedRequest.getCurrentUsername(), selectedRequest.getNewUsername())) {
            lblNewUsername.setStyle("-fx-background-color: #fff3cd; -fx-padding: 5;");
        }
        if (!equals(selectedRequest.getCurrentPhoneNumber(), selectedRequest.getNewPhoneNumber())) {
            lblNewPhoneNumber.setStyle("-fx-background-color: #fff3cd; -fx-padding: 5;");
        }
        if (!equals(selectedRequest.getCurrentEmail(), selectedRequest.getNewEmail())) {
            lblNewEmail.setStyle("-fx-background-color: #fff3cd; -fx-padding: 5;");
        }
        if (!equals(selectedRequest.getCurrentFullName(), selectedRequest.getNewFullName())) {
            lblNewFullName.setStyle("-fx-background-color: #fff3cd; -fx-padding: 5;");
        }
        if (!equals(selectedRequest.getCurrentRelationship(), selectedRequest.getNewRelationship())) {
            lblNewRelationship.setStyle("-fx-background-color: #fff3cd; -fx-padding: 5;");
        }
        if (!equals(selectedRequest.getCurrentDateOfBirth(), selectedRequest.getNewDateOfBirth())) {
            lblNewDateOfBirth.setStyle("-fx-background-color: #fff3cd; -fx-padding: 5;");
        }
        if (!equals(selectedRequest.getCurrentIdCardNumber(), selectedRequest.getNewIdCardNumber())) {
            lblNewIdCardNumber.setStyle("-fx-background-color: #fff3cd; -fx-padding: 5;");
        }
    }

    private void resetLabelStyles() {
        String defaultStyle = "-fx-padding: 5;";
        lblNewUsername.setStyle(defaultStyle);
        lblNewPhoneNumber.setStyle(defaultStyle);
        lblNewEmail.setStyle(defaultStyle);
        lblNewFullName.setStyle(defaultStyle);
        lblNewRelationship.setStyle(defaultStyle);
        lblNewDateOfBirth.setStyle(defaultStyle);
        lblNewIdCardNumber.setStyle(defaultStyle);
    }

    private boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 == null || o2 == null) return false;
        return o1.equals(o2);
    }

    private void clearDetailsView() {
        lblCurrentUsername.setText("");
        lblCurrentPhoneNumber.setText("");
        lblCurrentEmail.setText("");
        lblCurrentFullName.setText("");
        lblCurrentRelationship.setText("");
        lblCurrentDateOfBirth.setText("");
        lblCurrentIdCardNumber.setText("");
        
        lblNewUsername.setText("");
        lblNewPhoneNumber.setText("");
        lblNewEmail.setText("");
        lblNewFullName.setText("");
        lblNewRelationship.setText("");
        lblNewDateOfBirth.setText("");
        lblNewIdCardNumber.setText("");
        
        txtAdminComment.setText("");
        resetLabelStyles();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedRequest != null;
        boolean isPending = hasSelection && "PENDING".equals(selectedRequest.getStatus());
        
        System.out.println("DEBUG: updateButtonStates - hasSelection: " + hasSelection + 
                         ", isPending: " + isPending + 
                         ", selectedRequest: " + (selectedRequest != null ? selectedRequest.getRequestId() : "null") +
                         ", status: " + (selectedRequest != null ? selectedRequest.getStatus() : "null"));
        
        btnApprove.setDisable(!isPending);
        btnReject.setDisable(!isPending);
        
        System.out.println("DEBUG: btnApprove.isDisabled(): " + btnApprove.isDisabled());
        System.out.println("DEBUG: btnReject.isDisabled(): " + btnReject.isDisabled());
    }

    @FXML
    private void handleApprove() {
        System.out.println("DEBUG: handleApprove called"); // Debug log
        
        if (selectedRequest == null) {
            showAlert("Lỗi", "Vui lòng chọn một yêu cầu để phê duyệt!", Alert.AlertType.WARNING);
            return;
        }
        
        if (!"PENDING".equals(selectedRequest.getStatus())) {
            showAlert("Lỗi", "Chỉ có thể phê duyệt các yêu cầu đang chờ xử lý!", Alert.AlertType.WARNING);
            return;
        }

        String comment = txtAdminComment.getText().trim();
        if (comment.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập ghi chú cho quyết định phê duyệt!", Alert.AlertType.WARNING);
            txtAdminComment.requestFocus(); // Focus on comment field
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận phê duyệt");
        confirmAlert.setHeaderText("Phê duyệt yêu cầu thay đổi thông tin");
        confirmAlert.setContentText("Bạn có chắc chắn muốn phê duyệt yêu cầu này?\n" +
                                   "Thông tin của cư dân sẽ được cập nhật theo yêu cầu.\n\n" +
                                   "Ghi chú: " + comment);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Disable buttons during processing
                btnApprove.setDisable(true);
                btnReject.setDisable(true);
                btnApprove.setText("Đang xử lý...");
                
                int adminUserId = SessionManager.getInstance().getCurrentUser().getUserId();
                System.out.println("DEBUG: Approving request ID: " + selectedRequest.getRequestId() + 
                                 ", Admin ID: " + adminUserId + ", Comment: " + comment);
                
                boolean success = profileChangeRequestDAO.approveRequest(selectedRequest.getRequestId(), adminUserId, comment);
                
                if (success) {
                    showAlert("Thành công", "Đã phê duyệt yêu cầu thay đổi thông tin!\n" +
                            "Thông tin cư dân đã được cập nhật.", Alert.AlertType.INFORMATION);
                    txtAdminComment.clear(); // Clear comment after successful approval
                    loadAllRequests();
                } else {
                    showAlert("Lỗi", "Không thể phê duyệt yêu cầu. Vui lòng kiểm tra:\n" +
                            "- Kết nối cơ sở dữ liệu\n" +
                            "- Trạng thái yêu cầu\n" +
                            "- Quyền truy cập", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Lỗi", "Có lỗi xảy ra khi phê duyệt: " + e.getMessage(), Alert.AlertType.ERROR);
            } finally {
                // Re-enable buttons
                btnApprove.setText("Phê duyệt");
                updateButtonStates(); // This will properly set button states
            }
        }
    }

    @FXML
    private void handleReject() {
        System.out.println("DEBUG: handleReject called"); // Debug log
        
        if (selectedRequest == null) {
            showAlert("Lỗi", "Vui lòng chọn một yêu cầu để từ chối!", Alert.AlertType.WARNING);
            return;
        }
        
        if (!"PENDING".equals(selectedRequest.getStatus())) {
            showAlert("Lỗi", "Chỉ có thể từ chối các yêu cầu đang chờ xử lý!", Alert.AlertType.WARNING);
            return;
        }

        String comment = txtAdminComment.getText().trim();
        if (comment.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập lý do từ chối!", Alert.AlertType.WARNING);
            txtAdminComment.requestFocus(); // Focus on comment field
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận từ chối");
        confirmAlert.setHeaderText("Từ chối yêu cầu thay đổi thông tin");
        confirmAlert.setContentText("Bạn có chắc chắn muốn từ chối yêu cầu này?\n\n" +
                                   "Lý do từ chối: " + comment);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Disable buttons during processing
                btnReject.setDisable(true);
                btnApprove.setDisable(true);
                btnReject.setText("Đang xử lý...");
                
                int adminUserId = SessionManager.getInstance().getCurrentUser().getUserId();
                System.out.println("DEBUG: Rejecting request ID: " + selectedRequest.getRequestId() + 
                                 ", Admin ID: " + adminUserId + ", Comment: " + comment);
                
                boolean success = profileChangeRequestDAO.rejectRequest(selectedRequest.getRequestId(), adminUserId, comment);
                
                if (success) {
                    showAlert("Thành công", "Đã từ chối yêu cầu thay đổi thông tin!\n" +
                            "Lý do: " + comment, Alert.AlertType.INFORMATION);
                    txtAdminComment.clear(); // Clear comment after successful rejection
                    loadAllRequests();
                } else {
                    showAlert("Lỗi", "Không thể từ chối yêu cầu. Vui lòng kiểm tra:\n" +
                            "- Kết nối cơ sở dữ liệu\n" +
                            "- Trạng thái yêu cầu\n" +
                            "- Quyền truy cập", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Lỗi", "Có lỗi xảy ra khi từ chối: " + e.getMessage(), Alert.AlertType.ERROR);
            } finally {
                // Re-enable buttons  
                btnReject.setText("Từ chối");
                updateButtonStates(); // This will properly set button states
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadAllRequests();
    }
    
    // DEBUG: Method to force enable buttons for testing
    public void forceEnableButtons() {
        System.out.println("DEBUG: Force enabling buttons");
        btnApprove.setDisable(false);
        btnReject.setDisable(false);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    private void loadAllRequests() {
        try {
            List<ProfileChangeRequest> requests = profileChangeRequestDAO.getAllRequests();
            requestList.clear();
            requestList.addAll(requests);
            
            lblResultCount.setText("Tổng số yêu cầu: " + requests.size());
            
            // Clear selection and details
            tableView.getSelectionModel().clearSelection();
            selectedRequest = null;
            updateDetailsView();
            updateButtonStates();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải danh sách yêu cầu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}