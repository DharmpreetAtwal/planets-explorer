package org.example.planetsexplorer;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.example.planetsexplorer.celestial.Celestial;
import org.example.planetsexplorer.celestial.SecondaryBody;

public class PlanetsCamera {
    public static final PerspectiveCamera camera = new PerspectiveCamera(true);

    private final static Rotate rotateX= new Rotate(0, Rotate.X_AXIS);
    private final static Rotate rotateY= new Rotate(0, Rotate.Y_AXIS);
    private final static Rotate rotateZ= new Rotate(0, Rotate.Z_AXIS);
    private final static Translate translate = new Translate(0, 0.0, -50);
    public static double diffZ = 0;

    public static final Color startColor = Color.BLUE;
    public static final Color midColor = Color.YELLOW;
    public static final Color endColor = Color.RED;

    private boolean isShiftToggle = false;
    private final Scene scene;

    public PlanetsCamera(Scene scene) {
        this.scene = scene;
        this.initializeKeyEvents();
        camera.setFarClip(2000000);
        camera.getTransforms().addAll(rotateX, rotateY, rotateZ, translate);
    }

    private void initializeKeyEvents() {
        this.scene.setOnKeyPressed((e) -> {
            if(e.getCode() == KeyCode.SHIFT) {
                this.isShiftToggle = !this.isShiftToggle;
            }

            // Only update the Z position of the camera when O/P are pressed
            boolean updateZ = false; // Initially false, set to true if O/P are pressed
            switch (e.getCode()) {
                case W -> rotateX.setAngle(rotateX.getAngle() + 10);
                case S -> rotateX.setAngle(rotateX.getAngle() - 10);
                case A -> rotateY.setAngle(rotateY.getAngle() + 10);
                case D -> rotateY.setAngle(rotateY.getAngle() - 10);

                case Q -> translate.setZ(translate.getZ() + 1000000);
                case E -> translate.setZ(translate.getZ() - 1000000);
                case Z -> translate.setZ(translate.getZ() + 1000);
                case X -> translate.setZ(translate.getZ() - 1000);

                case C -> PlanetViewer.selectedCelestial = null;

                case SPACE -> {
                    if(PlanetViewer.selectedCelestial != null) {
                        Point3D point = PlanetViewer.selectedCelestial.getShape().localToScene(Point3D.ZERO);
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

            updateEphemeris(updateZ);
            updateCameraUI();
        });
    }

    public static void updateEphemeris(boolean updateZ) {
        // If O/P were pressed, save distance between camera and selectedCelestial before moving Celestial
        if(!updateZ && PlanetViewer.selectedCelestial != null) {
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

            if(updateZ)
                translate.setZ(pos.getZ() - diffZ);
        }
    }

    public static void updateCameraUI() {
        for(Celestial celestial: Celestial.celestialArrayList) {
            updateCelestialUI(celestial);
            updateOrbitRing(celestial);
        }
    }

    private static void updateCelestialUI(Celestial celestial) {
        Point2D celestialPoint = celestial.getScreenCoordinates();
        celestial.getGroupUI().setTranslateX(celestialPoint.getX());
        celestial.getGroupUI().setTranslateY(celestialPoint.getY());
    }

    private static void updateOrbitRing(Celestial celestial) {
        if(celestial instanceof SecondaryBody body) {
            body.getOrbitRing().getChildren().clear();

            int totalSegments = body.getEphemerisData().size();
            int midSegment = totalSegments / 2;
            int originalIndex = body.getEphemerisIndex();

            // Initialize the first point of the orbit ring
            body.setEphemerisIndex(0);
            body.updateEphemerisPosition(false);
            Point2D currPointScreen = body.getScreenCoordinates();
            Point3D currPointScene = body.getSceneCoordinates();

            for(int i=1; i < totalSegments; i++) {
                body.setEphemerisIndex(i);
                body.updateEphemerisPosition(false);

                Point2D nextPointScreen = body.getScreenCoordinates();
                Point3D cameraPointScene = PlanetsCamera.camera.localToScene(Point3D.ZERO);
                Point3D primaryPointScene = body.getPrimaryBody().getSceneCoordinates();
                Point3D nextPointScene = body.getSceneCoordinates();

                if(cameraPointScene.distance(currPointScene) < 2 * body.getOrbitDistance()) {
                    currPointScreen = nextPointScreen;
                    currPointScene = nextPointScene;
                    continue;
                }

                if(PlanetViewer.isHideOrbitGlobalSelected()) {
                    // If segment is further away from the camera than the celestialObject it's modelling
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
                    segment.setStroke(mixColours(startColor, midColor, ratio));
                } else {
                    ratio = (double) (i - midSegment) / (totalSegments - midSegment);
                    segment.setStroke(mixColours(midColor, endColor, ratio));
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

    private static Color mixColours(Color start, Color end, double ratio) {
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

    public PerspectiveCamera getCamera() {
        return camera;
    }
}