module org.example.planetsexplorer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.planetsexplorer to javafx.fxml;
    exports org.example.planetsexplorer;
}