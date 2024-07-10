package org.example.planetsexplorer;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.planetsexplorer.celestial.Celestial;
import org.example.planetsexplorer.celestial.PrimaryBody;
import org.example.planetsexplorer.celestial.SecondaryBody;

public class PlanetViewer {
    public static Celestial selectedCelestial = null;

    private final static Label lblName = new Label("Celestial Name: ");
    private final static Label lblDbID = new Label("DB ID: ");

    private final static Label lblSecondaryBodies = new Label("Secondary Bodies: ");

    private final static Label lblPrimaryBody = new Label("Primary Body: ");
    private final static Label lblOrbitDistance = new Label("Orbit Distance: ");
    private final static Label lblOrbitPeriodYear = new Label("Orbit Period Year: ");
    private final static Label lblSideRealDayHr = new Label("Side Real Day Hr: ");
    private final static Label lblObliquityToOrbit = new Label("Obliquity To Orbit: ");

    public PlanetViewer() {
        GridPane viewerGridRoot = new GridPane();

        GridPane.setConstraints(lblName, 0, 0);
        viewerGridRoot.getChildren().add(lblName);

        GridPane.setConstraints(lblDbID, 0, 1);
        viewerGridRoot.getChildren().add(lblDbID);

        // Should have a ComboBox of secondary bodies
        GridPane.setConstraints(lblSecondaryBodies, 0, 2);
        viewerGridRoot.getChildren().add(lblSecondaryBodies);

        // VISUAL SEPARATION BETWEEN PRIMARY/SEC CHARS

        GridPane.setConstraints(lblPrimaryBody, 0, 3);
        viewerGridRoot.getChildren().add(lblPrimaryBody);

        GridPane.setConstraints(lblOrbitDistance, 0, 4);
        viewerGridRoot.getChildren().add(lblOrbitDistance);

        GridPane.setConstraints(lblOrbitPeriodYear, 0, 5);
        viewerGridRoot.getChildren().add(lblOrbitPeriodYear);

        GridPane.setConstraints(lblSideRealDayHr, 0, 6);
        viewerGridRoot.getChildren().add(lblSideRealDayHr);

        GridPane.setConstraints(lblObliquityToOrbit, 0, 7);
        viewerGridRoot.getChildren().add(lblObliquityToOrbit);

        Scene viewerScene = new Scene(viewerGridRoot, 300, 600);
        Stage viewerStage = new Stage();
        viewerStage.setTitle("Planet Viewer");
        viewerStage.setScene(viewerScene);
        viewerStage.show();
    }

    public static void setSelectedCelestial(Celestial celestial) {
        selectedCelestial = celestial;

        lblName.setText("Celestial Name: " + celestial.getName());
        lblDbID.setText("DB ID: " + celestial.getDbID());

        if(celestial instanceof PrimaryBody primaryBody) {
            lblSecondaryBodies.setText("Secondary Bodies: " + primaryBody.getSecondaryBodies().size());
        }

        if(celestial instanceof SecondaryBody secondaryBody) {
            lblPrimaryBody.setText("Primary Body: " + secondaryBody.getPrimaryBody().getName());
            lblOrbitDistance.setText("Orbit Distance: " + secondaryBody.getOrbitDistance());
            lblOrbitPeriodYear.setText("Orbit Period Year: " + secondaryBody.getOrbitPeriodYear());
            lblSideRealDayHr.setText("Side Real Day Hr: " + secondaryBody.getSiderealDayHr());
            lblObliquityToOrbit.setText("Obliquity To Orbit: " + secondaryBody.getObliquityToOrbitDeg());
        }
    }

}
