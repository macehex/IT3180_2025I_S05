// Vị trí: src/main/java/com/example/quanlytoanha/controller/DebtDetailController.java
package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.ApartmentDebt;
import com.example.quanlytoanha.model.Invoice;
import com.example.quanlytoanha.model.InvoiceDetail;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

public class DebtDetailController {

    @FXML private VBox mainVBox;
    @FXML private VBox invoiceVBox;
    @FXML private Label lblHeader;
    @FXML private Label lblSubHeader;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Phương thức này được gọi từ AccountantDashboardController để truyền dữ liệu sang.
     */
    public void setData(ApartmentDebt summary, List<Invoice> invoiceList) {
        // Cập nhật tiêu đề
        lblHeader.setText("Chi tiết nợ cho Căn hộ: " + summary.getApartmentId() + " (" + summary.getOwnerName() + ")");
        lblSubHeader.setText(String.format("Tổng nợ: %,.0f VNĐ (%d hóa đơn)", summary.getTotalDue(), summary.getUnpaidCount()));

        // Xóa mọi nội dung cũ (nếu có)
        mainVBox.getChildren().remove(2, mainVBox.getChildren().size());
        invoiceVBox.getChildren().clear();

        // Tạo TitledPane cho mỗi hóa đơn
        for (Invoice invoice : invoiceList) {
            TitledPane pane = createInvoicePane(invoice);
            mainVBox.getChildren().add(pane);
        }
    }

    /**
     * Tạo một TitledPane chứa TableView cho một hóa đơn
     */
    private TitledPane createInvoicePane(Invoice invoice) {
        // 1. Tạo tiêu đề
        String title = String.format("HĐ #%d - Hạn: %s - Tổng: %,.0f VNĐ",
                invoice.getInvoiceId(),
                dateFormat.format(invoice.getDueDate()),
                invoice.getTotalAmount());

        // 2. Tạo bảng (TableView)
        TableView<InvoiceDetail> detailTable = new TableView<>();

        // 3. Tạo các cột
        TableColumn<InvoiceDetail, String> nameCol = new TableColumn<>("Tên phí");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);

        TableColumn<InvoiceDetail, BigDecimal> amountCol = new TableColumn<>("Số tiền");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(150);
        // (Có thể thêm định dạng tiền tệ cho cột này)

        detailTable.getColumns().add(nameCol);
        detailTable.getColumns().add(amountCol);

        // 4. Đổ dữ liệu vào bảng
        detailTable.getItems().setAll(invoice.getDetails());

        int rowCount = invoice.getDetails().size();
        if (rowCount == 0) {
            detailTable.setPlaceholder(new Label("Hóa đơn này chưa có chi tiết phí."));
        }

        // 1. Tính toán chiều cao cần thiết
        // (Chiều cao 1 dòng * số dòng) + (Chiều cao của Header) + (Padding)
        // (Giả sử -fx-cell-size là 35px, header ~30px, padding 10px)
        double tableHeight = (rowCount * 35) + 30 + 10;

        // 2. Đặt chiều cao CỐ ĐỊNH cho TableView
        // Việc này ngăn TableView bị "nén" (squish)
        detailTable.setPrefHeight(tableHeight);

        // 5. Tạo TitledPane
        TitledPane titledPane = new TitledPane(title, detailTable);
        titledPane.setExpanded(true); // Mặc định mở
        return titledPane;
    }
}