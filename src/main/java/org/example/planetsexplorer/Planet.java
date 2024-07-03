package org.example.planetsexplorer;

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
import javafx.beans.value.ChangeListener;
import org.json.JSONObject;

public class Planet {
    public static ArrayList<Planet> planetArrayList = new ArrayList<>();

    private final String name;
    private final Sphere shape;
    private final Planet primaryBody;
    private final ArrayList<Planet> secondaryBodies = new ArrayList<>();

    private final float orbitDistance;
    private final float orbitPeriodYear;
    private final float siderealDayHr;
    private final float obliquityToOrbitDeg;


    private ArrayList<JSONObject> ephemData = new ArrayList<>();


    private ChangeListener<Number> orbitRotationAngleListener = null;
    private final Rotate orbitRotation = new Rotate();
    private final Rotate tiltCorrectionZ = new Rotate(0, Rotate.Z_AXIS);
    private final Rotate spinRotation = new Rotate();
    private final Rotate tiltRotation = new Rotate(0, Rotate.Y_AXIS);

    private Point3D rotationAxis;
    private MeshView orbitRing = null;

    public Planet(String name, float shapeRadius, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg, float orbitDistance, Planet primaryBody) {
        this.name = name;
        this.shape = new Sphere(shapeRadius, 2);

        this.orbitDistance = orbitDistance;
        this.orbitPeriodYear = orbitPeriodYear;
        this.siderealDayHr = siderealDayHr;
        this.obliquityToOrbitDeg = obliquityToOrbitDeg;
        this.tiltRotation.setAngle(-obliquityToOrbitDeg);
        this.primaryBody = primaryBody;

        if(this.primaryBody != null) {
            this.primaryBody.addSecondaryBody(this);
            this.orbitRing = createRing(this.orbitDistance, this.orbitDistance + 1, 5, 30);

            this.shape.setTranslateX(this.primaryBody.shape.getTranslateX() + (double)this.orbitDistance);
            this.shape.setTranslateY(this.primaryBody.shape.getTranslateY());
            this.shape.setTranslateZ(this.primaryBody.shape.getTranslateZ());
        }

        this.shape.setMaterial(new PhongMaterial(Color.ORANGE));
        this.shape.getTransforms().addAll(orbitRotation, tiltRotation, tiltCorrectionZ, spinRotation);

        this.initializeMouseEvents();
        this.initializeAnimationProperties();
//        this.initializeSpinAnimation();

        planetArrayList.add(this);
    }

    private void initializeMouseEvents() {
        this.shape.setOnMouseClicked(e-> {
            if(this.orbitRotationAngleListener == null) {
                planetArrayList.forEach(planet -> {
                    if(planet.orbitRotationAngleListener != null) {
                        planet.orbitRotation.angleProperty().removeListener(planet.orbitRotationAngleListener);
                    }
                });

                this.orbitRotationAngleListener = (observable, oldValue, newValue) -> {
                    Point3D newPivotScene = shape.localToScene(Point3D.ZERO);
                    Point3D parentScene = shape.getParent().localToScene(0, 0, 0);
                    Point3D shapeScene = shape.localToScene(0, 0, 0);

                    double parentTranslateX = parentScene.getX();
                    double shapeSceneX = shapeScene.getX();
                    double relX = shapeSceneX - parentTranslateX;
                    Main.camera.getTranslate().setX(relX);
                    Main.camera.getRotateX().setPivotX(newPivotScene.getX());
                    Main.camera.getRotateX().setPivotY(newPivotScene.getY());
                    Main.camera.getRotateX().setPivotZ(newPivotScene.getZ());

                    double parentTranslateY = parentScene.getY();
                    double shapeSceneY = shapeScene.getY();
                    double relY = shapeSceneY - parentTranslateY;
                    Main.camera.getTranslate().setY(relY);
                    Main.camera.getRotateY().setPivotX(newPivotScene.getX());
                    Main.camera.getRotateY().setPivotY(newPivotScene.getY());
                    Main.camera.getRotateY().setPivotZ(newPivotScene.getZ());

                    Main.camera.getRotateZ().setPivotX(newPivotScene.getX());
                    Main.camera.getRotateZ().setPivotY(newPivotScene.getY());
                    Main.camera.getRotateZ().setPivotZ(newPivotScene.getZ());
                };

                this.orbitRotation.angleProperty().addListener(this.orbitRotationAngleListener);
            }
        });
    }

    // IMPORTANT: This method MUST be called after this.shape.setTranslateX/Y have been called
    // IMPORTANT: Bindings must be set before transformations
    private void initializeAnimationProperties() {
        if(this.primaryBody != null) {
            // Set the pivot point of the orbit to be the center of primaryBody
            // Distance from primary body = secondaryBody's X - primaryBody's X
            this.orbitRotation.pivotXProperty().bind(
                    this.primaryBody.shape.translateXProperty().subtract(this.shape.translateXProperty()));
            this.orbitRotation.pivotYProperty().bind(
                    this.primaryBody.shape.translateYProperty().subtract(this.shape.translateYProperty()));
            this.orbitRotation.pivotZProperty().bind(
                    this.primaryBody.shape.translateZProperty().subtract(this.shape.translateZProperty()));
        }

        this.rotationAxis = new Point3D(
                Math.sin(Math.toRadians(this.obliquityToOrbitDeg)),
                0,
                -Math.cos(Math.toRadians(this.obliquityToOrbitDeg)));
        this.spinRotation.setAxis(this.rotationAxis);
        this.tiltCorrectionZ.angleProperty().bind(this.orbitRotation.angleProperty().multiply(-1));
    }

    private void initializeSpinAnimation() {
        // Rotate shape to align with tilted axis
        Timeline spinTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(this.spinRotation.angleProperty(), 0)),
                new KeyFrame(Duration.seconds(Math.abs(this.siderealDayHr)/10), new KeyValue(this.spinRotation.angleProperty(), 360)));

        if(this.siderealDayHr < 0) spinTimeline.setRate(-1);
        spinTimeline.setCycleCount(Timeline.INDEFINITE);
        spinTimeline.play();
    }

    public void animateSecondaryBodies() {
        this.secondaryBodies.forEach(body ->{
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(body.orbitRotation.angleProperty(), 0)),
                    new KeyFrame(Duration.seconds(body.orbitPeriodYear), new KeyValue(body.orbitRotation.angleProperty(), 360)));

            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        });
    }

    private void addSecondaryBody(Planet secondaryBody) {
        this.secondaryBodies.add(secondaryBody);
        secondaryBody.shape.setTranslateX(this.shape.getTranslateX());
        secondaryBody.shape.setTranslateY(this.shape.getTranslateY());
        secondaryBody.shape.setTranslateZ(this.shape.getTranslateZ() + (double)secondaryBody.orbitDistance);
    }

    public ArrayList<JSONObject> getEphemData() {
        return ephemData;
    }

    public void setEphemData(ArrayList<JSONObject> ephemData) {
        this.ephemData = ephemData;
    }

    public void setEphemIndex(int index) {
        JSONObject data = this.ephemData.get(index);
        this.setEphemerisPosition(data.getFloat("qr") / 10000000, data.getFloat("ma"));
    }

    private void setEphemerisPosition(float orbitDistance, float meanAnon) {
        float x = (float) Math.cos(Math.toRadians(meanAnon)) * orbitDistance;
        float y = (float) Math.sin(Math.toRadians(meanAnon)) * orbitDistance;

        this.shape.setTranslateX(x);
        this.shape.setTranslateY(y);
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