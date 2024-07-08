package org.example.planetsexplorer;

import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
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

                case Z -> this.translate.setZ(this.translate.getZ() + 10);
                case X -> this.translate.setZ(this.translate.getZ() - 10);

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
                planet.setEphemIndex(HorizonSystem.empherisIndex % planet.getEphemData().size());
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
                Point3D primaryBodyPoint = body.getPrimaryBody().getShape().localToScene(Point3D.ZERO, true);
                Point3D primaryBodyPointSec = celestial.getShape().sceneToLocal(primaryBodyPoint);
                Point3D finalPoint = celestial.getShape().localToScene(primaryBodyPointSec, true);

                body.getOrbitRing().setCenterX(finalPoint.getX());
                body.getOrbitRing().setCenterY(finalPoint.getY());

                double angle = body.getOrbitRotation().getAngle();
                body.getOrbitRotation().setAngle(0);

                Point3D diffVec =  celestial.getShape().localToScene(Point3D.ZERO, true).subtract(finalPoint);
                body.getOrbitRing().setRadiusX(Math.abs(diffVec.getX()));

                body.getOrbitRotation().setAngle(90);
                diffVec =  celestial.getShape().localToScene(Point3D.ZERO, true).subtract(finalPoint);
                body.getOrbitRing().setRadiusY(Math.abs(diffVec.getY()));

                body.getOrbitRotation().setAngle(angle);
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