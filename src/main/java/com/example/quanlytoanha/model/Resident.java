// Vị trí: src/main/java/com/example/quanlytoanha/model/Resident.java
package com.example.quanlytoanha.model;

import java.sql.Timestamp;
import java.util.Date;
// Giả sử bạn có cả các model khác như Vehicle, Apartment
// import java.util.List;

/**
 * Đại diện cho Cư dân. Kế thừa từ User và thêm các thông tin
 * từ bảng 'residents'.
 */
public class Resident extends User {

    // Thông tin thêm từ bảng 'residents'
    private int residentId;
    private int apartmentId;
    private Date dateOfBirth;
    private String idCardNumber;
    private String relationship; // (Quan hệ với chủ hộ)
    // private List<Vehicle> vehicles; // Có thể thêm sau

    // Constructor
    public Resident(int userId, String username, String email, String fullName, Role role, Timestamp createdAt, Timestamp lastLogin, String phoneNumber,
                    int residentId, int apartmentId, Date dateOfBirth, String idCardNumber, String relationship) {

        // Gọi constructor của lớp cha (User)
        super(userId, username, email, fullName, role, createdAt, lastLogin, phoneNumber);

        // Gán các trường của riêng lớp Resident
        this.residentId = residentId;
        this.apartmentId = apartmentId;
        this.dateOfBirth = dateOfBirth;
        this.idCardNumber = idCardNumber;
        this.relationship = relationship;
    }

    /**
     * Định nghĩa phương thức abstract từ lớp cha
     */
    @Override
    public void displayDashboard() {
        System.out.println("--- Cư dân Dashboard ---");
        System.out.println("Xin chào, " + getFullName());
        System.out.println("1. Xem hóa đơn");
        System.out.println("2. Gửi yêu cầu dịch vụ");
        System.out.println("3. Xem thông báo");
        System.out.println("4. Đăng ký xe");
    }

    // Các phương thức riêng của Cư dân
    public void payInvoice() {
        System.out.println("Đang thực hiện thanh toán hóa đơn...");
    }

    public void submitServiceRequest() {
        System.out.println("Gửi yêu cầu sửa chữa/dịch vụ...");
    }

    // --- Getters/Setters cho các trường riêng ---

    public int getResidentId() {
        return residentId;
    }

    public void setResidentId(int residentId) {
        this.residentId = residentId;
    }

    public int getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(int apartmentId) {
        this.apartmentId = apartmentId;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
