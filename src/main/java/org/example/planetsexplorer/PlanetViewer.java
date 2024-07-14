package org.example.planetsexplorer;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.planetsexplorer.celestial.Celestial;
import org.example.planetsexplorer.celestial.PrimaryBody;
import org.example.planetsexplorer.celestial.SecondaryBody;

import java.time.LocalDate;

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

    private final static DatePicker dateEphemStart = new DatePicker();
    private final static ComboBox<Integer> hourEphemStart = new ComboBox<>();
    private final static ComboBox<Integer> minEphemStart = new ComboBox<>();

    private final static DatePicker dateEphemStop = new DatePicker();
    private final static ComboBox<Integer> hourEphemStop = new ComboBox<>();
    private final static ComboBox<Integer> minEphemStop = new ComboBox<>();

    private final static ComboBox<StepSize> stepEphem = new ComboBox<>();

    private final static Button btnQueryEphem = new Button("Query Ephemeris");

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


        GridPane.setConstraints(dateEphemStart, 0, 9);
        viewerGridRoot.getChildren().add(dateEphemStart);

        GridPane.setConstraints(hourEphemStart, 1, 9);
        viewerGridRoot.getChildren().add(hourEphemStart);

        GridPane.setConstraints(minEphemStart, 2, 9);
        viewerGridRoot.getChildren().add(minEphemStart);


        GridPane.setConstraints(dateEphemStop, 0, 10);
        viewerGridRoot.getChildren().add(dateEphemStop);

        GridPane.setConstraints(hourEphemStop, 1, 10);
        viewerGridRoot.getChildren().add(hourEphemStop);

        GridPane.setConstraints(minEphemStop, 2, 10);
        viewerGridRoot.getChildren().add(minEphemStop);

        GridPane.setConstraints(stepEphem, 0, 11);
        viewerGridRoot.getChildren().add(stepEphem);

        for(StepSize step: StepSize.values()) {
            stepEphem.getItems().add(step);
        }

        GridPane.setConstraints(btnQueryEphem, 0, 12);
        viewerGridRoot.getChildren().add(btnQueryEphem);

        btnQueryEphem.setOnMouseClicked(e -> {
            if(selectedCelestial instanceof SecondaryBody secBody) {
                secBody.setEphemeris(
                        dateEphemStart.getValue() + " " + hourEphemStart.getValue() + ":" + minEphemStart.getValue(),
                        dateEphemStop.getValue() + " " + hourEphemStop.getValue() + ":" + minEphemStop.getValue(),
                        stepEphem.getValue());
                PlanetsCamera.updateUIPosition(true);
            }
        });

        for(int i=0; i<=24; i++) {
            hourEphemStart.getItems().add(i);
            hourEphemStop.getItems().add(i);
        }

        for(int i=0; i<=60; i++) {
            minEphemStart.getItems().add(i);
            minEphemStop.getItems().add(i);
        }

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

            LocalDate startDate = LocalDate.of(secondaryBody.getEphemStartYear(),
                    secondaryBody.getEphemStartMonth(),
                    secondaryBody.getEphemStartDay());
            dateEphemStart.setValue(startDate);
            hourEphemStart.setValue(secondaryBody.getEphemStartHour());
            minEphemStart.setValue(secondaryBody.getEphemStartMinute());

            LocalDate stopDate = LocalDate.of(secondaryBody.getEphemStopYear(),
                    secondaryBody.getEphemStopMonth(),
                    secondaryBody.getEphemStopDay());
            dateEphemStop.setValue(stopDate);
            hourEphemStop.setValue(secondaryBody.getEphemStopHour());
            minEphemStop.setValue(secondaryBody.getEphemStopMinute());

            stepEphem.setValue(secondaryBody.getEphemStepSize());
        }
    }
}
