module com.example.planetsexplorer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires org.json;

    opens com.example.planetsexplorer to javafx.fxml;
    exports com.example.planetsexplorer;
}