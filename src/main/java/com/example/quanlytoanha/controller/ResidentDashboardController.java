package com.example.quanlytoanha.controller;
import com.example.quanlytoanha.dao.NotificationDAO; // Import mới
import com.example.quanlytoanha.dao.TransactionDAO;  // Import mới
import com.example.quanlytoanha.model.Notification;  // Import mới
import com.example.quanlytoanha.model.Transaction;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.model.ServiceConsumptionData;
import com.example.quanlytoanha.model.MonthlyConsumptionData;
import com.example.quanlytoanha.service.ServiceConsumptionService;
import com.example.quanlytoanha.session.SessionManager;
import io.github.palexdev.materialfx.controls.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.sql.SQLException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class ResidentDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label pageTitle;
    @FXML private Label debtAmountLabel;
    @FXML private Label paidAmountLabel;
    @FXML private Label notificationCountLabel;
    @FXML private VBox contentContainer;
    @FXML private VBox dashboardContent;

    // Chart-related FXML elements
    @FXML private Label chartPeriodLabel;
    @FXML private Label totalAmountLabel;
    @FXML private MFXButton currentMonthBtn;
    @FXML private MFXButton trendBtn;
    @FXML private VBox chartsContainer;
    @FXML private VBox pieChartContainer;
    @FXML private VBox lineChartContainer;
    @FXML private VBox noDataContainer;
    @FXML private PieChart serviceConsumptionChart;
    @FXML private LineChart<String, Number> consumptionTrendChart;
    @FXML private CategoryAxis monthAxis;
    @FXML private NumberAxis amountAxis;

    // --- CÁC NÚT MENU (MaterialFX) ---
    @FXML private MFXButton homeButton;
    @FXML private MFXButton invoiceHistoryButton;
    @FXML private MFXButton loginHistoryButton;
    @FXML private MFXButton notificationButton;
    @FXML private MFXButton viewMyRequestsButton;

    // MỚI: Thêm khai báo cho nút Tạo Phản Ánh (Lỗi cũ do thiếu dòng này)
    @FXML private MFXButton createRequestButton;
    
    // Nút yêu cầu thay đổi thông tin cá nhân
    @FXML private MFXButton profileChangeRequestButton;

    @FXML private MFXButton btnLogout;

    // Biến theo dõi nút đang được chọn
    private MFXButton currentActiveButton = null;

    // Service instances
    private ServiceConsumptionService consumptionService;
    private NumberFormat currencyFormatter;
    private int currentApartmentId = -1;

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final TransactionDAO transactionDAO = TransactionDAO.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Xin chào, " + currentUser.getUsername());
        }

        // Initialize services
        consumptionService = ServiceConsumptionService.getInstance();
        currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        // Get apartment ID for current user
        if (currentUser != null) {
            currentApartmentId = consumptionService.getApartmentIdByUserId(currentUser.getUserId());
        }

        // Thiết lập hiệu ứng hover cho tất cả các nút
        setupButtonHoverEffects();
        setupChartButtonEffects();

        loadDashboardData();
        showDashboardContent();
        loadServiceConsumptionChart();
    }

    /**
     * CẬP NHẬT: Hàm này giờ gọn hơn, gọi hàm chung cho từng nút
     */
    private void setupButtonHoverEffects() {
        setupSingleButtonHover(homeButton);
        setupSingleButtonHover(invoiceHistoryButton);
        setupSingleButtonHover(loginHistoryButton);
        setupSingleButtonHover(notificationButton);
        setupSingleButtonHover(viewMyRequestsButton);

        // MỚI: Đăng ký hiệu ứng cho nút Tạo Phản Ánh
        setupSingleButtonHover(createRequestButton);
        setupSingleButtonHover(profileChangeRequestButton);

        // Nút Đăng xuất (Logout) có màu riêng nên xử lý riêng
        if (btnLogout != null) {
            btnLogout.setOnMouseEntered(e ->
                    btnLogout.setStyle("-fx-background-color: rgba(244,67,54,1.0); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 600; -fx-cursor: hand; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER;")
            );
            btnLogout.setOnMouseExited(e ->
                    btnLogout.setStyle("-fx-background-color: rgba(244,67,54,0.9); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 600; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER;")
            );
        }
    }

    /**
     * MỚI: Hàm dùng chung để xử lý hiệu ứng Hover cho các nút menu
     * Giúp code không bị lặp lại và dễ sửa đổi style
     */
    private void setupSingleButtonHover(MFXButton button) {
        if (button == null) return; // Tránh lỗi NullPointerException

        // Khi chuột đi vào: Sáng hơn + Căn giữa (CENTER)
        button.setOnMouseEntered(e -> {
            if (currentActiveButton != button) {
                button.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER;");
            }
        });

        // Khi chuột đi ra: Tối hơn + Căn trái (CENTER_LEFT)
        button.setOnMouseExited(e -> {
            if (currentActiveButton != button) {
                button.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER_LEFT;");
            }
        });
    }

    private void loadDashboardData() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // 1. XỬ LÝ SỐ TIỀN TRẢ THÁNG NÀY
        try {
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());

            // Lấy danh sách giao dịch trong tháng này
            List<Transaction> transactions = transactionDAO.getTransactions(
                    currentUser.getUserId(),
                    startOfMonth,
                    endOfMonth
            );

            // Tính tổng tiền
            BigDecimal totalPaidThisMonth = BigDecimal.ZERO;
            for (Transaction t : transactions) {
                if (t.getAmount() != null) {
                    totalPaidThisMonth = totalPaidThisMonth.add(t.getAmount());
                }
            }

            paidAmountLabel.setText(currencyFormatter.format(totalPaidThisMonth) + " VND");

        } catch (Exception e) {
            e.printStackTrace();
            paidAmountLabel.setText("Lỗi");
        }

        // 2. XỬ LÝ SỐ THÔNG BÁO CHƯA ĐỌC
        try {
            List<Notification> unreadNotifications = notificationDAO.getUnreadNotificationsForUser(currentUser.getUserId());
            int count = unreadNotifications.size();
            notificationCountLabel.setText(String.valueOf(count));
        } catch (SQLException e) {
            e.printStackTrace();
            notificationCountLabel.setText("0");
        }
    }

    private void showDashboardContent() {
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(dashboardContent);
        pageTitle.setText("Trang chủ");
        setActiveButton(homeButton);
    }

    private void loadContentFromFxml(String fXMLPath, String title, MFXButton activeButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fXMLPath));
            Node content = loader.load();

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(content);
            pageTitle.setText(title);
            setActiveButton(activeButton);

        } catch (IOException e) {
            e.printStackTrace();
            showDashboardContent();
        }
    }

    /**
     * CẬP NHẬT: Đảm bảo nút Active cũng được căn giữa và có viền
     */
    private void setActiveButton(MFXButton activeButton) {
        // Reset nút cũ (trả về lề trái)
        if (currentActiveButton != null) {
            currentActiveButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER_LEFT;");
        }

        // Set nút mới (Căn giữa + Viền sáng)
        currentActiveButton = activeButton;
        if (currentActiveButton != null) {
            currentActiveButton.setStyle("-fx-background-color: rgba(255,255,255,0.35); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 600; -fx-font-family: 'DejaVu Sans'; -fx-border-color: rgba(255,255,255,0.5); -fx-border-width: 2; -fx-border-radius: 12; -fx-alignment: CENTER;");
        }
    }

    @FXML
    private void handleHomeButton() {
        showDashboardContent();
    }

    @FXML
    private void handleInvoiceHistoryButton() {
        loadContentFromFxml("/com/example/quanlytoanha/view/invoice_history_embedded.fxml", "Hóa đơn và Lịch sử giao dịch", invoiceHistoryButton);
    }

    @FXML
    private void handleLoginHistoryButton() {
        loadContentFromFxml("/com/example/quanlytoanha/view/login_management_embedded.fxml", "Quản lý đăng nhập", loginHistoryButton);
    }

    @FXML
    private void handleNotificationButton() {
        loadContentFromFxml("/com/example/quanlytoanha/view/notification_view_embedded.fxml", "Thông báo", notificationButton);
    }

    @FXML
    private void handleCreateRequestButton() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/create_service_request.fxml"));
            Node content = loader.load();
            CreateServiceRequestController controller = loader.getController();
            controller.setCurrentResidentId(currentUser.getUserId());
            
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(content);
            pageTitle.setText("Tạo Phản Ánh");
            setActiveButton(createRequestButton);
        } catch (IOException e) {
            e.printStackTrace();
            showDashboardContent();
        }
    }

    @FXML
    private void handleViewMyRequestsButton() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/my_requests_list.fxml"));
            Node content = loader.load();
            MyRequestsListController controller = loader.getController();
            controller.loadDataForResident(currentUser.getUserId());
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(content);
            pageTitle.setText("Yêu Cầu Của Tôi");
            setActiveButton(viewMyRequestsButton);
        } catch (IOException e) {
            e.printStackTrace();
            showDashboardContent();
        }
    }

    @FXML
    private void handleProfileChangeRequestButton() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/profile_change_request.fxml"));
            Node content = loader.load();
            
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(content);
            pageTitle.setText("Thay Đổi Thông Tin");
            setActiveButton(profileChangeRequestButton);
            
        } catch (IOException e) {
            e.printStackTrace();
            showDashboardContent();
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage currentStage = (Stage) btnLogout.getScene().getWindow();
        currentStage.close();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quanlytoanha/view/login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Quản lý Tòa nhà - Đăng nhập");
            loginStage.setScene(new Scene(root, 400, 300));
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup chart button hover effects
     */
    private void setupChartButtonEffects() {
        if (currentMonthBtn != null) {
            currentMonthBtn.setOnMouseEntered(e -> 
                currentMonthBtn.setStyle("-fx-background-color: #5a67d8; -fx-background-radius: 8; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans'; -fx-cursor: hand;"));
            currentMonthBtn.setOnMouseExited(e -> {
                if (currentMonthBtn.getStyle().contains("#667eea")) {
                    currentMonthBtn.setStyle("-fx-background-color: #667eea; -fx-background-radius: 8; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
                } else {
                    currentMonthBtn.setStyle("-fx-background-color: rgba(102,126,234,0.3); -fx-background-radius: 8; -fx-text-fill: #667eea; -fx-font-size: 13px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
                }
            });
        }

        if (trendBtn != null) {
            trendBtn.setOnMouseEntered(e -> 
                trendBtn.setStyle("-fx-background-color: #5a67d8; -fx-background-radius: 8; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans'; -fx-cursor: hand;"));
            trendBtn.setOnMouseExited(e -> {
                if (trendBtn.getStyle().contains("#667eea")) {
                    trendBtn.setStyle("-fx-background-color: #667eea; -fx-background-radius: 8; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
                } else {
                    trendBtn.setStyle("-fx-background-color: rgba(102,126,234,0.3); -fx-background-radius: 8; -fx-text-fill: #667eea; -fx-font-size: 13px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
                }
            });
        }
    }

    /**
     * Load service consumption chart (default: current month pie chart)
     */
    private void loadServiceConsumptionChart() {
        if (currentApartmentId == -1) {
            showNoDataMessage();
            return;
        }

        showCurrentMonthChart();
    }

    /**
     * Show current month pie chart
     */
    @FXML
    private void showCurrentMonthChart() {
        if (currentApartmentId == -1) {
            showNoDataMessage();
            return;
        }

        // Update button styles
        setActiveChartButton(currentMonthBtn);
        chartPeriodLabel.setText("Tháng hiện tại");

        // Get current month data
        List<ServiceConsumptionData> consumptionData = consumptionService.getCurrentMonthConsumption(currentApartmentId);

        if (consumptionData.isEmpty()) {
            showNoDataMessage();
            return;
        }

        // Show pie chart container
        pieChartContainer.setVisible(true);
        lineChartContainer.setVisible(false);
        noDataContainer.setVisible(false);

        // Clear previous data
        serviceConsumptionChart.getData().clear();

        // Add data to pie chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        BigDecimal total = BigDecimal.ZERO;

        for (ServiceConsumptionData data : consumptionData) {
            pieChartData.add(new PieChart.Data(
                String.format("%s (%.1f%%)", data.getServiceName(), data.getPercentage()),
                data.getAmount().doubleValue()
            ));
            total = total.add(data.getAmount());
        }

        serviceConsumptionChart.setData(pieChartData);

        // Update total amount label
        totalAmountLabel.setText("Tổng chi phí: " + currencyFormatter.format(total.doubleValue()) + " VND");

        // Apply custom colors to pie chart
        applyPieChartColors();
    }

    /**
     * Show 6-month trend line chart
     */
    @FXML
    private void showTrendChart() {
        if (currentApartmentId == -1) {
            showNoDataMessage();
            return;
        }

        // Update button styles
        setActiveChartButton(trendBtn);
        chartPeriodLabel.setText("6 tháng gần đây");

        // Get 6-month trend data
        List<MonthlyConsumptionData> trendData = consumptionService.getSixMonthConsumptionTrend(currentApartmentId);

        if (trendData.isEmpty()) {
            showNoDataMessage();
            return;
        }

        // Show line chart container
        pieChartContainer.setVisible(false);
        lineChartContainer.setVisible(true);
        noDataContainer.setVisible(false);

        // Clear previous data
        consumptionTrendChart.getData().clear();

        // Prepare data for line chart
        Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();

        for (MonthlyConsumptionData monthData : trendData) {
            String monthLabel = monthData.getMonthYearLabel();

            for (Map.Entry<String, BigDecimal> serviceEntry : monthData.getServiceBreakdown().entrySet()) {
                String serviceName = serviceEntry.getKey();
                BigDecimal amount = serviceEntry.getValue();

                // Get or create series for this service
                XYChart.Series<String, Number> series = seriesMap.computeIfAbsent(serviceName, 
                    k -> {
                        XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                        newSeries.setName(k);
                        return newSeries;
                    });

                // Add data point
                series.getData().add(new XYChart.Data<>(monthLabel, amount.doubleValue()));
            }
        }

        // Add all series to chart
        for (XYChart.Series<String, Number> series : seriesMap.values()) {
            consumptionTrendChart.getData().add(series);
        }

        // Configure axes
        amountAxis.setLabel("Số tiền (VND)");
        monthAxis.setLabel("Tháng");
    }

    /**
     * Show no data message
     */
    private void showNoDataMessage() {
        pieChartContainer.setVisible(false);
        lineChartContainer.setVisible(false);
        noDataContainer.setVisible(true);
    }

    /**
     * Set active chart button
     */
    private void setActiveChartButton(MFXButton activeButton) {
        // Reset both buttons to inactive state
        currentMonthBtn.setStyle("-fx-background-color: rgba(102,126,234,0.3); -fx-background-radius: 8; -fx-text-fill: #667eea; -fx-font-size: 13px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
        trendBtn.setStyle("-fx-background-color: rgba(102,126,234,0.3); -fx-background-radius: 8; -fx-text-fill: #667eea; -fx-font-size: 13px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");

        // Set active button
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #667eea; -fx-background-radius: 8; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
        }
    }

    /**
     * Apply custom colors to pie chart slices
     */
    private void applyPieChartColors() {
        String[] colors = {
            "#667eea", "#f093fb", "#4facfe", "#43e97b", "#fa709a", 
            "#fee140", "#a8edea", "#d299c2", "#89f7fe", "#66a6ff"
        };

        // Apply colors after the chart is rendered
        serviceConsumptionChart.applyCss();
        serviceConsumptionChart.layout();

        ObservableList<PieChart.Data> data = serviceConsumptionChart.getData();
        for (int i = 0; i < data.size() && i < colors.length; i++) {
            data.get(i).getNode().setStyle("-fx-pie-color: " + colors[i] + ";");
        }
    }
}