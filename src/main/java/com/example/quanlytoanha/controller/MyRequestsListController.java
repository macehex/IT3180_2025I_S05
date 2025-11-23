// VỊ TRÍ: src/main/java/com/example/quanlytoanha/controller/MyRequestsListController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.ServiceRequest;
import com.example.quanlytoanha.service.ServiceRequestService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Date;
import java.util.List;

public class MyRequestsListController {

    // --- Khai báo các thành phần UI từ FXML ---
    @FXML
    private TableView<ServiceRequest> requestTable;
    @FXML
    private TableColumn<ServiceRequest, Integer> idColumn;
    @FXML
    private TableColumn<ServiceRequest, String> titleColumn;
    @FXML
    private TableColumn<ServiceRequest, String> typeColumn;
    @FXML
    private TableColumn<ServiceRequest, String> statusColumn;
    @FXML
    private TableColumn<ServiceRequest, Date> createdDateColumn;

    // --- Service ---
    private ServiceRequestService requestService;

    // --- Dữ liệu ---
    private int currentResidentId;
    private ObservableList<ServiceRequest> requestObservableList;

    /**
     * Hàm này được tự động gọi sau khi FXML đã được load xong.
     */
    @FXML
    public void initialize() {
        this.requestService = new ServiceRequestService();

        // 1. Liên kết các cột với các thuộc tính của ServiceRequest
        // (Cách này tường minh hơn là chỉ dùng FXML)
        idColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("reqTitle"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("reqType"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    }

    /**
     * Hàm này phải được gọi từ bên ngoài (ví dụ: từ Dashboard)
     * để truyền ID của Cư dân và tải dữ liệu.
     */
    public void loadDataForResident(int residentId) {
        this.currentResidentId = residentId;

        // 2. Gọi Service để lấy dữ liệu
        List<ServiceRequest> requestsFromDB = requestService.getRequestsForUser(this.currentResidentId);

        // 3. Chuyển List thông thường sang ObservableList
        // TableView của JavaFX cần ObservableList để tự động cập nhật
        requestObservableList = FXCollections.observableArrayList(requestsFromDB);

        // 4. Đổ dữ liệu vào TableView
        requestTable.setItems(requestObservableList);
    }
}
