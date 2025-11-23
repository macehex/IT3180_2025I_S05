package com.example.quanlytoanha.service;

import com.example.quanlytoanha.model.ServiceConsumptionData;
import com.example.quanlytoanha.model.MonthlyConsumptionData;
import com.example.quanlytoanha.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ServiceConsumptionService {
    private static ServiceConsumptionService instance;

    private ServiceConsumptionService() {}

    public static ServiceConsumptionService getInstance() {
        if (instance == null) {
            instance = new ServiceConsumptionService();
        }
        return instance;
    }

    /**
     * Get current month service consumption breakdown for pie chart
     */
    public List<ServiceConsumptionData> getCurrentMonthConsumption(int apartmentId) {
        List<ServiceConsumptionData> consumptionData = new ArrayList<>();
        String sql = """
            SELECT 
                id.name as service_name,
                SUM(id.amount) as total_amount
            FROM invoicedetails id
            JOIN invoices i ON id.invoice_id = i.invoice_id 
            WHERE i.apartment_id = ? 
              AND EXTRACT(YEAR FROM i.due_date) = EXTRACT(YEAR FROM CURRENT_DATE)
              AND EXTRACT(MONTH FROM i.due_date) = EXTRACT(MONTH FROM CURRENT_DATE)
            GROUP BY id.name
            ORDER BY total_amount DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, apartmentId);
            ResultSet rs = stmt.executeQuery();

            // First pass: calculate total amount
            BigDecimal totalAmount = BigDecimal.ZERO;
            Map<String, BigDecimal> serviceAmounts = new LinkedHashMap<>();
            
            while (rs.next()) {
                String serviceName = rs.getString("service_name");
                BigDecimal amount = rs.getBigDecimal("total_amount");
                serviceAmounts.put(serviceName, amount);
                totalAmount = totalAmount.add(amount);
            }

            // Second pass: calculate percentages and create data objects
            for (Map.Entry<String, BigDecimal> entry : serviceAmounts.entrySet()) {
                double percentage = 0;
                if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    percentage = entry.getValue()
                        .divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
                }
                
                consumptionData.add(new ServiceConsumptionData(
                    entry.getKey(),
                    entry.getValue(),
                    percentage
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return consumptionData;
    }

    /**
     * Get 6-month service consumption trend for line chart
     */
    public List<MonthlyConsumptionData> getSixMonthConsumptionTrend(int apartmentId) {
        List<MonthlyConsumptionData> monthlyData = new ArrayList<>();
        String sql = """
            SELECT 
                EXTRACT(YEAR FROM i.due_date) as year,
                EXTRACT(MONTH FROM i.due_date) as month,
                id.name as service_name,
                SUM(id.amount) as total_amount
            FROM invoicedetails id
            JOIN invoices i ON id.invoice_id = i.invoice_id 
            WHERE i.apartment_id = ? 
              AND i.due_date >= CURRENT_DATE - INTERVAL '6 months'
            GROUP BY EXTRACT(YEAR FROM i.due_date), EXTRACT(MONTH FROM i.due_date), id.name
            ORDER BY year, month
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, apartmentId);
            ResultSet rs = stmt.executeQuery();

            Map<String, Map<String, BigDecimal>> monthlyBreakdown = new LinkedHashMap<>();
            
            while (rs.next()) {
                int year = rs.getInt("year");
                int month = rs.getInt("month");
                String serviceName = rs.getString("service_name");
                BigDecimal amount = rs.getBigDecimal("total_amount");
                
                String monthKey = year + "-" + String.format("%02d", month);
                
                monthlyBreakdown
                    .computeIfAbsent(monthKey, k -> new HashMap<>())
                    .put(serviceName, amount);
            }

            // Convert to MonthlyConsumptionData objects
            for (Map.Entry<String, Map<String, BigDecimal>> entry : monthlyBreakdown.entrySet()) {
                String[] parts = entry.getKey().split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                
                monthlyData.add(new MonthlyConsumptionData(year, month, entry.getValue()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return monthlyData;
    }

    /**
     * Get apartment ID for a given resident user ID
     */
    public int getApartmentIdByUserId(int userId) {
        String sql = """
            SELECT a.apartment_id 
            FROM apartments a
            JOIN residents r ON a.apartment_id = r.apartment_id
            WHERE r.user_id = ? 
            LIMIT 1
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("apartment_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; // Not found
    }

    /**
     * Get total consumption for current month
     */
    public BigDecimal getCurrentMonthTotal(int apartmentId) {
        String sql = """
            SELECT SUM(i.total_amount) as monthly_total
            FROM invoices i
            WHERE i.apartment_id = ? 
              AND EXTRACT(YEAR FROM i.due_date) = EXTRACT(YEAR FROM CURRENT_DATE)
              AND EXTRACT(MONTH FROM i.due_date) = EXTRACT(MONTH FROM CURRENT_DATE)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, apartmentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("monthly_total");
                return total != null ? total : BigDecimal.ZERO;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }
}