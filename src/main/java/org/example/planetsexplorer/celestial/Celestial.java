package org.example.planetsexplorer.celestial;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.shape.Sphere;
import org.example.planetsexplorer.PlanetViewer;
import org.example.planetsexplorer.PlanetsCamera;

import java.util.ArrayList;

/**
 * A {@code Celestial} is a major body in NASA's
 * {@link org.example.planetsexplorer.HorizonSystem HorizonSystem}.
 * {@code Celestial} contains all the fields that every modelled body in the database
 * needs in the program.
 *
 * @author Dharmpreet Atwal
 * @see PrimaryBody
 * @see Sun
 * @see SecondaryBody
 * @see Planet
 * @see Moon
 * @see Spacecraft
 */
public abstract class Celestial {
    /**
     * A list of all constructed {@code Celestial} objects
     */
    public static final ArrayList<Celestial> celestialArrayList = new ArrayList<>();

    /**
     * The unique name of the {@code Celestial}
     */
    private final String name;

    /**
     * The ID used to represent the {@code Celestial} in the database. Unique to each body.
     */
    private final String dbID;

    /**
     * The JavaFX Node used to represent the 3D model of the {@code Celestial} in the Scene.
     */
    private final Sphere shape;

    /**
     * The variables that controls how many polygons the 3D sphere representation of the
     * Celestial should have. Keep this value low to reduce graphical load.
     *
     * @see Celestial#shape
     */
    private static final int shapeDivisions = 2;

    /**
     * The Group node that contains all the 2D UI elements associated with this {@code Celestial}
     */
    private final Group groupUI = new Group();

    /**
     * The 2D UI label that displays the {@code Celestial} name and database ID
     */
    private final Label labelName = new Label();

    /**
     * Constructs a {@code Celestial} given a unique name, database ID, and physical radius.
     *
     * @param name The unique title.
     * @param dbID The unique database id.
     * @param shapeRadius The radius of the body's shape.
     */
    public Celestial(String name, String dbID, float shapeRadius) {
        this.name = name;
        this.dbID = dbID;
        this.shape = new Sphere(shapeRadius, shapeDivisions);

        this.labelName.setText(name + " " + dbID);
        this.labelName.setStyle("-fx-background-color: white; -fx-border-color: black;");
        this.groupUI.getChildren().add(this.labelName);
        this.groupUI.setOpacity(0.5);
        initializeUIMouseEvents();

        celestialArrayList.add(this);
    }

    /**
     * Initializes the actions taken when the UI elements are clicked on. Only
     * called at construction. The control flow of this method depends on what
     * kind of {@code Celestial} this is, the type of {@link PlanetViewer#selectedCelestial},
     * and the values of {@link PlanetViewer#selectPrimary}, and {@link PlanetViewer#copyEphemeris}.
     */
    private void initializeUIMouseEvents() {
        this.labelName.setOnMouseClicked(e -> {
            // Decide whether to copy ephemeris, change the PrimaryBody, or to select a new Celestial
            Celestial selected = PlanetViewer.selectedCelestial;

            // Both this Celestial / selectedCelestial are SecondaryBody's, copyEphemeris is toggled
            // Copy this Celestial's ephemeris data range to the selectedCelestial's
            if(selected instanceof SecondaryBody selectedBody
                    && this instanceof SecondaryBody clickedBody
                    && PlanetViewer.copyEphemeris) {
                selectedBody.copyEphemerisDateRange(clickedBody);
                PlanetViewer.copyEphemeris = false;

            // This celestial is PrimaryBody, selectedCelestial is Spacecraft, selectPrimary is toggled
            // Set spacecraft's primaryBody to be this celestial
            } else if(selected instanceof Spacecraft spacecraft
                    && this instanceof PrimaryBody clickedBody
                    && PlanetViewer.selectPrimary) {
                spacecraft.changePrimaryBody(clickedBody);
                clickedBody.addSecondaryBody(spacecraft);
                spacecraft.initializeEphemeris();

                PlanetViewer.selectPrimary = false;

            // If no flags set, or an invalid selection for copy ephemeris / change primaryBody
            // Move the camera to the Celestial's Position
            } else {
                Point3D shapePos = this.shape.localToScene(Point3D.ZERO);

                PlanetsCamera.updateTranslate(shapePos.getX(), shapePos.getY());
                PlanetsCamera.updatePivot(shapePos);

                PlanetViewer.setSelectedCelestial(this);
                PlanetsCamera.updateEphemeris();
                PlanetsCamera.updateCameraUI();
            }
        });
    }

    /**
     * Project the shape's 3D position onto the screen, and return this 2D point.
     * @return The shape's 2D projection onto the screen
     */
    public Point2D getScreenCoordinates() {
        Point3D screenPoint = this.shape.localToScene(Point3D.ZERO, true);
        return new Point2D(screenPoint.getX(), screenPoint.getY());
    }

    /**
     * Return the global 3D scene coordinates of the shape.
     * @return The shape's 3D scene position.
     */
    public Point3D getSceneCoordinates() {
        return this.shape.localToScene(Point3D.ZERO);
    }

    public String getName() {
        return name;
    }

    public Sphere getShape() {
        return shape;
    }

    public Group getGroupUI() {
        return groupUI;
    }

    public String getDbID() {
        return dbID;
    }
}
