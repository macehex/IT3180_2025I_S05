// Vị trí: src/main/java/com/example/quanlytoanha/service/ReportExportService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.model.ApartmentDebt;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportExportService {

    public static final String FONT_PATH_CLASSPATH = "/fonts/DejaVuSans.ttf";

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PDType0Font font;
    private PDType0Font fontBold;

    /**
     * Tải font (SỬA LỖI: Tải từ Classpath)
     */
    private void loadFonts(PDDocument document) throws IOException {
        try (InputStream fontStream = getClass().getResourceAsStream(FONT_PATH_CLASSPATH)) {

            if (fontStream == null) {
                throw new IOException("LỖI FONT: Không tìm thấy file '" + FONT_PATH_CLASSPATH + "'. Hãy đảm bảo file font nằm trong 'src/main/resources/fonts/'");
            }

            font = PDType0Font.load(document, fontStream, false);
            fontBold = font;

        } catch (IOException e) {
            System.err.println("Lỗi nghiêm trọng khi tải font: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Xuất Báo cáo Biến động Dân cư (dùng Apache PDFBox)
     * (SỬA LỖI: Cung cấp đúng chiều rộng cột)
     */
    public File exportPopulationReport(Stage stage, LocalDate startDate, LocalDate endDate, Map<String, Integer> stats) throws IOException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo cáo Dân cư");
        fileChooser.setInitialFileName("BaoCaoDanCu_" + startDate.toString() + "_" + endDate.toString() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) {
            throw new IOException("Người dùng đã hủy thao tác.");
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            loadFonts(document);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                contentStream.beginText();
                contentStream.setFont(fontBold, 18);
                contentStream.newLineAtOffset(150, 750);
                contentStream.showText("BÁO CÁO BIẾN ĐỘNG DÂN CƯ");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(180, 725);
                contentStream.showText("Từ ngày: " + startDate.format(dtf) + " đến ngày: " + endDate.format(dtf));
                contentStream.endText();

                // --- SỬA LỖI (US7_2_1): Định nghĩa chiều rộng cột cho Báo cáo Dân cư ---
                float[] colWidths = {250, 100}; // Cột 1 rộng 250, Cột 2 rộng 100

                drawTable(contentStream, 700, colWidths, // Truyền colWidths
                        new String[]{"Loại Biến động", "Số lượng"}, // Header
                        List.of(
                                new String[]{"Số lượng cư dân chuyển vào", stats.getOrDefault("moveIns", 0).toString()},
                                new String[]{"Số lượng cư dân chuyển đi", stats.getOrDefault("moveOuts", 0).toString()}
                        )
                );
            }

            document.save(file);
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Lỗi khi lưu file PDF: " + e.getMessage());
        }
    }

    /**
     * Xuất Báo cáo Công nợ ra file PDF
     * (SỬA LỖI: Cung cấp đúng chiều rộng cột)
     */
    public File exportDebtReport(Stage stage, LocalDate startDate, LocalDate endDate, List<ApartmentDebt> data) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo cáo Công nợ");
        fileChooser.setInitialFileName("BaoCaoCongNo_" + startDate.toString() + "_" + endDate.toString() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) {
            throw new IOException("Người dùng đã hủy thao tác.");
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            loadFonts(document);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                contentStream.beginText();
                contentStream.setFont(fontBold, 18);
                contentStream.newLineAtOffset(180, 750);
                contentStream.showText("BÁO CÁO CÔNG NỢ CHI TIẾT");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(160, 725);
                contentStream.showText("Thời gian (theo ngày đến hạn): " + startDate.format(dtf) + " - " + endDate.format(dtf));
                contentStream.endText();

                String[] headers = {"Căn hộ", "Chủ hộ", "Điện thoại", "Ngày đến hạn", "Số tiền (VNĐ)"};

                // --- SỬA LỖI (US7_2_1): Định nghĩa chiều rộng cột cho Báo cáo Công nợ ---
                float[] colWidths = {60, 150, 100, 100, 120}; // (Giữ nguyên như cũ)

                List<String[]> tableData = new java.util.ArrayList<>();
                for (ApartmentDebt debt : data) {

                    String dueDateStr = "N/A";
                    if (debt.getEarliestDueDate() != null) {
                        LocalDate dueDate = new java.sql.Date(debt.getEarliestDueDate().getTime()).toLocalDate();
                        dueDateStr = dtf.format(dueDate);
                    }

                    String totalDueStr = "0";
                    if (debt.getTotalDue() != null) {
                        totalDueStr = String.format("%,.0f", debt.getTotalDue());
                    }

                    tableData.add(new String[]{
                            String.valueOf(debt.getApartmentId()),
                            debt.getOwnerName(),
                            debt.getPhoneNumber() != null ? debt.getPhoneNumber() : "",
                            dueDateStr,
                            totalDueStr
                    });
                }

                drawTable(contentStream, 700, colWidths, headers, tableData); // Truyền colWidths
            }

            document.save(file);
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Lỗi khi lưu file PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Hàm tiện ích (đơn giản) để vẽ Bảng bằng PDFBox
     * (SỬA LỖI: Chấp nhận colWidths)
     */
    private void drawTable(PDPageContentStream contentStream, float yStart, float[] colWidths, String[] headers, List<String[]> data) throws IOException {
        final float rowHeight = 20f;
        final float tableTop = yStart;
        final float margin = 50;
        // (Xóa mảng colWidths cố định ở đây)

        float nextX = margin;
        float nextY = tableTop;
        contentStream.setFont(fontBold, 10);

        contentStream.beginText();
        contentStream.newLineAtOffset(nextX, nextY);
        for (int i = 0; i < headers.length; i++) {
            contentStream.showText(headers[i]);
            // nextX += colWidths[i]; // (Không cần theo dõi nextX)
            contentStream.newLineAtOffset(colWidths[i], 0); // Di chuyển sang phải theo độ rộng
        }
        contentStream.endText();
        nextY -= rowHeight;

        contentStream.setFont(font, 9);
        for (String[] row : data) {
            nextX = margin;
            contentStream.beginText();
            contentStream.newLineAtOffset(nextX, nextY);
            for (int i = 0; i < row.length; i++) {
                String text = (row[i] != null) ? row[i] : "";
                if (text.length() > 30) text = text.substring(0, 27) + "...";

                contentStream.showText(text);
                // nextX += colWidths[i]; // (Không cần theo dõi nextX)
                contentStream.newLineAtOffset(colWidths[i], 0);
            }
            contentStream.endText();
            nextY -= rowHeight;
        }
    }
}