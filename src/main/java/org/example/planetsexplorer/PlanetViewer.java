package org.example.planetsexplorer;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.planetsexplorer.celestial.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class PlanetViewer {
    public static Celestial selectedCelestial = null;

    private final static Tab selectedCelestialTab = new Tab("Selected Celestial");
    private final static Tab queryCelestialTab = new Tab("Query Celestial");
    private final static TabPane tabPane = new TabPane(selectedCelestialTab, queryCelestialTab);

    private final static GridPane queryGridPane = new GridPane();

    private final static Label lblName = new Label("Celestial Name: ");
    private final static Label lblDbID = new Label("DB ID: ");

    private final static Label lblSecondaryBodies = new Label("Secondary Bodies: ");

    private final static Label lblPrimaryBody = new Label("Primary Body: ");
    private final static Label lblOrbitDistance = new Label("Orbit Distance: ");
    private final static Label lblOrbitPeriodYear = new Label("Orbit Period Year: ");
    private final static Label lblSideRealDayHr = new Label("Side Real Day Hr: ");
    private final static Label lblObliquityToOrbit = new Label("Obliquity To Orbit: ");
    private final static Label lblRadius = new Label("Radius: ");

    private final static DatePicker dateEphemStart = new DatePicker();
    private final static ComboBox<Integer> hourEphemStart = new ComboBox<>();
    private final static ComboBox<Integer> minEphemStart = new ComboBox<>();

    private final static DatePicker dateEphemStop = new DatePicker();
    private final static ComboBox<Integer> hourEphemStop = new ComboBox<>();
    private final static ComboBox<Integer> minEphemStop = new ComboBox<>();

    private final static ComboBox<StepSize> stepEphem = new ComboBox<>();

    private final static Button btnQueryEphem = new Button("Query Ephemeris");

    private final static CheckBox checkEphemFrozen = new CheckBox("Freeze Selected Ephemeris");
    private final static CheckBox checkDisableOrbitSelected = new CheckBox("Disable Selected Celestial Orbit Ring ");
    private final static CheckBox checkHideOrbitGlobal = new CheckBox("Hide Orbit Ring Behind Body");

    public static void initializePlanetViewer() {
        GridPane viewerGridRoot = new GridPane();
        selectedCelestialTab.setContent(viewerGridRoot);
        selectedCelestialTab.setClosable(false);

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

        GridPane.setConstraints(lblRadius, 0, 8);
        viewerGridRoot.getChildren().add(lblRadius);

        // VISUAL SEPARATION

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

        GridPane.setConstraints(btnQueryEphem, 0, 12);
        viewerGridRoot.getChildren().add(btnQueryEphem);

        // VISUAL SEPARATION

        GridPane.setConstraints(checkEphemFrozen, 0, 13);
        checkEphemFrozen.setAllowIndeterminate(false);
        viewerGridRoot.getChildren().add(checkEphemFrozen);

        GridPane.setConstraints(checkDisableOrbitSelected, 0, 14);
        checkDisableOrbitSelected.setAllowIndeterminate(false);
        viewerGridRoot.getChildren().add(checkDisableOrbitSelected);

        GridPane.setConstraints(checkHideOrbitGlobal, 0, 15);
        checkHideOrbitGlobal.setAllowIndeterminate(false);
        viewerGridRoot.getChildren().add(checkHideOrbitGlobal);

        initializeComboBoxes();
        initializeQueryButton();
        initializeCheckboxes();

        queryCelestialTab.setContent(queryGridPane);

        for(int i=199; i<=999; i=i+100) {
            int rowIndex = ((i / 100) - 1) * 2;
            CheckBox planetCheckBox = new CheckBox(i + " " + HorizonSystem.idNameMap.get(String.valueOf(i)));
            GridPane.setConstraints(planetCheckBox, 0, rowIndex);
            queryGridPane.getChildren().add(planetCheckBox);
            initializeQueryCheckbox(i, rowIndex, planetCheckBox);
        }

        Scene viewerScene = new Scene(tabPane, 400, 600);
        Stage viewerStage = new Stage();
        viewerStage.setTitle("Planet Viewer");
        viewerStage.setScene(viewerScene);
        viewerStage.show();
    }

    private static void initializeQueryCheckbox(int planetID, int rowIndex, CheckBox planetCheckbox) {
        GridPane moonGridPane = new GridPane();
        moonGridPane.setVisible(false);
        moonGridPane.setManaged(false);

        GridPane.setConstraints(moonGridPane, 0, rowIndex + 1);
        queryGridPane.getChildren().add(moonGridPane);

        // Begin at the first possible moonID, "[1-9]01"
        // Continues until HorizonSystem has no valid ID
        int row = 0;
        int col = 0;
        int moonID = planetID - 98;
        String moonName = HorizonSystem.idNameMap.get(String.valueOf(moonID));

        while(moonName != null) {
            CheckBox moonCheckbox = new CheckBox(String.valueOf(moonID));
            GridPane.setConstraints(moonCheckbox, col, row);
            moonGridPane.getChildren().add(moonCheckbox);

            moonCheckbox.selectedProperty().addListener(e-> {
                if(moonCheckbox.isSelected()) Moon.createMoon(moonCheckbox.getText(), moonCheckbox.getText().charAt(0) + "99");
                else Moon.deleteMoon(moonCheckbox.getText());
            });

            if(col == 8) { col = 0; row++; }
            moonID++;
            col++; // Col should only be updated at the end to give the indentation
            moonName = HorizonSystem.idNameMap.get(String.valueOf(moonID));
        }

        planetCheckbox.setOnMouseClicked(e -> {
            if(planetCheckbox.isSelected()) {
                Planet.createPlanet(planetCheckbox.getText().substring(0, 3));
                moonGridPane.setVisible(true);
                moonGridPane.setManaged(true);
            } else {
                Planet.deletePlanet(planetCheckbox.getText().substring(0, 3));
                moonGridPane.setVisible(false);
                moonGridPane.setManaged(false);
                for(Node node: moonGridPane.getChildren())
                    if(node instanceof CheckBox checkBox)
                        checkBox.setSelected(false);
            }
        });
    }

    private static void initializeComboBoxes() {
        for(StepSize step: StepSize.values()) {
            stepEphem.getItems().add(step);
        }

        for(int i=0; i<=24; i++) {
            hourEphemStart.getItems().add(i);
            hourEphemStop.getItems().add(i);
        }

        for(int i=0; i<=60; i++) {
            minEphemStart.getItems().add(i);
            minEphemStop.getItems().add(i);
        }
    }

    private static void initializeQueryButton() {
        btnQueryEphem.setOnMouseClicked(e -> {
            if(selectedCelestial instanceof SecondaryBody secBody && queryEphemInputCheck()) {
                secBody.setEphemeris(
                        dateEphemStart.getValue(),
                        hourEphemStart.getValue(),
                        minEphemStart.getValue(),
                        dateEphemStop.getValue(),
                        hourEphemStop.getValue(),
                        minEphemStop.getValue(),
                        stepEphem.getValue());
                PlanetsCamera.updateEphemeris(false);
                PlanetsCamera.updateCameraUI();
            }
        });
    }

    private static void initializeCheckboxes() {
        checkEphemFrozen.selectedProperty().addListener(e -> {
            if(selectedCelestial instanceof SecondaryBody secBody)
                secBody.setEphemFrozen(checkEphemFrozen.isSelected());
        });

        checkDisableOrbitSelected.selectedProperty().addListener(e -> {
            if(selectedCelestial instanceof SecondaryBody secBody)
                secBody.getOrbitRing().setVisible(!checkDisableOrbitSelected.isSelected());
        });
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
            lblRadius.setText("Radius: " + secondaryBody.getShape().getRadius() * HorizonSystem.pixelKmScale);

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
            checkEphemFrozen.setSelected(secondaryBody.isEphemFrozen());
            checkDisableOrbitSelected.setSelected(!secondaryBody.getOrbitRing().isVisible());
        }
    }

    public static boolean queryEphemInputCheck() {
        // Max limit per ephem query of 90024 rows
        // Each extra minute produces 4 extra rows, 90024 = 22506 * 4
        // No need for years case, already default
        ChronoUnit units = ChronoUnit.YEARS;
        switch (stepEphem.getValue()) {
            case MINUTES -> units = ChronoUnit.MINUTES;
            case HOURS -> units = ChronoUnit.HOURS;
            case DAYS -> units = ChronoUnit.DAYS;
            case MONTHS -> units = ChronoUnit.MONTHS;
        }

        long diff = getUnitDiff(units);
        return diff > 0 && diff <= 22505;
    }

    private static long getUnitDiff(ChronoUnit units) {
        LocalDateTime startDateTime = LocalDateTime.of(
                dateEphemStart.getValue().getYear(),
                dateEphemStart.getValue().getMonth(),
                dateEphemStart.getValue().getDayOfMonth(),
                hourEphemStart.getValue(),
                minEphemStart.getValue()
        );

        LocalDateTime stopDateTime = LocalDateTime.of(
                dateEphemStop.getValue().getYear(),
                dateEphemStop.getValue().getMonth(),
                dateEphemStop.getValue().getDayOfMonth(),
                hourEphemStop.getValue(),
                minEphemStop.getValue()
        );

        return startDateTime.until(stopDateTime, units);
    }

    public static boolean isHideOrbitGlobalSelected() {
        return checkHideOrbitGlobal.isSelected();
    }
}
