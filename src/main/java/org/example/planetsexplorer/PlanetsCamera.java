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
    private final Rotate rotateX= new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY= new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ= new Rotate(0, Rotate.Z_AXIS);
    private final Translate translate = new Translate(0, 0.0, -50);
    private boolean isShiftToggle = false;
    private final Scene scene;

    public PlanetsCamera(Scene scene) {
        this.scene = scene;
        this.initializeKeyEvents();
        camera.setFarClip(20000);
        camera.getTransforms().addAll(this.rotateX, this.rotateY, this.rotateZ, this.translate);
    }

    private void initializeKeyEvents() {
        this.scene.setOnKeyPressed((e) -> {
            if(e.getCode() == KeyCode.SHIFT) {
                this.isShiftToggle = !this.isShiftToggle;
            }

            switch (e.getCode()) {
                case W -> this.rotateX.setAngle(this.rotateX.getAngle() + 10);
                case S -> this.rotateX.setAngle(this.rotateX.getAngle() - 10);

                case A -> this.rotateY.setAngle(this.rotateY.getAngle() + 10);
                case D -> this.rotateY.setAngle(this.rotateY.getAngle() - 10);

                case Q -> this.translate.setZ(this.translate.getZ() + 1000000);
                case E -> this.translate.setZ(this.translate.getZ() - 1000000);

                case Z -> this.translate.setZ(this.translate.getZ() + 1000);
                case X -> this.translate.setZ(this.translate.getZ() - 1000);

                case P -> {
                    HorizonSystem.empherisIndex = HorizonSystem.empherisIndex + 1;
                    updateEphemeris();
                }

                case O -> {
                    if(HorizonSystem.empherisIndex > 0) {
                        HorizonSystem.empherisIndex = HorizonSystem.empherisIndex - 1;
                        updateEphemeris();
                    }
                }

                case SPACE -> {
                    if(PlanetViewer.selectedCelestial != null) {
                        Point3D point = PlanetViewer.selectedCelestial.getShape().localToScene(Point3D.ZERO);
                        this.translate.setZ(-PlanetViewer.selectedCelestial.getShape().getRadius()*5 + point.getZ());
                    }
                }
            }

            updateUIPosition();
        });
    }

    private void updateEphemeris() {
        for(Planet planet: Planet.planetArrayList) {
            if(!planet.getEphemData().isEmpty()) {
//                planet.setEphemIndex(HorizonSystem.empherisIndex % planet.getEphemData().size());
            }

            for(SecondaryBody secondaryBody: planet.getSecondaryBodies()) {
                secondaryBody.setEphemIndex(HorizonSystem.empherisIndex % secondaryBody.getEphemData().size());
            }
        }
        if(PlanetViewer.selectedCelestial != null) {
            Point3D pos = PlanetViewer.selectedCelestial.getShape().localToScene(Point3D.ZERO);
            Main.updateCameraTranslate(pos.getX(),pos.getY());
            Main.updateCameraPivot(PlanetViewer.selectedCelestial.getShape().localToScene(Point3D.ZERO));
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
                    if(cameraPosGlobal.distance(nextPointGlobal) > cameraPosGlobal.distance(celestialPrimaryPointGlobal) + (body.getOrbitDistance() / 2) ||
                            cameraPosGlobal.distance(lastPointGlobal) > cameraPosGlobal.distance(celestialPrimaryPointGlobal) + (body.getOrbitDistance() / 2)) {
                        lastPointScreen = nextPointScreen;
                        lastPointGlobal = nextPointGlobal;
                        continue;
                    }

                    Line segment = new Line(lastPointScreen.getX(), lastPointScreen.getY(), nextPointScreen.getX(), nextPointScreen.getY());
                    segment.setOpacity(1 - ((double) i / totalSegments));
                    segment.setStrokeWidth(5);
                    body.getOrbitRing().getChildren().add(segment);

                    // Determining the mix of two gradient colors
                    double ratio;
                    if (i <= midSegment) {
                        ratio = (double) i / midSegment;
                        segment.setStroke(mixColours(startColor, midColor, ratio));
                    } else {
                        ratio = (double) (i - midSegment) / (totalSegments - midSegment);
                        segment.setStroke(mixColours(midColor, endColor, ratio));
                    }

                    lastPointScreen = nextPointScreen;
                    lastPointGlobal = nextPointGlobal;
                }
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