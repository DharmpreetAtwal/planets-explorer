package com.example.planetsexplorer;

import java.util.ArrayList;

import javafx.animation.*;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Planet {
    public static ArrayList<Planet> planetArrayList = new ArrayList<>();
    private final Sphere shape;
    private Planet primaryBody = null;
    private float orbitDistance = 0.0F;
    private final Rotate orbitRotation = new Rotate();
    private final ArrayList<Planet> secondaryBodies = new ArrayList<>();
    private final float orbitPeriodYear;
    private final float siderealDayHr;
    private final float obliquityToOrbitDeg;
    private MeshView orbitRing = null;
    private RotateTransition planetSpin;

    public Planet(float shapeRadius, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg, float translateX, float translateY) {
        this.shape = new Sphere(shapeRadius, 2);
        this.orbitPeriodYear = orbitPeriodYear;
        this.siderealDayHr = siderealDayHr;
        this.obliquityToOrbitDeg = obliquityToOrbitDeg;

        this.shape.setMaterial(new PhongMaterial(Color.ORANGE));
        this.shape.setTranslateX(translateX);
        this.shape.setTranslateY(translateY);
        this.shape.getTransforms().add(orbitRotation);
//        this.initializeSpinAnimation();
        planetArrayList.add(this);
    }

    private void initializeSpinAnimation() {
        this.planetSpin = new RotateTransition(Duration.seconds(Math.abs(this.siderealDayHr)), this.shape);
//        this.planetSpin.setInterpolator(Interpolator.LINEAR);

        Rotate rot = new Rotate(-this.obliquityToOrbitDeg, Rotate.Y_AXIS);
        this.shape.getTransforms().add(rot);

        Point3D titledAxis = new Point3D(
                Math.sin(Math.toRadians(this.obliquityToOrbitDeg)),
                0,
                -Math.cos(Math.toRadians(this.obliquityToOrbitDeg)));
        this.planetSpin.setAxis(titledAxis);

        if(this.siderealDayHr > 0) {
            this.planetSpin.setFromAngle(0);
            this.planetSpin.setToAngle(360);
            planetSpin.setByAngle(1);
        }
        else {
            this.planetSpin.setFromAngle(0);
            this.planetSpin.setToAngle(-360);
            planetSpin.setByAngle(-1);
        }

        planetSpin.setCycleCount(-1);
        planetSpin.play();
    }

    public void setOrbitDistance(float distance) {
        this.orbitDistance = distance;
    }

    public void setPrimaryBody(Planet planet) {
        this.primaryBody = planet;
        planet.addSecondaryBody(this);

        this.orbitRing = createRing(this.orbitDistance, this.orbitDistance + 1, 5, 30);
        this.shape.setTranslateX(this.primaryBody.shape.getTranslateX() + (double)this.orbitDistance);
        this.shape.setTranslateY(this.primaryBody.shape.getTranslateY());
        this.shape.setTranslateZ(this.primaryBody.shape.getTranslateZ());
    }

    private void addSecondaryBody(Planet secondaryBody) {
        this.secondaryBodies.add(secondaryBody);
        secondaryBody.shape.setTranslateX(this.shape.getTranslateX());
        secondaryBody.shape.setTranslateY(this.shape.getTranslateY());
        secondaryBody.shape.setTranslateZ(this.shape.getTranslateZ() + (double)secondaryBody.orbitDistance);
    }

    public void animateSecondaryBodies() {
        this.secondaryBodies.forEach(body ->{
            body.getOrbitRotation().pivotXProperty().bind(
                    this.shape.translateXProperty().subtract(body.getShape().translateXProperty()));
            body.getOrbitRotation().pivotYProperty().bind(
                    this.shape.translateYProperty().subtract(body.getShape().translateYProperty()));
            body.getOrbitRotation().pivotZProperty().bind(
                    this.shape.translateZProperty().subtract(body.getShape().translateZProperty()));

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(body.getOrbitRotation().angleProperty(), 0)),
                    new KeyFrame(Duration.seconds(body.orbitPeriodYear), new KeyValue(body.getOrbitRotation().angleProperty(), 360)));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        });
    }

    public Rotate getOrbitRotation() {
        return orbitRotation;
    }

    public Sphere getShape() {
        return this.shape;
    }

    public MeshView getOrbitRing() {
        return orbitRing;
    }

    private MeshView createRing(float innerRadius, float outerRadius, int sides, int rings) {
        TriangleMesh mesh = new TriangleMesh();

        float tubeRadius = (outerRadius - innerRadius) / 2f;
        float centerRadius = innerRadius + tubeRadius;

        for (int ring = 0; ring <= rings; ring++) {
            double theta = 2.0 * Math.PI * ring / rings;
            float cosTheta = (float) Math.cos(theta);
            float sinTheta = (float) Math.sin(theta);

            for (int side = 0; side <= sides; side++) {
                double phi = 2.0 * Math.PI * side / sides;
                float cosPhi = (float) Math.cos(phi);
                float sinPhi = (float) Math.sin(phi);

                float x = (centerRadius + tubeRadius * cosPhi) * cosTheta;
                float y = (centerRadius + tubeRadius * cosPhi) * sinTheta;
                float z = tubeRadius * sinPhi;

                mesh.getPoints().addAll(x, y, z);

                float u = (float) side / sides;
                float v = (float) ring / rings;
                mesh.getTexCoords().addAll(u, v);
            }
        }

        for (int ring = 0; ring < rings; ring++) {
            for (int side = 0; side < sides; side++) {
                int current = ring * (sides + 1) + side;
                int next = current + sides + 1;

                mesh.getFaces().addAll(
                        current, current, next, next, current + 1, current + 1,
                        next, next, next + 1, next + 1, current + 1, current + 1
                );
            }
        }

        MeshView meshView = new MeshView(mesh);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.rgb(0, 0, 255, 0.33));
        meshView.setMaterial(material);


        return meshView;
    }

}