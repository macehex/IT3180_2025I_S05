module com.example.quanlytoanha {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires javafx.graphics;
    requires java.sql;
    requires java.desktop;
    requires spring.security.core;

    opens com.example.quanlytoanha to javafx.fxml;
    opens com.example.quanlytoanha.controller to javafx.fxml;
    opens com.example.quanlytoanha.model to javafx.base;
    exports com.example.quanlytoanha;
    exports com.example.quanlytoanha.controller;  // Thêm dòng này nếu cần

}