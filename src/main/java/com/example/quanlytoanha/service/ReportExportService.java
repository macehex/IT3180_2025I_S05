// Vị trí: src/main/java/com/example/quanlytoanha/service/ReportExportService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.model.ApartmentDebt;
import com.example.quanlytoanha.model.VehicleAccessLog;
import com.example.quanlytoanha.model.VisitorLog;
import com.example.quanlytoanha.model.AssetReport;
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

    private static final DateTimeFormatter dtf_dmy = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter dtf_full = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private PDType0Font font;
    private PDType0Font fontBold;

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
     * (Đã sửa: truyền colWidths)
     */
    public File exportPopulationReport(Stage stage, LocalDate startDate, LocalDate endDate, Map<String, Integer> stats) throws IOException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo cáo Dân cư");
        fileChooser.setInitialFileName("BaoCaoDanCu_" + startDate.toString() + "_" + endDate.toString() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) throw new IOException("Người dùng đã hủy thao tác.");

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
                contentStream.showText("Từ ngày: " + startDate.format(dtf_dmy) + " đến ngày: " + endDate.format(dtf_dmy));
                contentStream.endText();

                float[] colWidths = {250, 100};

                drawTable(contentStream, 700, colWidths,
                        new String[]{"Loại Biến động", "Số lượng"},
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
     * (Đã sửa: truyền colWidths)
     */
    public File exportDebtReport(Stage stage, LocalDate startDate, LocalDate endDate, List<ApartmentDebt> data) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo cáo Công nợ");
        fileChooser.setInitialFileName("BaoCaoCongNo_" + startDate.toString() + "_" + endDate.toString() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) throw new IOException("Người dùng đã hủy thao tác.");

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
                contentStream.showText("Thời gian (theo ngày đến hạn): " + startDate.format(dtf_dmy) + " - " + endDate.format(dtf_dmy));
                contentStream.endText();

                String[] headers = {"Căn hộ", "Chủ hộ", "Điện thoại", "Ngày đến hạn", "Số tiền (VNĐ)"};
                float[] colWidths = {60, 150, 100, 100, 120};

                List<String[]> tableData = new java.util.ArrayList<>();
                for (ApartmentDebt debt : data) {
                    String dueDateStr = "N/A";
                    if (debt.getEarliestDueDate() != null) {
                        LocalDate dueDate = new java.sql.Date(debt.getEarliestDueDate().getTime()).toLocalDate();
                        dueDateStr = dtf_dmy.format(dueDate);
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

                drawTable(contentStream, 700, colWidths, headers, tableData);
            }
            document.save(file);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Lỗi khi lưu file PDF: " + e.getMessage());
        }
    }

    /**
     * Xuất Lịch sử Xe Ra/Vào
     * (SỬA LỖI: Cập nhật colWidths)
     */
    public File exportVehicleAccessLog(Stage stage, LocalDate startDate, LocalDate endDate, List<VehicleAccessLog> data) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Lịch sử Xe Ra/Vào");
        fileChooser.setInitialFileName("BaoCaoXeRaVao_" + startDate.toString() + "_" + endDate.toString() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) throw new IOException("Người dùng đã hủy thao tác.");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            loadFonts(document);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                contentStream.beginText();
                contentStream.setFont(fontBold, 18);
                contentStream.newLineAtOffset(180, 750);
                contentStream.showText("LỊCH SỬ XE RA/VÀO");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(180, 725);
                contentStream.showText("Từ ngày: " + startDate.format(dtf_dmy) + " đến ngày: " + endDate.format(dtf_dmy));
                contentStream.endText();

                String[] headers = {"Thời gian", "Loại", "Biển số", "Cư dân", "Ghi chú", "Bảo vệ"};

                // --- SỬA LỖI (US8_1_1): Điều chỉnh lại độ rộng các cột ---
                float[] colWidths = {110, 30, 80, 90, 140, 80}; // 6 cột

                List<String[]> tableData = new java.util.ArrayList<>();
                for (VehicleAccessLog log : data) {
                    tableData.add(new String[]{
                            log.getAccessTime() != null ? dtf_full.format(log.getAccessTime().toInstant().atZone(ZoneId.systemDefault())) : "N/A",
                            log.getAccessType(),
                            log.getLicensePlate(),
                            log.getResidentFullName() != null ? log.getResidentFullName() : "Khách",
                            log.getNotes(),
                            log.getGuardFullName()
                    });
                }

                drawTable(contentStream, 700, colWidths, headers, tableData);
            }
            document.save(file);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Lỗi khi lưu file PDF: " + e.getMessage());
        }
    }

    /**
     * Xuất Lịch sử Khách Ra/Vào
     * (Đã sửa: colWidths)
     */
    public File exportVisitorLog(Stage stage, LocalDate startDate, LocalDate endDate, List<VisitorLog> data) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Lịch sử Khách");
        fileChooser.setInitialFileName("BaoCaoKhachRaVao_" + startDate.toString() + "_" + endDate.toString() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) throw new IOException("Người dùng đã hủy thao tác.");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            loadFonts(document);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                contentStream.beginText();
                contentStream.setFont(fontBold, 18);
                contentStream.newLineAtOffset(180, 750);
                contentStream.showText("LỊCH SỬ KHÁCH RA/VÀO");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(180, 725);
                contentStream.showText("Từ ngày: " + startDate.format(dtf_dmy) + " đến ngày: " + endDate.format(dtf_dmy));
                contentStream.endText();

                String[] headers = {"Check-in", "Check-out", "Tên khách", "Căn hộ", "Lý do", "Bảo vệ"};
                float[] colWidths = {110, 110, 90, 40, 120, 90};

                List<String[]> tableData = new java.util.ArrayList<>();
                for (VisitorLog log : data) {
                    tableData.add(new String[]{
                            log.getCheckInTime() != null ? dtf_full.format(log.getCheckInTime().toInstant().atZone(ZoneId.systemDefault())) : "N/A",
                            log.getCheckOutTime() != null ? dtf_full.format(log.getCheckOutTime().toInstant().atZone(ZoneId.systemDefault())) : "(Đang ở)",
                            log.getVisitorName(),
                            log.getApartmentId() != null ? String.valueOf(log.getApartmentId()) : "",
                            log.getReason(),
                            log.getGuardFullName()
                    });
                }

                drawTable(contentStream, 700, colWidths, headers, tableData);
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
     * (Đã sửa lỗi chồng chữ)
     */
    private void drawTable(PDPageContentStream contentStream, float yStart, float[] colWidths, String[] headers, List<String[]> data) throws IOException {
        final float rowHeight = 20f;
        final float tableTop = yStart;
        final float margin = 30; // Giảm lề trái
        float nextY = tableTop;

        // --- 1. Vẽ Header ---
        contentStream.setFont(fontBold, 10);
        float currentX = margin;
        nextY -= rowHeight; // Dịch xuống 1 dòng để bắt đầu vẽ

        for (int i = 0; i < headers.length; i++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(currentX, nextY);
            contentStream.showText(headers[i]);
            contentStream.endText();
            currentX += colWidths[i]; // Di chuyển sang cột tiếp theo
        }
        nextY -= (rowHeight + 5); // Xuống dòng (thêm 5 padding)

        // --- 2. Vẽ Dữ liệu ---
        contentStream.setFont(font, 9);

        for (String[] row : data) {
            currentX = margin; // Reset X về lề trái
            for (int i = 0; i < row.length; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(currentX, nextY);

                String text = (row[i] != null) ? row[i] : "";

                // Logic Cắt bớt (Truncate)
                int maxChars = (int) (colWidths[i] / 4.5); // Ước tính
                if (text.length() > maxChars) {
                    text = text.substring(0, Math.max(0, maxChars - 3)) + "...";
                }

                contentStream.showText(text);
                contentStream.endText();

                currentX += colWidths[i]; // Di chuyển sang cột tiếp theo
            }
            nextY -= rowHeight; // Xuống dòng

            // (TODO: Logic qua trang mới nếu 'nextY' quá thấp)
        }
    }

    /**
     * Xuất Báo cáo Tài sản ra file PDF
     * Bao gồm: Báo cáo theo tình trạng, vị trí và chi phí bảo trì
     */
    public File exportAssetReport(Stage stage, com.example.quanlytoanha.model.AssetReport report) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu Báo cáo Tài sản");
        fileChooser.setInitialFileName("BaoCaoTaiSan_" + LocalDate.now().format(dtf_dmy) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) throw new IOException("Người dùng đã hủy thao tác.");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            loadFonts(document);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float yPosition = 750;
            final float rowHeight = 20f;
            final float sectionSpacing = 50f; // Tăng khoảng cách giữa các phần

            try {
                // --- Tiêu đề ---
                contentStream.beginText();
                contentStream.setFont(fontBold, 18);
                contentStream.newLineAtOffset(150, yPosition);
                contentStream.showText("BÁO CÁO TÀI SẢN");
                contentStream.endText();

                yPosition -= 40;
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(180, yPosition);
                contentStream.showText("Ngày xuất: " + LocalDate.now().format(dtf_dmy));
                contentStream.endText();

                yPosition -= sectionSpacing;

                // --- Tổng quan ---
                contentStream.beginText();
                contentStream.setFont(fontBold, 14);
                contentStream.newLineAtOffset(30, yPosition);
                contentStream.showText("TỔNG QUAN");
                contentStream.endText();
                yPosition -= 35;

                List<String[]> overviewData = List.of(
                    new String[]{"Tổng số tài sản", String.valueOf(report.getTotalAssets())},
                    new String[]{"Tổng giá trị ban đầu", String.format("%,.0f VNĐ", report.getTotalInitialCost().doubleValue())},
                    new String[]{"Tổng chi phí bảo trì", String.format("%,.0f VNĐ", report.getTotalMaintenanceCost().doubleValue())}
                );
                drawTable(contentStream, yPosition, new float[]{200, 200}, 
                    new String[]{"Chỉ tiêu", "Giá trị"}, overviewData);
                // Tính toán khoảng cách sau bảng: header (1 row) + data rows + padding
                yPosition -= (overviewData.size() * rowHeight + rowHeight + 25);
                yPosition -= sectionSpacing;

                // --- Báo cáo theo Tình trạng ---
                if (report.getStatusCounts() != null && !report.getStatusCounts().isEmpty()) {
                    // Kiểm tra nếu còn đủ không gian, nếu không thì tạo trang mới
                    if (yPosition < 150) {
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        loadFonts(document);
                        yPosition = 750;
                    }
                    
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 14);
                    contentStream.newLineAtOffset(30, yPosition);
                    contentStream.showText("BÁO CÁO THEO TÌNH TRẠNG");
                    contentStream.endText();
                    yPosition -= 35;

                    List<String[]> statusData = new java.util.ArrayList<>();
                    for (Map.Entry<String, Integer> entry : report.getStatusCounts().entrySet()) {
                        statusData.add(new String[]{entry.getKey(), String.valueOf(entry.getValue())});
                    }
                    drawTable(contentStream, yPosition, new float[]{200, 100}, 
                        new String[]{"Tình trạng", "Số lượng"}, statusData);
                    yPosition -= (statusData.size() * rowHeight + rowHeight + 25);
                    yPosition -= sectionSpacing;
                }

                // --- Báo cáo theo Vị trí ---
                if (report.getLocationCounts() != null && !report.getLocationCounts().isEmpty()) {
                    if (yPosition < 150) {
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        loadFonts(document);
                        yPosition = 750;
                    }
                    
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 14);
                    contentStream.newLineAtOffset(30, yPosition);
                    contentStream.showText("BÁO CÁO THEO VỊ TRÍ");
                    contentStream.endText();
                    yPosition -= 35;

                    List<String[]> locationData = new java.util.ArrayList<>();
                    for (Map.Entry<String, Integer> entry : report.getLocationCounts().entrySet()) {
                        locationData.add(new String[]{entry.getKey(), String.valueOf(entry.getValue())});
                    }
                    drawTable(contentStream, yPosition, new float[]{200, 100}, 
                        new String[]{"Vị trí", "Số lượng"}, locationData);
                    yPosition -= (locationData.size() * rowHeight + rowHeight + 25);
                    yPosition -= sectionSpacing;
                }

                // --- Chi phí bảo trì theo Vị trí ---
                if (report.getMaintenanceCostByLocation() != null && !report.getMaintenanceCostByLocation().isEmpty()) {
                    if (yPosition < 150) {
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        loadFonts(document);
                        yPosition = 750;
                    }
                    
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 14);
                    contentStream.newLineAtOffset(30, yPosition);
                    contentStream.showText("CHI PHÍ BẢO TRÌ THEO VỊ TRÍ");
                    contentStream.endText();
                    yPosition -= 35;

                    List<String[]> costByLocationData = new java.util.ArrayList<>();
                    for (Map.Entry<String, BigDecimal> entry : report.getMaintenanceCostByLocation().entrySet()) {
                        costByLocationData.add(new String[]{
                            entry.getKey(), 
                            String.format("%,.0f VNĐ", entry.getValue().doubleValue())
                        });
                    }
                    drawTable(contentStream, yPosition, new float[]{200, 200}, 
                        new String[]{"Vị trí", "Tổng chi phí"}, costByLocationData);
                    yPosition -= (costByLocationData.size() * rowHeight + rowHeight + 25);
                    yPosition -= sectionSpacing;
                }

                // --- Chi phí bảo trì theo Tình trạng ---
                if (report.getMaintenanceCostByStatus() != null && !report.getMaintenanceCostByStatus().isEmpty()) {
                    if (yPosition < 150) {
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        loadFonts(document);
                        yPosition = 750;
                    }
                    
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 14);
                    contentStream.newLineAtOffset(30, yPosition);
                    contentStream.showText("CHI PHÍ BẢO TRÌ THEO TÌNH TRẠNG");
                    contentStream.endText();
                    yPosition -= 35;

                    List<String[]> costByStatusData = new java.util.ArrayList<>();
                    for (Map.Entry<String, BigDecimal> entry : report.getMaintenanceCostByStatus().entrySet()) {
                        costByStatusData.add(new String[]{
                            entry.getKey(), 
                            String.format("%,.0f VNĐ", entry.getValue().doubleValue())
                        });
                    }
                    drawTable(contentStream, yPosition, new float[]{200, 200}, 
                        new String[]{"Tình trạng", "Tổng chi phí"}, costByStatusData);
                }
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }
            document.save(file);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Lỗi khi lưu file PDF: " + e.getMessage(), e);
        }
    }
}