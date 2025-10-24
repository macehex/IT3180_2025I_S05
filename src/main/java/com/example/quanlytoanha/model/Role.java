// Vị trí: src/main/java/com/example/quanlytoanha/model/Role.java
package com.example.quanlytoanha.model;

public enum Role {
    ADMIN(1, "Ban quản trị"),
    ACCOUNTANT(2, "Kế toán"),
    POLICE(3, "Công an"),
    RESIDENT(4, "Cư dân");

    private final int roleId;
    private final String roleName;

    Role(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public int getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    // Phương thức tĩnh để lấy Role từ roleId
    public static Role fromId(int roleId) {
        for (Role role : values()) {
            if (role.getRoleId() == roleId) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid roleId: " + roleId);
    }
}