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
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.List;

public class ResidentHistoryController {

    @FXML private Label lblHeaderTitle;
    @FXML private TableView<ResidentHistory> historyTable;

    private ResidentHistoryDAO historyDAO;
    private int residentId; // ID cư dân được truyền vào

    @FXML
    public void initialize() {
        this.historyDAO = new ResidentHistoryDAO();

        historyTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Kiểm tra click 2 lần
                ResidentHistory selectedItem = historyTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    showDetailDialog(selectedItem);
                }
            }
        });
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

    private void showDetailDialog(ResidentHistory history) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết Thay đổi");
        alert.setHeaderText("Người thực hiện: " + history.getChangedByUserFullName() +
                "\nThời điểm: " + history.getChangedAt());

        // Tạo layout để hiển thị dữ liệu cũ và mới
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label lblOld = new Label("Dữ liệu Cũ:");
        TextArea txtOld = new TextArea(history.getOldData());
        txtOld.setEditable(false);
        txtOld.setWrapText(true);
        txtOld.setMaxWidth(Double.MAX_VALUE);
        txtOld.setMaxHeight(Double.MAX_VALUE);

        Label lblNew = new Label("Dữ liệu Mới:");
        TextArea txtNew = new TextArea(history.getNewData());
        txtNew.setEditable(false);
        txtNew.setWrapText(true);
        txtNew.setMaxWidth(Double.MAX_VALUE);
        txtNew.setMaxHeight(Double.MAX_VALUE);

        // Sắp xếp vào Grid
        grid.add(lblOld, 0, 0);
        grid.add(txtOld, 0, 1);
        grid.add(lblNew, 1, 0);
        grid.add(txtNew, 1, 1);

        GridPane.setVgrow(txtOld, Priority.ALWAYS);
        GridPane.setHgrow(txtOld, Priority.ALWAYS);
        GridPane.setVgrow(txtNew, Priority.ALWAYS);
        GridPane.setHgrow(txtNew, Priority.ALWAYS);

        alert.getDialogPane().setContent(grid);
        alert.getDialogPane().setPrefSize(800, 500); // Kích thước to rõ
        alert.showAndWait();
    }
}