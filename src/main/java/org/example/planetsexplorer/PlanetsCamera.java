package org.example.planetsexplorer;

import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

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
        this.camera.setFarClip(5000);
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

                case Q -> this.translate.setZ(this.translate.getZ() + 10);
                case E -> this.translate.setZ(this.translate.getZ() - 50);

                case P -> {
                    HorizonSystem.empherisIndex = HorizonSystem.empherisIndex + 1;
                    for(Planet planet: Planet.planetArrayList) {
                        if(!planet.getEphemData().isEmpty())
                            planet.setEphemIndex(HorizonSystem.empherisIndex % planet.getEphemData().size());
                    }
                }

                case O -> {
                    if(HorizonSystem.empherisIndex > 0) {
                        HorizonSystem.empherisIndex = HorizonSystem.empherisIndex - 1;
                        for(Planet planet: Planet.planetArrayList) {
                            if(!planet.getEphemData().isEmpty())
                                planet.setEphemIndex(HorizonSystem.empherisIndex % planet.getEphemData().size());
                        }
                    }
                }

                case LEFT -> this.translate.setX(this.translate.getX() - 15);
                case RIGHT -> this.translate.setX(this.translate.getX() + 15);
            }

        });
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