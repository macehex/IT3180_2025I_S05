package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.model.Role;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class ResidentListController implements Initializable {

    // Phần Thống kê (Top)
    @FXML private Label lblTotalResidents;
    @FXML private Label lblTotalApartments;

    // Phần Chức năng (Center)
    @FXML private TextField txtSearch;
    // @FXML private Button btnAddResident; // Nút này không có trong FXML mới, nhưng tôi để lại phòng trường hợp bạn thêm vào

    // THAY ĐỔI: Chuyển từ ListView sang TableView
    @FXML private TableView<Resident> residentTableView;
    @FXML private TableColumn<Resident, Integer> colResidentId;
    @FXML private TableColumn<Resident, String> colFullName;
    @FXML private TableColumn<Resident, Integer> colApartment;
    @FXML private TableColumn<Resident, String> colPhone;

    // Phần Phân trang (Bottom)
    @FXML private Button btnPreviousPage;
    @FXML private Label lblPaginationInfo;
    @FXML private Button btnNextPage;

    // --- Biến quản lý dữ liệu ---

    // Danh sách gốc chứa TẤT CẢ cư dân
    private ObservableList<Resident> masterResidentList = FXCollections.observableArrayList();

    // Danh sách đã được lọc (dựa trên tìm kiếm)
    private FilteredList<Resident> filteredData;

    // Danh sách đã được sắp xếp (dựa trên click cột)
    private SortedList<Resident> sortedData;

    // Biến cho phân trang
    private int currentPage = 0;
    private final int itemsPerPage = 15; // Hiển thị 15 mục mỗi trang

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // 1. Tải dữ liệu gốc (Mock Data)
        loadMockData();

        // 2. Cập nhật các ô thống kê (dựa trên danh sách gốc)
        updateStatistics();

        // 3. Cấu hình các cột của TableView
        setupTableColumns();

        // 4. Cấu hình logic Tìm kiếm và Phân trang
        setupSearchAndPagination();

        // 5. Hiển thị trang đầu tiên
        updateTablePage();
    }

    /**
     * Liên kết các cột trong FXML với các thuộc tính (properties) trong model Resident.
     * Tên trong "PropertyValueFactory" phải khớp chính xác với tên getter trong model
     * (ví dụ: "fullName" -> getFullName(), "apartmentId" -> getApartmentId()).
     */
    private void setupTableColumns() {
        // Giả định model Resident của bạn có các getter:
        // getId(), getFullName(), getApartmentId(), getPhoneNumber()
        colResidentId.setCellValueFactory(new PropertyValueFactory<>("residentId"));

        // Các dòng này đã chính xác vì "User" (lớp cha) có getFullName() và getPhoneNumber()
        // và "Resident" (lớp con) có getApartmentId()
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colApartment.setCellValueFactory(new PropertyValueFactory<>("apartmentId"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
    }

    /**
     * Thiết lập các listener cho thanh tìm kiếm và khởi tạo các danh sách
     * FilteredList/SortedList để quản lý dữ liệu động.
     */
    private void setupSearchAndPagination() {
        // 1. Bao bọc danh sách gốc trong FilteredList
        filteredData = new FilteredList<>(masterResidentList, p -> true);

        // 2. Thêm listener cho thanh tìm kiếm
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(resident -> {
                // Nếu không tìm kiếm, hiển thị tất cả
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Lọc dựa trên nhiều trường
                if (resident.getFullName().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Khớp tên
                } else if (resident.getPhoneNumber().contains(lowerCaseFilter)) {
                    return true; // Khớp SĐT
                } else if (String.valueOf(resident.getApartmentId()).contains(lowerCaseFilter)) {
                    return true; // Khớp số căn hộ
                } else if (String.valueOf(resident.getResidentId()).contains(lowerCaseFilter)) {
                    return true; // Khớp mã cư dân
                }

                return false; // Không khớp
            });

            // Khi tìm kiếm, luôn quay về trang đầu tiên
            currentPage = 0;
            updateTablePage();
        });

        // 3. Bao bọc FilteredList trong SortedList
        sortedData = new SortedList<>(filteredData);

        // 4. Liên kết comparator của SortedList với TableView
        // Điều này cho phép người dùng sắp xếp bằng cách nhấp vào tiêu đề cột
        sortedData.comparatorProperty().bind(residentTableView.comparatorProperty());
    }

    /**
     * Hàm này tính toán và hiển thị dữ liệu cho trang hiện tại
     * dựa trên danh sách đã lọc và sắp xếp.
     */
    private void updateTablePage() {
        // Tính toán tổng số trang
        int totalItems = sortedData.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (totalPages == 0) {
            totalPages = 1; // Luôn có ít nhất 1 trang, ngay cả khi trống
        }

        // Đảm bảo trang hiện tại không vượt quá tổng số trang (hữu ích khi tìm kiếm)
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        // Tính toán chỉ số bắt đầu và kết thúc
        int fromIndex = currentPage * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, totalItems);

        // Lấy danh sách con (sublist) cho trang hiện tại
        List<Resident> pageData = sortedData.subList(fromIndex, toIndex);
        residentTableView.setItems(FXCollections.observableArrayList(pageData));

        // Cập nhật thông tin phân trang
        lblPaginationInfo.setText("Trang " + (currentPage + 1) + " / " + totalPages);

        // Cập nhật trạng thái nút
        btnPreviousPage.setDisable(currentPage == 0);
        btnNextPage.setDisable(currentPage >= totalPages - 1);
    }


    // --- LỚP NỘI BỘ (INNER CLASS) ---
    //
    // ĐÃ XÓA: private class ResidentCell extends ListCell<Resident>
    // Lớp này không còn cần thiết khi sử dụng TableView.
    //
    // ---


    // --- CÁC HÀM XỬ LÝ SỰ KIỆN ---

    @FXML
    void handlePreviousPage(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            updateTablePage();
        }
    }

    @FXML
    void handleNextPage(ActionEvent event) {
        int totalPages = (int) Math.ceil((double) sortedData.size() / itemsPerPage);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateTablePage();
        }
    }

    // Tạm thời vô hiệu hóa hàm này vì không có nút trong FXML mới
    // @FXML
    // void handleAddResident(ActionEvent event) {
    //     System.out.println("Nút 'Thêm mới' được nhấn!");
    //     // (Code mở cửa sổ Thêm mới)
    // }


    // --- HÀM TẢI DỮ LIỆU ---

    private void updateStatistics() {
        // Cập nhật thống kê dựa trên danh sách gốc (masterResidentList)
        lblTotalResidents.setText(String.valueOf(masterResidentList.size()));

        long apartmentCount = masterResidentList.stream()
                .map(Resident::getApartmentId)
                .distinct()
                .count();
        lblTotalApartments.setText(String.valueOf(apartmentCount));
    }

    /**
     * Tải dữ liệu giả (Mock Data) sử dụng Model của bạn
     */
    private void loadMockData() {
        long now = System.currentTimeMillis();
        Timestamp ts = new Timestamp(now);
        Date dob = new Date(now - 946708560000L); // Giả sử sinh năm 2000

        // Thêm dữ liệu vào danh sách gốc
        masterResidentList.addAll(
                new Resident(1, "nguyenvana", "a@email.com", "Nguyễn Văn An", Role.RESIDENT, ts, ts, "0901234567", 101, 201, dob, "001200000123", "Chủ hộ"),
                new Resident(2, "tranb", "b@email.com", "Trần Thị Bình", Role.RESIDENT, ts, ts, "0908888888", 102, 201, dob, "001200000456", "Vợ/Chồng"),
                new Resident(3, "lecuong", "c@email.com", "Lê Hoàng Cường", Role.RESIDENT, ts, ts, "0912345678", 103, 205, dob, "001200000789", "Chủ hộ"),
                new Resident(4, "phamdung", "d@email.com", "Phạm Thị Dung", Role.RESIDENT, ts, ts, "0934567890", 104, 101, dob, "001200001011", "Con"),
                new Resident(5, "hoangem", "e@email.com", "Hoàng Văn Em", Role.RESIDENT, ts, ts, "0945678901", 105, 202, dob, "001200001213", "Chủ hộ"),
                new Resident(6, "vophuong", "f@email.com", "Võ Thị Phương", Role.RESIDENT, ts, ts, "0956789012", 106, 301, dob, "001200001415", "Người thuê")
                // Thêm nhiều dữ liệu giả ở đây để kiểm tra phân trang
        );

        // Thêm 20 cư dân giả nữa để kiểm tra phân trang
        for (int i = 7; i < 27; i++) {
            masterResidentList.add(
                    new Resident(i, "user" + i, i + "@email.com", "Người Dùng " + i, Role.RESIDENT, ts, ts, "09000000" + i, 100 + i, 200 + (i % 5), dob, "00120000" + i, "Thành viên")
            );
        }
    }
}
