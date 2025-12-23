package com.example.quanlytoanha.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ContributionHistoryDTO {
    private LocalDateTime paymentDate;
    private String roomNumber;     // Số phòng (VD: 405)
    private String ownerName;      // Tên chủ hộ
    private String feeName;        // Tên quỹ (VD: Đóng góp Tết)
    private BigDecimal amount;     // Số tiền đóng

    public ContributionHistoryDTO(LocalDateTime paymentDate, String roomNumber, String ownerName, String feeName, BigDecimal amount) {
        this.paymentDate = paymentDate;
        this.roomNumber = roomNumber;
        this.ownerName = ownerName;
        this.feeName = feeName;
        this.amount = amount;
    }

    // Getters
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public String getRoomNumber() { return roomNumber; }
    public String getOwnerName() { return ownerName; }
    public String getFeeName() { return feeName; }
    public BigDecimal getAmount() { return amount; }
}
