// Vị trí: src/main/java/com/example/quanlytoanha/model/DebtReport.java
package com.example.quanlytoanha.model;

import java.math.BigDecimal;

// Lớp này chứa dữ liệu thống kê tổng quan
public class DebtReport {
    private int totalUnpaidInvoices; // Tổng số hóa đơn chưa thanh toán
    private int totalOverdueInvoices; // Số hóa đơn đã quá hạn
    private BigDecimal totalDebtAmount;  // Tổng số tiền đang nợ
    private BigDecimal totalOverdueAmount; // Tổng số tiền đã quá hạn

    public DebtReport() {
    }

    /**
     * Constructor đầy đủ
     */
    public DebtReport(int totalUnpaidInvoices, int totalOverdueInvoices, BigDecimal totalDebtAmount, BigDecimal totalOverdueAmount) {
        this.totalUnpaidInvoices = totalUnpaidInvoices;
        this.totalOverdueInvoices = totalOverdueInvoices;
        this.totalDebtAmount = totalDebtAmount;
        this.totalOverdueAmount = totalOverdueAmount;
    }

    // --- Getters and Setters ---

    public int getTotalUnpaidInvoices() {
        return totalUnpaidInvoices;
    }

    public void setTotalUnpaidInvoices(int totalUnpaidInvoices) {
        this.totalUnpaidInvoices = totalUnpaidInvoices;
    }

    public int getTotalOverdueInvoices() {
        return totalOverdueInvoices;
    }

    public void setTotalOverdueInvoices(int totalOverdueInvoices) {
        this.totalOverdueInvoices = totalOverdueInvoices;
    }

    public BigDecimal getTotalDebtAmount() {
        return totalDebtAmount;
    }

    public void setTotalDebtAmount(BigDecimal totalDebtAmount) {
        this.totalDebtAmount = totalDebtAmount;
    }

    public BigDecimal getTotalOverdueAmount() {
        return totalOverdueAmount;
    }

    public void setTotalOverdueAmount(BigDecimal totalOverdueAmount) {
        this.totalOverdueAmount = totalOverdueAmount;
    }

    // --- (Tùy chọn) Phương thức toString() để debug ---
    @Override
    public String toString() {
        return "DebtReport{" +
                "totalUnpaidInvoices=" + totalUnpaidInvoices +
                ", totalOverdueInvoices=" + totalOverdueInvoices +
                ", totalDebtAmount=" + totalDebtAmount +
                ", totalOverdueAmount=" + totalOverdueAmount +
                '}';
    }
}

