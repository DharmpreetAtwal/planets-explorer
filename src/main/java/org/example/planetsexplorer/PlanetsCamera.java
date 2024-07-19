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
import org.example.planetsexplorer.celestial.Planet;
import org.example.planetsexplorer.celestial.SecondaryBody;

public class PlanetsCamera {
    public static final PerspectiveCamera camera = new PerspectiveCamera(true);
    public static double diffZ = 0;
    private final Rotate rotateX= new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY= new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ= new Rotate(0, Rotate.Z_AXIS);
    private final static Translate translate = new Translate(0, 0.0, -50);
    private boolean isShiftToggle = false;
    private final Scene scene;

    public PlanetsCamera(Scene scene) {
        this.scene = scene;
        this.initializeKeyEvents();
        camera.setFarClip(20000);
        camera.getTransforms().addAll(this.rotateX, this.rotateY, this.rotateZ, translate);
    }

    private void initializeKeyEvents() {
        this.scene.setOnKeyPressed((e) -> {
            if(e.getCode() == KeyCode.SHIFT) {
                this.isShiftToggle = !this.isShiftToggle;
            }

            boolean updateZ = false;
            // Only update the orbit ring UI when camera is transformed
            // Only update the orbit ring UI when O/P are NOT pressed
            // By default true, set to false if O/P are pressed
            switch (e.getCode()) {
                case W -> this.rotateX.setAngle(this.rotateX.getAngle() + 10);
                case S -> this.rotateX.setAngle(this.rotateX.getAngle() - 10);

                case A -> this.rotateY.setAngle(this.rotateY.getAngle() + 10);
                case D -> this.rotateY.setAngle(this.rotateY.getAngle() - 10);

                case C -> PlanetViewer.selectedCelestial = null;

                case Q -> translate.setZ(translate.getZ() + 1000000);
                case E -> translate.setZ(translate.getZ() - 1000000);
                case Z -> translate.setZ(translate.getZ() + 1000);
                case X -> translate.setZ(translate.getZ() - 1000);

                case P -> {
                    HorizonSystem.empherisIndex = HorizonSystem.empherisIndex + 1;
                    updateZ = true;
                }

                case O -> {
                    if(HorizonSystem.empherisIndex > 0) {
                        HorizonSystem.empherisIndex = HorizonSystem.empherisIndex - 1;
                        updateZ = true;
                    }
                }

                case SPACE -> {
                    if(PlanetViewer.selectedCelestial != null) {
                        Point3D point = PlanetViewer.selectedCelestial.getShape().localToScene(Point3D.ZERO);
                        translate.setZ(-PlanetViewer.selectedCelestial.getShape().getRadius()*5 + point.getZ());
                    }
                }
            }

            if(!updateZ && PlanetViewer.selectedCelestial != null) {
                Point3D pos = PlanetViewer.selectedCelestial.getShape().localToScene(Point3D.ZERO);
                diffZ = Math.abs(pos.getZ() - translate.getZ());
            }

            updateEphemeris(updateZ);
            updateUIPosition();
        });
    }

    public static void updateEphemeris(boolean updateZ) {
        for(Celestial celestial: Celestial.celestialArrayList) {
            if(celestial instanceof SecondaryBody secbody) {
                if(!secbody.getEphemData().isEmpty() && !secbody.isEphemFrozen()){
                    secbody.setEphemIndex(HorizonSystem.empherisIndex % secbody.getEphemData().size());
                }
            }
        }

        if(PlanetViewer.selectedCelestial != null) {
            Point3D pos = PlanetViewer.selectedCelestial.getShape().localToScene(Point3D.ZERO);
            Main.updateCameraTranslate(pos.getX(),pos.getY());
            Main.updateCameraPivot(PlanetViewer.selectedCelestial.getShape().localToScene(Point3D.ZERO));

            if(updateZ) translate.setZ(pos.getZ() + diffZ);
        }
    }

    public static void updateUIPosition() {
        for(Celestial celestial: Celestial.celestialArrayList) {
            Point2D celestialPoint = celestial.getScreenCoordinates();
            celestial.getGroupUI().setTranslateX(celestialPoint.getX());
            celestial.getGroupUI().setTranslateY(celestialPoint.getY());

            if(celestial instanceof SecondaryBody body) {
                body.getOrbitRing().getChildren().clear();

                Color startColor = Color.BLUE;
                Color midColor = Color.YELLOW;
                Color endColor = Color.RED;

                int totalSegments = body.getEphemData().size();
                int midSegment = totalSegments / 2;
                double originalAngle = body.getOrbitRotation().getAngle();

                body.getOrbitRotation().setAngle(body.getEphemData().get(0).getFloat("ma"));
                Point2D lastPointScreen = body.getScreenCoordinates();
                Point3D lastPointGlobal = body.getSceneCoordinates();

                for(int i=1; i < totalSegments; i++) {
                    body.getOrbitRotation().setAngle(body.getEphemData().get(i).getFloat("ma"));
                    Point2D nextPointScreen = body.getScreenCoordinates();

                    Point3D cameraPosGlobal = PlanetsCamera.camera.localToScene(Point3D.ZERO);
                    Point3D celestialPrimaryPointGlobal = body.getPrimaryBody().getSceneCoordinates();
                    Point3D nextPointGlobal = body.getSceneCoordinates();

                    // If segment is further away from the camera than the celestialObject it's modelling
//                    if(cameraPosGlobal.distance(nextPointGlobal) > cameraPosGlobal.distance(celestialPrimaryPointGlobal) + (body.getOrbitDistance() / 2) ||
//                            cameraPosGlobal.distance(lastPointGlobal) > cameraPosGlobal.distance(celestialPrimaryPointGlobal) + (body.getOrbitDistance() / 2)) {
//                        lastPointScreen = nextPointScreen;
//                        lastPointGlobal = nextPointGlobal;
//                        continue;
//                    }

                    Line segment = new Line(lastPointScreen.getX(), lastPointScreen.getY(), nextPointScreen.getX(), nextPointScreen.getY());
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

                    lastPointScreen = nextPointScreen;
                    lastPointGlobal = nextPointGlobal;
                }

                body.getOrbitRotation().setAngle(originalAngle);
            }
        }
    }

    private static Color mixColours(Color start, Color end, double ratio) {
        double r = start.getRed() + (end.getRed() - start.getRed()) * ratio;
        double g = start.getGreen() + (end.getGreen() - start.getGreen()) * ratio;
        double b = start.getBlue() + (end.getBlue() - start.getBlue()) * ratio;
        return new Color(r, g, b, 1.0);
    }

    public Translate getTranslate() {
        return translate;
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public Rotate getRotateX() {
        return rotateX;
    }

    public Rotate getRotateY() {
        return rotateY;
    }

    public Rotate getRotateZ() {
        return rotateZ;
    }
}