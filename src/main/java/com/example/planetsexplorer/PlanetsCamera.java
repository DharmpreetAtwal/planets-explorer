package com.example.planetsexplorer;

import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class PlanetsCamera {
    private final Rotate rotateX;
    private final Rotate rotateY;
    private final Rotate rotateZ;
    private final Translate translate;
    private final PerspectiveCamera camera;
    private boolean isShiftToggle = false;
    private final Scene scene;

    public PlanetsCamera(Scene scene) {
        this.rotateX = new Rotate(0, Rotate.X_AXIS);
        this.rotateY = new Rotate(0, Rotate.Y_AXIS);
        this.rotateZ = new Rotate(0, Rotate.Z_AXIS);
        this.translate = new Translate(0.0, 0.0, -4000.0);
        this.camera = new PerspectiveCamera(true);
        this.scene = scene;
        this.initializeKeyEvents();
        this.camera.setFarClip(50000);
        this.camera.getTransforms().addAll(this.rotateX, this.rotateY, this.rotateZ, this.translate);
    }

    private void initializeKeyEvents() {
        this.scene.setOnKeyPressed((e) -> {
            if(e.getCode() == KeyCode.SHIFT) {
                this.isShiftToggle = !this.isShiftToggle;
            }

                switch (e.getCode()) {
                    case W -> this.setTranslateY(this.getTranslateY() - 150.0);
                    case A -> this.setTranslateX(this.getTranslateX() - 150.0);
                    case S -> this.setTranslateY(this.getTranslateY() + 150.0);
                    case D -> this.setTranslateX(this.getTranslateX() + 150.0);
                    case Q -> this.setTranslateZ(this.getTranslateZ() + 500);
                    case E -> this.setTranslateZ(this.getTranslateZ() - 500);
                    case UP -> this.setRotateX(this.getRotateX() + 10);
                    case DOWN -> this.setRotateX(this.getRotateX() - 10);
                }
//            }

        });
    }

    public double getTranslateX() {
        return this.translate.getX();
    }

    public void setTranslateX(double x) {
        this.translate.setX(x);
    }

    public double getTranslateY() {
        return this.translate.getY();
    }

    public void setTranslateY(double y) {
        this.translate.setY(y);
    }

    public double getTranslateZ() {
        return this.translate.getZ();
    }

    public void setTranslateZ(double z) {
        this.translate.setZ(z);
    }

    public double getRotateX() {
        return this.rotateX.getAngle();
    }

    public void setRotateX(double rotateX) {
        this.rotateX.setAngle(rotateX);
    }

    public double getRotateY() {
        return this.rotateY.getAngle();
    }

    public void setRotateY(double rotateY) {
        this.rotateY.setAngle(rotateY);
    }

    public double getRotateZ() {
        return this.rotateZ.getAngle();
    }

    public void setRotateZ(double rotateZ) {
        this.rotateZ.setAngle(rotateZ);
    }

    public PerspectiveCamera getCamera() {
        return this.camera;
    }
}