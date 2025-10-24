// Vị trí: src/main/java/com/example/quanlytoanha/model/Permission.java
package com.example.quanlytoanha.model;

import java.util.Objects;

/**
 * Đại diện cho một Quyền (Permission) trong hệ thống.
 * Lớp này là một POJO (Plain Old Java Object) ánh xạ trực tiếp
 * với bảng 'permission' trong database.
 */
public class Permission {

    // --- Fields ---
    // Tương ứng với các cột trong bảng 'permission'
    private int permissionId;
    private String permissionName;

    // --- Constructors ---

    /**
     * Constructor mặc định (rỗng).
     * Cần thiết cho nhiều thư viện ORM (như JPA) hoặc (Jackson).
     */
    public Permission() {
    }

    /**
     * Constructor đầy đủ.
     * Dùng khi tạo đối tượng sau khi đọc từ database.
     * @param permissionId ID của quyền
     * @param permissionName Tên của quyền (ví dụ: "CREATE_INVOICE", "VIEW_USERS")
     */
    public Permission(int permissionId, String permissionName) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
    }

    // --- Getters and Setters ---
    // Cung cấp các phương thức để truy cập và cập nhật các trường

    public int getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(int permissionId) {
        this.permissionId = permissionId;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    // --- Helper Methods (Rất nên có) ---

    /**
     * Phương thức toString() giúp cho việc debug (gỡ lỗi) dễ dàng hơn.
     * Khi bạn in đối tượng Permission ra console, nó sẽ hiển thị thông tin rõ ràng.
     */
    @Override
    public String toString() {
        return "Permission{" +
                "permissionId=" + permissionId +
                ", permissionName='" + permissionName + '\'' +
                '}';
    }

    /**
     * Phương thức equals() và hashCode() rất quan trọng
     * nếu bạn định lưu các đối tượng Permission trong một Collection
     * như là HashSet hoặc HashMap (ví dụ: trong danh sách quyền của User).
     * Chúng ta chỉ cần so sánh dựa trên 'permissionId' vì nó là khóa chính (pk).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return permissionId == that.permissionId;
    }

    @Override
    public int hashCode() {
        // Sử dụng Objects.hash(permissionId) cũng được,
        // nhưng trả về chính permissionId là đơn giản và hiệu quả nhất
        return Integer.hashCode(permissionId);
    }
}