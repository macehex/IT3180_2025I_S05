// Vị trí: src/main/java/com/example/quanlytoanha/service/MaintenanceService.java
package com.example.quanlytoanha.service;

import com.example.quanlytoanha.dao.MaintenanceDAO;
import com.example.quanlytoanha.model.Maintenance;
import java.sql.SQLException;
import java.util.List;

public class MaintenanceService {

    private MaintenanceDAO maintenanceDAO = new MaintenanceDAO();

    public List<Maintenance> getAllMaintenanceHistory() throws SQLException {
        return maintenanceDAO.getAllMaintenanceHistory();
    }

    public boolean scheduleMaintenance(Maintenance maintenance) throws SQLException {
        // (Có thể thêm logic validation ở đây)
        return maintenanceDAO.scheduleMaintenance(maintenance);
    }

    public boolean completeMaintenance(Maintenance maintenance) throws SQLException {
        // (Có thể thêm logic validation ở đây)
        return maintenanceDAO.completeMaintenance(
                maintenance.getMaintenanceId(),
                maintenance.getMaintenanceDate(),
                maintenance.getCost(),
                maintenance.getPerformedBy(),
                maintenance.getDescription()
        );
    }
}