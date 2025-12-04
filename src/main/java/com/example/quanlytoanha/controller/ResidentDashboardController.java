package com.example.quanlytoanha.controller;

import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.model.ServiceConsumptionData;
import com.example.quanlytoanha.model.MonthlyConsumptionData;
import com.example.quanlytoanha.service.NotificationService; // [MỚI] Import Service thông báo
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
    @FXML private Label notificationCountLabel; // Đây là Label hiển thị số lượng
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
    @FXML private MFXButton createRequestButton;
    @FXML private MFXButton btnLogout;

    // Biến theo dõi nút đang được chọn
    private MFXButton currentActiveButton = null;

    // Service instances
    private ServiceConsumptionService consumptionService;
    private NotificationService notificationService; // [MỚI] Khai báo Service
    private NumberFormat currencyFormatter;
    private int currentApartmentId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Xin chào, " + currentUser.getUsername());
        }

        // Initialize services
        consumptionService = ServiceConsumptionService.getInstance();
        notificationService = new NotificationService(); // [MỚI] Khởi tạo Service
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

    private void setupButtonHoverEffects() {
        setupSingleButtonHover(homeButton);
        setupSingleButtonHover(invoiceHistoryButton);
        setupSingleButtonHover(loginHistoryButton);
        setupSingleButtonHover(notificationButton);
        setupSingleButtonHover(viewMyRequestsButton);
        setupSingleButtonHover(createRequestButton);

        if (btnLogout != null) {
            btnLogout.setOnMouseEntered(e ->
                    btnLogout.setStyle("-fx-background-color: rgba(244,67,54,1.0); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 600; -fx-cursor: hand; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER;")
            );
            btnLogout.setOnMouseExited(e ->
                    btnLogout.setStyle("-fx-background-color: rgba(244,67,54,0.9); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 600; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER;")
            );
        }
    }

    private void setupSingleButtonHover(MFXButton button) {
        if (button == null) return;

        button.setOnMouseEntered(e -> {
            if (currentActiveButton != button) {
                button.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER;");
            }
        });

        button.setOnMouseExited(e -> {
            if (currentActiveButton != button) {
                button.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER_LEFT;");
            }
        });
    }

    // Hàm load dữ liệu dashboard đã được cập nhật logic lấy số thông báo
    private void loadDashboardData() {
        debtAmountLabel.setText("0 VND");
        paidAmountLabel.setText("0 VND");

        // --- CẬP NHẬT SỐ THÔNG BÁO TỪ DB ---
        try {
            int unreadCount = notificationService.getUnreadCount();
            notificationCountLabel.setText(String.valueOf(unreadCount));
        } catch (Exception e) {
            e.printStackTrace();
            notificationCountLabel.setText("0"); // Fallback nếu lỗi
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

    private void setActiveButton(MFXButton activeButton) {
        if (currentActiveButton != null) {
            currentActiveButton.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans'; -fx-alignment: CENTER_LEFT;");
        }

        currentActiveButton = activeButton;
        if (currentActiveButton != null) {
            currentActiveButton.setStyle("-fx-background-color: rgba(255,255,255,0.35); -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: 600; -fx-font-family: 'DejaVu Sans'; -fx-border-color: rgba(255,255,255,0.5); -fx-border-width: 2; -fx-border-radius: 12; -fx-alignment: CENTER;");
        }
    }

    @FXML
    private void handleHomeButton() {
        // [MỚI] Load lại dữ liệu khi quay về Home để cập nhật số thông báo mới nhất
        loadDashboardData();
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
            Parent root = loader.load();
            CreateServiceRequestController controller = loader.getController();
            controller.setCurrentResidentId(currentUser.getUserId());
            Stage stage = new Stage();
            stage.setTitle("Tạo Yêu Cầu Mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
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

    // Chart logic methods... (Giữ nguyên không thay đổi)
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

    private void loadServiceConsumptionChart() {
        if (currentApartmentId == -1) {
            showNoDataMessage();
            return;
        }
        showCurrentMonthChart();
    }

    @FXML
    private void showCurrentMonthChart() {
        if (currentApartmentId == -1) {
            showNoDataMessage();
            return;
        }
        setActiveChartButton(currentMonthBtn);
        chartPeriodLabel.setText("Tháng hiện tại");

        List<ServiceConsumptionData> consumptionData = consumptionService.getCurrentMonthConsumption(currentApartmentId);
        if (consumptionData.isEmpty()) {
            showNoDataMessage();
            return;
        }

        pieChartContainer.setVisible(true);
        lineChartContainer.setVisible(false);
        noDataContainer.setVisible(false);

        serviceConsumptionChart.getData().clear();
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
        totalAmountLabel.setText("Tổng chi phí: " + currencyFormatter.format(total.doubleValue()) + " VND");
        applyPieChartColors();
    }

    @FXML
    private void showTrendChart() {
        if (currentApartmentId == -1) {
            showNoDataMessage();
            return;
        }
        setActiveChartButton(trendBtn);
        chartPeriodLabel.setText("6 tháng gần đây");

        List<MonthlyConsumptionData> trendData = consumptionService.getSixMonthConsumptionTrend(currentApartmentId);
        if (trendData.isEmpty()) {
            showNoDataMessage();
            return;
        }

        pieChartContainer.setVisible(false);
        lineChartContainer.setVisible(true);
        noDataContainer.setVisible(false);
        consumptionTrendChart.getData().clear();

        Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();
        for (MonthlyConsumptionData monthData : trendData) {
            String monthLabel = monthData.getMonthYearLabel();
            for (Map.Entry<String, BigDecimal> serviceEntry : monthData.getServiceBreakdown().entrySet()) {
                String serviceName = serviceEntry.getKey();
                BigDecimal amount = serviceEntry.getValue();
                XYChart.Series<String, Number> series = seriesMap.computeIfAbsent(serviceName,
                        k -> {
                            XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                            newSeries.setName(k);
                            return newSeries;
                        });
                series.getData().add(new XYChart.Data<>(monthLabel, amount.doubleValue()));
            }
        }

        for (XYChart.Series<String, Number> series : seriesMap.values()) {
            consumptionTrendChart.getData().add(series);
        }
        amountAxis.setLabel("Số tiền (VND)");
        monthAxis.setLabel("Tháng");
    }

    private void showNoDataMessage() {
        pieChartContainer.setVisible(false);
        lineChartContainer.setVisible(false);
        noDataContainer.setVisible(true);
    }

    private void setActiveChartButton(MFXButton activeButton) {
        currentMonthBtn.setStyle("-fx-background-color: rgba(102,126,234,0.3); -fx-background-radius: 8; -fx-text-fill: #667eea; -fx-font-size: 13px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
        trendBtn.setStyle("-fx-background-color: rgba(102,126,234,0.3); -fx-background-radius: 8; -fx-text-fill: #667eea; -fx-font-size: 13px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");

        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #667eea; -fx-background-radius: 8; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500; -fx-font-family: 'DejaVu Sans';");
        }
    }

    private void applyPieChartColors() {
        String[] colors = {
                "#667eea", "#f093fb", "#4facfe", "#43e97b", "#fa709a",
                "#fee140", "#a8edea", "#d299c2", "#89f7fe", "#66a6ff"
        };
        serviceConsumptionChart.applyCss();
        serviceConsumptionChart.layout();
        ObservableList<PieChart.Data> data = serviceConsumptionChart.getData();
        for (int i = 0; i < data.size() && i < colors.length; i++) {
            data.get(i).getNode().setStyle("-fx-pie-color: " + colors[i] + ";");
        }
    }
}