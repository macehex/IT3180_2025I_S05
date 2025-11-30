package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.dao.ServiceRequestDAO;
import com.example.quanlytoanha.model.ServiceRequest;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminRequestListController implements Initializable {

    @FXML
    private TableView<ServiceRequest> requestTable;

    // --- KHAI BÁO CÁC CỘT (Cần đặt fx:id tương ứng bên file FXML) ---
    @FXML private TableColumn<ServiceRequest, Integer> colId;
    @FXML private TableColumn<ServiceRequest, Integer> colUserId;
    @FXML private TableColumn<ServiceRequest, String> colType;
    @FXML private TableColumn<ServiceRequest, String> colTitle;
    @FXML private TableColumn<ServiceRequest, String> colStatus;
    @FXML private TableColumn<ServiceRequest, String> colDate;

    private ServiceRequestDAO serviceRequestDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceRequestDAO = new ServiceRequestDAO();

        // 1. Cấu hình cột (Mapping dữ liệu từ Model vào Bảng)
        setupColumns();

        // 2. Tải dữ liệu từ Database
        loadData();

        // 3. Tạo Menu chuột phải để cập nhật trạng thái
        setupContextMenu();
    }

    /**
     * Liên kết các cột trong bảng với các thuộc tính trong file ServiceRequest.java
     */
    private void setupColumns() {
        // Lưu ý: Chuỗi trong ngoặc kép phải khớp chính xác tên biến trong ServiceRequest.java
        // Ví dụ: private int requestId; -> "requestId"

        // Cách 1: Nếu bạn đã đặt fx:id cho cột bên FXML
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        if (colUserId != null) colUserId.setCellValueFactory(new PropertyValueFactory<>("reqUserId"));
        if (colType != null) colType.setCellValueFactory(new PropertyValueFactory<>("reqType"));
        if (colTitle != null) colTitle.setCellValueFactory(new PropertyValueFactory<>("reqTitle"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (colDate != null) colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    }

    private void loadData() {
        // Gọi hàm lấy TOÀN BỘ dữ liệu từ DAO
        List<ServiceRequest> allRequests = serviceRequestDAO.getAllServiceRequests();
        ObservableList<ServiceRequest> observableList = FXCollections.observableArrayList(allRequests);
        requestTable.setItems(observableList);
    }

    /**
     * Thiết lập menu chuột phải (Context Menu) cho từng dòng
     */
    private void setupContextMenu() {
        // Tạo Menu
        ContextMenu contextMenu = new ContextMenu();

        // Tạo các lựa chọn
        MenuItem itemProcessing = new MenuItem("Đánh dấu: Đang xử lý (IN_PROGRESS)");
        MenuItem itemCompleted = new MenuItem("Đánh dấu: Đã hoàn thành (COMPLETED)");
        MenuItem itemCancelled = new MenuItem("Đánh dấu: Hủy bỏ (CANCELLED)");

        // Gắn sự kiện khi chọn menu -> Gọi hàm update
        itemProcessing.setOnAction(e -> updateStatusForSelected("IN_PROGRESS"));
        itemCompleted.setOnAction(e -> updateStatusForSelected("COMPLETED"));
        itemCancelled.setOnAction(e -> updateStatusForSelected("CANCELLED"));

        // Thêm item vào menu
        contextMenu.getItems().addAll(itemProcessing, itemCompleted, itemCancelled);

        // Gắn menu vào từng dòng (Row) của bảng
        requestTable.setRowFactory(tv -> {
            TableRow<ServiceRequest> row = new TableRow<>();

            // Chỉ hiện menu khi dòng đó có dữ liệu (không phải dòng trống)
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });
    }

    /**
     * Hàm gọi DAO để cập nhật trạng thái xuống Database
     */
    private void updateStatusForSelected(String newStatus) {
        // Lấy dòng đang được chọn
        ServiceRequest selectedRequest = requestTable.getSelectionModel().getSelectedItem();

        if (selectedRequest != null) {
            // Gọi DAO (Hàm này bạn đã thêm ở bước trước)
            boolean success = serviceRequestDAO.updateRequestStatus(selectedRequest.getRequestId(), newStatus);

            if (success) {
                // Load lại dữ liệu để bảng cập nhật trạng thái mới
                loadData();

                // Hiển thị thông báo thành công
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đã cập nhật yêu cầu #" + selectedRequest.getRequestId() + " sang trạng thái: " + newStatus);
                alert.show();
            } else {
                // Báo lỗi
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setContentText("Không thể cập nhật trạng thái. Vui lòng kiểm tra lại.");
                alert.show();
            }
        } else {
            // Trường hợp chưa chọn dòng nào mà bấm (ít xảy ra với context menu)
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Chưa chọn");
            alert.setContentText("Vui lòng chọn một yêu cầu để cập nhật.");
            alert.show();
        }
    }
}