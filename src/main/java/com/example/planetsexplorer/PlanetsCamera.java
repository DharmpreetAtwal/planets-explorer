package com.example.planetsexplorer;

import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class PlanetsCamera {
    private final Rotate rotateX;
    private final Rotate rotateY;
    private final Rotate rotateZ;
    private final Translate translate;
    private final PerspectiveCamera camera;
    private Scene scene;

    public PlanetsCamera(Scene scene) {
        this.rotateX = new Rotate(-20.0, Rotate.X_AXIS);
        this.rotateY = new Rotate(-20.0, Rotate.Y_AXIS);
        this.rotateZ = new Rotate(-20.0, Rotate.Z_AXIS);
        this.translate = new Translate(0.0, 0.0, -20.0);
        this.camera = new PerspectiveCamera(true);
        this.scene = null;
        this.scene = scene;
        this.initializeKeyEvents();
        this.camera.setFarClip(500.0);
        this.camera.getTransforms().addAll(new Transform[]{this.rotateX, this.rotateY, this.rotateZ, this.translate});
    }

    private void initializeKeyEvents() {
        this.scene.setOnKeyPressed((e) -> {
            switch (e.getCode()) {
                case W -> this.setTranslateZ(this.getTranslateZ() + 10.0);
                case S -> this.setTranslateZ(this.getTranslateZ() - 10.0);
                case A -> this.setRotateZ(this.getRotateZ() + 10.0);
                case D -> this.setRotateZ(this.getRotateZ() - 10.0);
            }

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