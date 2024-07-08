package org.example.planetsexplorer;

import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.example.planetsexplorer.celestial.Celestial;
import org.example.planetsexplorer.celestial.Planet;
import org.example.planetsexplorer.celestial.SecondaryBody;

public class PlanetsCamera {
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Rotate rotateX= new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY= new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ= new Rotate(0, Rotate.Z_AXIS);
    private final Translate translate = new Translate(0, 0.0, -50);
    private boolean isShiftToggle = false;
    private final Scene scene;

    public PlanetsCamera(Scene scene) {
        this.scene = scene;
        this.initializeKeyEvents();
        this.camera.setFarClip(20000);
        this.camera.getTransforms().addAll(this.rotateX, this.rotateY, this.rotateZ, this.translate);
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

                case Z -> this.translate.setZ(this.translate.getZ() + 10000);
                case X -> this.translate.setZ(this.translate.getZ() - 10000);

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
                    if(Main.selectedCelestial != null) {
                        Point3D point = Main.selectedCelestial.getShape().localToScene(Point3D.ZERO);
                        this.translate.setZ(-Main.selectedCelestial.getShape().getRadius()*5 + point.getZ());
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
        if(Main.selectedCelestial != null) {
            Point3D pos = Main.selectedCelestial.getShape().localToScene(Point3D.ZERO);
            Main.updateCameraTranslate(pos.getX(),pos.getY());
            Main.updateCameraPivot(Main.selectedCelestial.getShape().localToScene(Point3D.ZERO));
        }
    }

    public static void updateUIPosition() {
        for(Celestial celestial: Celestial.celestialArrayList) {
            Point3D celestialPoint = celestial.getShape().localToScene(Point3D.ZERO, true);
            celestial.getGroupUI().setTranslateX(celestialPoint.getX());
            celestial.getGroupUI().setTranslateY(celestialPoint.getY());

            if(celestial instanceof SecondaryBody) {
                SecondaryBody body = (SecondaryBody)celestial;
                body.getOrbitRing().getElements().clear();
                body.getOrbitRotation().setAngle(body.getEphemData().get(0).getFloat("ma"));
                Point3D lastPoint = body.getShape().localToScene(Point3D.ZERO, true);
                body.getOrbitRing().getElements().add(new MoveTo(lastPoint.getX(), lastPoint.getY()));

                for(int i=1; i < body.getEphemData().size(); i++) {
                    body.getOrbitRotation().setAngle(body.getEphemData().get(i).getFloat("ma"));
                    Point3D nextPoint = body.getShape().localToScene(Point3D.ZERO, true);
                    body.getOrbitRing().getElements().add(new LineTo(nextPoint.getX(), nextPoint.getY()));
                }
            }
        }
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