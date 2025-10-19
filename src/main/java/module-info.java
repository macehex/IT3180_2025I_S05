module com.example.quanlytoanha {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    opens com.example.quanlytoanha to javafx.fxml;
    exports com.example.quanlytoanha;
}