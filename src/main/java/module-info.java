module org.example.planetsexplorer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires org.json;
    requires com.opencsv;

    opens org.example.planetsexplorer to javafx.fxml;
    exports org.example.planetsexplorer;
    exports org.example.planetsexplorer.celestial;
    opens org.example.planetsexplorer.celestial to javafx.fxml;
}