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

        // Xóa mọi nội dung cũ trong invoiceVBox
        invoiceVBox.getChildren().clear();

        // Tạo TitledPane cho mỗi hóa đơn và thêm vào invoiceVBox (trong ScrollPane)
        for (Invoice invoice : invoiceList) {
            TitledPane pane = createInvoicePane(invoice);
            invoiceVBox.getChildren().add(pane);
        }
    }

    /**
     * Tạo một TitledPane chứa TableView cho một hóa đơn
     */
    private TitledPane createInvoicePane(Invoice invoice) {
        // 1. Tạo tiêu đề
        String title = String.format("📄 HĐ #%d - Hạn: %s - Tổng: %,.0f VNĐ",
                invoice.getInvoiceId(),
                dateFormat.format(invoice.getDueDate()),
                invoice.getTotalAmount());

        // 2. Tạo bảng (TableView)
        TableView<InvoiceDetail> detailTable = new TableView<>();
        detailTable.setStyle("-fx-background-color: #ffffff; -fx-border-color: #3d6ba8; -fx-border-width: 2px;");

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
        
        // Style cho các dòng trong bảng
        detailTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<InvoiceDetail> row = new javafx.scene.control.TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    int index = row.getIndex();
                    if (index % 2 == 0) {
                        row.setStyle("-fx-background-color: #ffffff;");
                    } else {
                        row.setStyle("-fx-background-color: #e8f5e9;");
                    }
                }
            });
            return row;
        });

        int rowCount = invoice.getDetails().size();
        if (rowCount == 0) {
            Label placeholder = new Label("Hóa đơn này chưa có chi tiết phí.");
            placeholder.setStyle("-fx-text-fill: #21468B; -fx-font-style: italic;");
            detailTable.setPlaceholder(placeholder);
        }

        // 1. Tính toán chiều cao cần thiết
        // (Chiều cao 1 dòng * số dòng) + (Chiều cao của Header) + (Padding)
        // (Giả sử -fx-cell-size là 35px, header ~30px, padding 10px)
        double tableHeight = (rowCount * 35) + 30 + 10;

        // 2. Đặt chiều cao CỐ ĐỊNH cho TableView
        // Việc này ngăn TableView bị "nén" (squish)
        detailTable.setPrefHeight(tableHeight);

        // 5. Tạo TitledPane với style xanh đậm
        TitledPane titledPane = new TitledPane(title, detailTable);
        titledPane.setExpanded(true); // Mặc định mở
        titledPane.setStyle("-fx-background-color: #ffffff; " +
                           "-fx-border-color: #21468B; " +
                           "-fx-border-width: 2px; " +
                           "-fx-border-radius: 5px; " +
                           "-fx-text-fill: #21468B;" +
                           "-fx-font-weight: bold;");
        return titledPane;
    }
}