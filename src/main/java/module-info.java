module com.example.quanlytoanha {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.base;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;
    requires MaterialFX;
    requires java.sql;
    requires java.desktop;
    requires spring.security.core;
    requires java.prefs;
    requires com.google.gson;
    requires org.apache.pdfbox;

    opens com.example.quanlytoanha to javafx.fxml, javafx.graphics;
    opens com.example.quanlytoanha.controller to javafx.fxml;
    opens com.example.quanlytoanha.model to javafx.base;
    opens com.example.quanlytoanha.ui to javafx.fxml, javafx.graphics;

    exports com.example.quanlytoanha;
    exports com.example.quanlytoanha.controller;
    exports com.example.quanlytoanha.model;
    exports com.example.quanlytoanha.ui;
}