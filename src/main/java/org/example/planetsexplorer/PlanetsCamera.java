package org.example.planetsexplorer;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.example.planetsexplorer.celestial.Celestial;
import org.example.planetsexplorer.celestial.SecondaryBody;

/**
 * The camera used to navigate throughout the 3D scene. {@code PlanetsCamera}
 * attaches the keypress event handlers and their associated transformations
 * or ephemeris updates. {@code PlanetsCamera} also handles updates to the
 * 2D UI overlay that visualizes the orbit of a given {@link Celestial}.
 */
public final class PlanetsCamera {
    /**
     * The JavaFX Node that represents the 3D camera in the scene.
     */
    private final static PerspectiveCamera camera = new PerspectiveCamera(true);

    /**
     * The camera's rotation about the X-axis
     */
    private final static Rotate rotateX= new Rotate(0, Rotate.X_AXIS);

    /**
     * The camera's rotation about the Y-axis
     */
    private final static Rotate rotateY= new Rotate(0, Rotate.Y_AXIS);

    /**
     * The camera's rotation about the Z-axis
     */
    private final static Rotate rotateZ= new Rotate(0, Rotate.Z_AXIS);

    /**
     * The camera's X-Y-Z translation.
     */
    private final static Translate translate = new Translate(0, 0.0, -50);

    /**
     * Keeps track of the Z coordinate difference between the camera and the
     * {@link PlanetViewer#selectedCelestial}. Only changed before an ephemeris
     * update.
     *
     * @see PlanetsCamera#updateEphemeris()
     */
    private static double diffZ = 0;

    /**
     * The starting color in the orbit path gradient.
     */
    private static final Color startColor = Color.BLUE;

    /**
     * The middle color in the orbit path gradient.
     */
    private static final Color midColor = Color.YELLOW;

    /**
     * The ending color in the orbit path gradient.
     */
    private static final Color endColor = Color.RED;

    /**
     * The maximum distance the camera will render 3D objects.
     */
    private static final int maxFarClip = 2000000;

    /**
     * The increment used in any rotation transformations.
     */
    private static final int angleIncrement = 10;

    /**
     * The larger increment for zooming the camera in or out
     */
    private static final int maxZoomIncrement = 1000000;

    /**
     * The smaller increment for zooming the camera in or out
     */
    private static final int minZoomIncrement = 1000;

    /**
     * Don't let this class be instantiated
     */
    private PlanetsCamera(){}

    /**
     * A method called in {@link Main} that initializes the scene components.
     *
     * @param mainScene The scene from which key press events will be read from
     * @param scene3D The 3D {@code SubScene} that will contain the camera
     * @param rootScene3D The root of the 3D {@code SubScene}.
     */
    public static void initializeCamera(Scene mainScene, SubScene scene3D, Group rootScene3D) {
        initializeKeyEvents(mainScene);
        camera.setFarClip(maxFarClip);
        camera.getTransforms().addAll(rotateX, rotateY, rotateZ, translate);

        scene3D.setCamera(camera);
        rootScene3D.getChildren().add(camera);
    }

    /**
     * Only called once to initialize the keypress events of a given scene.
     *
     * @param mainScene The scene from which keypress events will be read.
     */
    private static void initializeKeyEvents(Scene mainScene) {
        mainScene.setOnKeyPressed((e) -> {
            // Only update the Z position of the camera when O/P are pressed
            boolean updateZ = false; // Initially false, set to true if O/P are pressed
            switch (e.getCode()) {
                case W -> rotateX.setAngle(rotateX.getAngle() + angleIncrement);
                case S -> rotateX.setAngle(rotateX.getAngle() - angleIncrement);
                case A -> rotateY.setAngle(rotateY.getAngle() + angleIncrement);
                case D -> rotateY.setAngle(rotateY.getAngle() - angleIncrement);

                case Q -> translate.setZ(translate.getZ() + maxZoomIncrement);
                case E -> translate.setZ(translate.getZ() - maxZoomIncrement);
                case Z -> translate.setZ(translate.getZ() + minZoomIncrement);
                case X -> translate.setZ(translate.getZ() - minZoomIncrement);

                case C -> PlanetViewer.selectedCelestial = null;

                case SPACE -> {
                    // Zoom the camera into the selectedCelestial, and then zoom it out a bit so that
                    // the camera doesn't clip into the shape
                    if(PlanetViewer.selectedCelestial != null) {
                        Point3D point = PlanetViewer.selectedCelestial.getSceneCoordinates();
                        translate.setZ(point.getZ() - PlanetViewer.selectedCelestial.getShape().getRadius()*5);
                    }
                }

                case P -> {
                    HorizonSystem.ephemerisIndex = HorizonSystem.ephemerisIndex + 1;
                    updateZ = true;
                }

                case O -> {
                    if(HorizonSystem.ephemerisIndex > 0) {
                        HorizonSystem.ephemerisIndex = HorizonSystem.ephemerisIndex - 1;
                        updateZ = true;
                    }
                }
            }

            // Only call this method if O/P were pressed
            if(updateZ) updateEphemeris();
            updateCameraUI();
        });
    }

    /**
     * Iterates through each {@link Celestial} and updates their ephemeris index
     * to match the {@link HorizonSystem} index.
     *
     * @see PlanetViewer#selectedCelestial
     */
    public static void updateEphemeris() {
        // Save distance between camera and selectedCelestial before moving Celestial
        if(PlanetViewer.selectedCelestial != null) {
            Point3D pos = PlanetViewer.selectedCelestial.getShape().localToScene(Point3D.ZERO);
            diffZ = pos.getZ() - translate.getZ();
        }

        // Moving all celestials if they are SecondaryBody and not frozen
        for(Celestial celestial: Celestial.celestialArrayList) {
            if(celestial instanceof SecondaryBody secBody && !secBody.getEphemerisData().isEmpty()) {
                if(!secBody.isEphemerisFrozen())
                    secBody.setEphemerisIndex(HorizonSystem.ephemerisIndex);
                secBody.updateEphemerisPosition(true);
            }
        }

        // After selectedCelestial is moved, update camera's position
        // Maintains the original diffZ from before Celestial was moved
        if(PlanetViewer.selectedCelestial != null) {
            Point3D pos = PlanetViewer.selectedCelestial.getShape().localToScene(Point3D.ZERO);
            updateTranslate(pos.getX(),pos.getY());
            updatePivot(PlanetViewer.selectedCelestial.getShape().localToScene(Point3D.ZERO));
            translate.setZ(pos.getZ() - diffZ);
        }
    }

    /**
     * Updates the 2D overlay elements associated with each {@code Celestial}
     */
    public static void updateCameraUI() {
        for(Celestial celestial: Celestial.celestialArrayList) {
            updateCelestialUI(celestial);
            updateOrbitRing(celestial);
        }
    }

    /**
     * Updates the 2D UI elements associated with a given {@code Celestial}
     * @param celestial
     */
    private static void updateCelestialUI(Celestial celestial) {
        Point2D celestialPoint = celestial.getScreenCoordinates();
        celestial.getGroupUI().setTranslateX(celestialPoint.getX());
        celestial.getGroupUI().setTranslateY(celestialPoint.getY());
    }

    /**
     * Updates a given {@code Celestial} 2D overlay orbit visualization
     * @param celestial The celestial whose orbit ring is to be updated.
     */
    private static void updateOrbitRing(Celestial celestial) {
        if(celestial instanceof SecondaryBody body) {
            // Reset the ring visualization
            body.getOrbitRing().getChildren().clear();

            int totalSegments = body.getEphemerisData().size();
            int midSegment = totalSegments / 2;
            int originalIndex = body.getEphemerisIndex();

            // Initialize the first point of the orbit ring
            body.setEphemerisIndex(0);
            body.updateEphemerisPosition(false);
            Point2D currPointScreen = body.getScreenCoordinates();
            Point3D currPointScene = body.getSceneCoordinates();

            // Iterate through each point in the ephemeris
            for(int i=1; i < totalSegments; i++) {
                body.setEphemerisIndex(i);
                body.updateEphemerisPosition(false);

                Point2D nextPointScreen = body.getScreenCoordinates();
                Point3D cameraPointScene = PlanetsCamera.camera.localToScene(Point3D.ZERO);
                Point3D primaryPointScene = body.getPrimaryBody().getSceneCoordinates();
                Point3D nextPointScene = body.getSceneCoordinates();

                // If camera is close to body, hide its orbit ring
                if(cameraPointScene.distance(currPointScene) < 2 * body.getOrbitDistance()) {
                    currPointScreen = nextPointScreen;
                    currPointScene = nextPointScene;
                    continue;
                }

                if(PlanetViewer.isHideOrbitGlobalSelected()) {
                    // If segment is further away from the camera than the celestialObject it's modelling, hide that segment
                    if(cameraPointScene.distance(nextPointScene) > cameraPointScene.distance(primaryPointScene) + (body.getOrbitDistance() / 2) ||
                            cameraPointScene.distance(currPointScene) > cameraPointScene.distance(primaryPointScene) + (body.getOrbitDistance() / 2)) {
                        currPointScreen = nextPointScreen;
                        currPointScene = nextPointScene;
                        continue;
                    }
                }

                Line segment = new Line(currPointScreen.getX(), currPointScreen.getY(),
                        nextPointScreen.getX(), nextPointScreen.getY());
                double ratio; // Determining the mix of two gradient colors
                if (i <= midSegment) {
                    ratio = (double) i / midSegment;
                    segment.setStroke(mixColors(startColor, midColor, ratio));
                } else {
                    ratio = (double) (i - midSegment) / (totalSegments - midSegment);
                    segment.setStroke(mixColors(midColor, endColor, ratio));
                }

                segment.setOpacity(1 - ((double) i / totalSegments));
                segment.setStrokeWidth(5);
                body.getOrbitRing().getChildren().add(segment);

                currPointScreen = nextPointScreen;
                currPointScene = nextPointScene;
            }

            body.setEphemerisIndex(originalIndex);
            body.updateEphemerisPosition(true);
        }
    }

    /**
     * Helper method that returns the fractional mix of two colors
     * @param start The dominant colour
     * @param end The color being mixed in
     * @param ratio The fractional mix of the two colors
     * @return The mix of start and end colors
     */
    private static Color mixColors(Color start, Color end, double ratio) {
        double r = start.getRed() + (end.getRed() - start.getRed()) * ratio;
        double g = start.getGreen() + (end.getGreen() - start.getGreen()) * ratio;
        double b = start.getBlue() + (end.getBlue() - start.getBlue()) * ratio;
        return new Color(r, g, b, 1.0);
    }

    public static void updateTranslate(double x, double y) {
        translate.setX(x);
        translate.setY(y);
    }

    public static void updatePivot(Point3D pivot) {
        rotateX.setPivotX(pivot.getX());
        rotateX.setPivotY(pivot.getY());
        rotateX.setPivotZ(pivot.getZ());

        rotateY.setPivotX(pivot.getX());
        rotateY.setPivotY(pivot.getY());
        rotateY.setPivotZ(pivot.getZ());

        rotateZ.setPivotX(pivot.getX());
        rotateZ.setPivotY(pivot.getY());
        rotateZ.setPivotZ(pivot.getZ());
    }
}