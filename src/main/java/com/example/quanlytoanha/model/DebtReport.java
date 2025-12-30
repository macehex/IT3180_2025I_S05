// Vị trí: src/main/java/com/example/quanlytoanha/model/DebtReport.java
package com.example.quanlytoanha.model;

import java.math.BigDecimal;

// Lớp này chứa dữ liệu thống kê tổng quan
public class DebtReport {
    private int totalUnpaidInvoices; // Tổng số hóa đơn chưa thanh toán
    private int totalOverdueInvoices; // Số hóa đơn đã quá hạn
    private BigDecimal totalDebtAmount;  // Tổng số tiền đang nợ
    private BigDecimal totalOverdueAmount; // Tổng số tiền đã quá hạn
    private int totalPaidInvoices;        // Tổng số hóa đơn đã thanh toán
    private BigDecimal totalCollectedAmount;

    public DebtReport() {
        this.totalDebtAmount = BigDecimal.ZERO;
        this.totalOverdueAmount = BigDecimal.ZERO;
        this.totalCollectedAmount = BigDecimal.ZERO;
    }

    /**
     * Constructor đầy đủ
     */
    public DebtReport(int totalUnpaidInvoices, int totalOverdueInvoices, BigDecimal totalDebtAmount, BigDecimal totalOverdueAmount) {
        this.totalUnpaidInvoices = totalUnpaidInvoices;
        this.totalOverdueInvoices = totalOverdueInvoices;
        this.totalDebtAmount = totalDebtAmount;
        this.totalOverdueAmount = totalOverdueAmount;
        this.totalPaidInvoices = totalPaidInvoices;
        this.totalCollectedAmount = totalCollectedAmount;
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

    public int getTotalPaidInvoices() { return totalPaidInvoices; }
    public void setTotalPaidInvoices(int totalPaidInvoices) { this.totalPaidInvoices = totalPaidInvoices; }

    public BigDecimal getTotalCollectedAmount() { return totalCollectedAmount; }
    public void setTotalCollectedAmount(BigDecimal totalCollectedAmount) { this.totalCollectedAmount = totalCollectedAmount; }
    // --- (Tùy chọn) Phương thức toString() để debug ---
    @Override
    public String toString() {
        return "DebtReport{" +
                "unpaid=" + totalUnpaidInvoices +
                ", overdue=" + totalOverdueInvoices +
                ", debt=" + totalDebtAmount +
                ", overdueAmount=" + totalOverdueAmount +
                ", paid=" + totalPaidInvoices +
                ", collected=" + totalCollectedAmount +
                '}';
    }
}

