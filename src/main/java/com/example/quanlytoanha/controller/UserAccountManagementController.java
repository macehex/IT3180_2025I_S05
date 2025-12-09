package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Role;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.dao.ResidentDAO;
import com.example.quanlytoanha.dao.ApartmentDAO;
import com.example.quanlytoanha.service.UserAccountService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class UserAccountManagementController {

    // ==========================================================
    // TAB 1: QUẢN LÝ TÀI KHOẢN NGƯỜI DÙNG
    // ==========================================================
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhoneNumber;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colCreatedAt;
    @FXML private TableColumn<User, String> colLastLogin;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtEmail;
    @FXML private TextField txtFullName;
    @FXML private TextField txtPhoneNumber;
    @FXML private ComboBox<Role> comboRole;

    @FXML private Button btnAddUser;
    @FXML private Button btnUpdateUser;
    @FXML private Button btnDeleteUser;
    @FXML private Button btnResetPassword;
    @FXML private Button btnRefresh;

    private UserAccountService userAccountService;
    private ResidentDAO residentDAO;
    private ApartmentDAO apartmentDAO;
    private ObservableList<User> userList;
    private User selectedUser;

    @FXML
    public void initialize() {
        this.userAccountService = new UserAccountService();
        this.residentDAO = new ResidentDAO();
        this.apartmentDAO = new ApartmentDAO();
        this.userList = FXCollections.observableArrayList();

        // Cấu hình bảng User
        setupUserTable();
        
        // Cấu hình form User
        setupUserForm();

        // Tải dữ liệu ban đầu
        loadUserData();
    }

    private void setupUserTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colRole.setCellValueFactory(entry -> {
            Role role = entry.getValue().getRole();
            return new javafx.beans.property.SimpleStringProperty(role != null ? role.getRoleName() : "");
        });
        colCreatedAt.setCellValueFactory(entry -> {
            Timestamp createdAt = entry.getValue().getCreatedAt();
            if (createdAt != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                return new javafx.beans.property.SimpleStringProperty(sdf.format(createdAt));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colLastLogin.setCellValueFactory(entry -> {
            Timestamp lastLogin = entry.getValue().getLastLogin();
            if (lastLogin != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                return new javafx.beans.property.SimpleStringProperty(sdf.format(lastLogin));
            }
            return new javafx.beans.property.SimpleStringProperty("Chưa đăng nhập");
        });

        userTable.setItems(userList);
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedUser = newVal;
            if (newVal != null) {
                fillUserForm(newVal);
            } else {
                clearUserForm();
            }
        });

        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupUserForm() {
        // Điền danh sách Role vào ComboBox
        comboRole.setItems(FXCollections.observableArrayList(Role.values()));
        comboRole.setConverter(new javafx.util.StringConverter<Role>() {
            @Override
            public String toString(Role role) {
                return role != null ? role.getRoleName() : "";
            }

            @Override
            public Role fromString(String string) {
                return null;
            }
        });

        // Ẩn nút Update và Delete ban đầu
        btnUpdateUser.setDisable(true);
        btnDeleteUser.setDisable(true);
        btnResetPassword.setDisable(true);
    }

    private void fillUserForm(User user) {
        txtUsername.setText(user.getUsername());
        txtUsername.setDisable(true); // Không cho sửa username
        txtEmail.setText(user.getEmail());
        txtFullName.setText(user.getFullName());
        txtPhoneNumber.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        comboRole.setValue(user.getRole());
        txtPassword.clear();

        btnAddUser.setDisable(true);
        btnUpdateUser.setDisable(false);
        btnDeleteUser.setDisable(false); // Cho phép xóa mọi user (Resident sẽ xóa kèm hồ sơ)
        btnResetPassword.setDisable(false);
    }

    private void clearUserForm() {
        txtUsername.clear();
        txtUsername.setDisable(false);
        txtPassword.clear();
        txtEmail.clear();
        txtFullName.clear();
        txtPhoneNumber.clear();
        comboRole.setValue(null);

        btnAddUser.setDisable(false);
        btnUpdateUser.setDisable(true);
        btnDeleteUser.setDisable(true);
        btnResetPassword.setDisable(true);
    }

    @FXML
    private void handleAddUser() {
        try {
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText();
            String email = txtEmail.getText().trim();
            String fullName = txtFullName.getText().trim();
            String phoneNumberInput = txtPhoneNumber.getText().trim();
            String phoneNumber = phoneNumberInput.isEmpty() ? null : phoneNumberInput;
            Role role = comboRole.getValue();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || role == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền đầy đủ thông tin bắt buộc.");
                return;
            }

            int userId = userAccountService.createUser(username, password, email, fullName, phoneNumber, role);
            if (userId > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã tạo tài khoản thành công!");
                clearUserForm();
                loadUserData();
            }

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân quyền", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể tạo tài khoản: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateUser() {
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một người dùng để cập nhật.");
            return;
        }

        try {
            String email = txtEmail.getText().trim();
            String fullName = txtFullName.getText().trim();
            String phoneNumberInput = txtPhoneNumber.getText().trim();
            String phoneNumber = phoneNumberInput.isEmpty() ? null : phoneNumberInput;
            Role role = comboRole.getValue();

            if (email.isEmpty() || role == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền đầy đủ thông tin bắt buộc.");
                return;
            }

            boolean success = userAccountService.updateUser(selectedUser.getUserId(), email, fullName, phoneNumber, role);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật tài khoản thành công!");
                loadUserData();
            }

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân quyền", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể cập nhật tài khoản: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteUser() {
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một người dùng để xóa.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Xóa tài khoản người dùng");
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa tài khoản:\n\n" +
                "Tên đăng nhập: " + selectedUser.getUsername() + "\n" +
                "Họ tên: " + selectedUser.getFullName() + "\n\n" +
                "⚠️ Hành động này không thể hoàn tác!");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success;

                    // Nếu là cư dân: xóa hồ sơ cư dân (nếu có) rồi luôn xóa tài khoản (force)
                    if (selectedUser.getRole() == Role.RESIDENT) {
                        try {
                            // Gỡ chủ hộ khỏi các căn hộ mà user này đang là owner để tránh FK
                            apartmentDAO.clearOwnerByUserId(selectedUser.getUserId());
                            residentDAO.removeResidentByUserId(selectedUser.getUserId());
                        } catch (SQLException ignore) {
                            // Không tìm thấy cư dân thì bỏ qua, vẫn xóa user
                        }
                        success = userAccountService.deleteUserForce(selectedUser.getUserId());
                    } else {
                        success = userAccountService.deleteUser(selectedUser.getUserId());
                    }

                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa tài khoản thành công!");
                        clearUserForm();
                        loadUserData();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa tài khoản. Kiểm tra ràng buộc dữ liệu hoặc thử lại.");
                    }
                } catch (SecurityException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Phân quyền", e.getMessage());
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể xóa tài khoản: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleResetPassword() {
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một người dùng để reset mật khẩu.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Mật khẩu");
        dialog.setHeaderText("Reset mật khẩu cho: " + selectedUser.getUsername());
        dialog.setContentText("Nhập mật khẩu mới:");

        dialog.showAndWait().ifPresent(newPassword -> {
            if (newPassword.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mật khẩu không được để trống.");
                return;
            }

            try {
                boolean success = userAccountService.resetUserPassword(selectedUser.getUserId(), newPassword);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã reset mật khẩu thành công!");
                }
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
            } catch (SecurityException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Phân quyền", e.getMessage());
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể reset mật khẩu: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadUserData();
    }

    private void loadUserData() {
        try {
            List<User> users = userAccountService.getAllUsers();
            userList.clear();
            userList.addAll(users);
        } catch (SecurityException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Phân quyền", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể tải danh sách người dùng: " + e.getMessage());
            e.printStackTrace();
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

