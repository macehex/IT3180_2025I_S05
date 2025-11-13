// Vị trí: src/main/java/com/example/quanlytoanha/controller/ResidentHistoryController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.dao.ResidentHistoryDAO;
import com.example.quanlytoanha.model.ResidentHistory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.util.List;

public class ResidentHistoryController {

    @FXML private Label lblHeaderTitle;
    @FXML private TableView<ResidentHistory> historyTable;

    private ResidentHistoryDAO historyDAO;
    private int residentId; // ID cư dân được truyền vào

    @FXML
    public void initialize() {
        this.historyDAO = new ResidentHistoryDAO();
    }

    // Hàm này được gọi từ màn hình Danh sách Cư dân (Resident List)
    public void setResidentData(int residentId, String residentName) {
        this.residentId = residentId;
        lblHeaderTitle.setText("LỊCH SỬ THAY ĐỔI: " + residentName.toUpperCase());

        // Tải dữ liệu
        Platform.runLater(this::loadHistory);
    }

    @FXML
    private void loadHistory() {
        if (residentId == 0) return; // Chưa có ID

        try {
            List<ResidentHistory> history = historyDAO.getHistoryForResident(residentId);
            historyTable.setItems(FXCollections.observableArrayList(history));
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Lỗi khi tải lịch sử.").showAndWait();
        }
    }
}