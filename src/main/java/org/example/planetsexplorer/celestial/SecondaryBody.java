package org.example.planetsexplorer.celestial;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.example.planetsexplorer.Main;
import org.json.JSONObject;

import java.util.ArrayList;

public class SecondaryBody extends PrimaryBody {
    private final Planet primaryBody;

    private final float orbitDistance;
    private final float orbitPeriodYear;
    private final float siderealDayHr;
    private final float obliquityToOrbitDeg;

    private ArrayList<JSONObject> ephemData = new ArrayList<>();

    private ChangeListener<Number> orbitRotationAngleListener = null;
    private final Rotate orbitRotation = new Rotate();
    private final Rotate tiltCorrectionZ = new Rotate(0);
    private final Rotate spinRotation = new Rotate();
    private final Rotate tiltRotation = new Rotate(0, Rotate.Y_AXIS);

    private MeshView orbitRing = null;

    public SecondaryBody(String name, float sphereRadius, Planet primaryBody, float distance, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, sphereRadius);
        this.orbitDistance = distance;
        this.orbitPeriodYear = orbitPeriodYear;
        this.siderealDayHr = siderealDayHr;
        this.obliquityToOrbitDeg = obliquityToOrbitDeg;
        this.tiltRotation.setAngle(-this.obliquityToOrbitDeg);

        this.primaryBody = primaryBody;

        if(this.primaryBody != null) {
            this.primaryBody.addSecondaryBody(this);
            this.orbitRing = createRing(this.orbitDistance, this.orbitDistance + 1, 5, 30);

            this.getShape().setTranslateX(this.primaryBody.getShape().getTranslateX() + (double)this.orbitDistance);
            this.getShape().setTranslateY(this.primaryBody.getShape().getTranslateY());
            this.getShape().setTranslateZ(this.primaryBody.getShape().getTranslateZ());
        }

        this.getShape().setMaterial(new PhongMaterial(Color.ORANGE));
        this.getShape().getTransforms().addAll(this.orbitRotation, this.tiltRotation, this.tiltCorrectionZ, this.spinRotation);

        this.initializeMouseEvents();
        this.initializeAnimationProperties();
        this.initializeSpinAnimation();
    }

    private void initializeMouseEvents() {
        this.getShape().setOnMouseClicked(e-> {
            if(this.orbitRotationAngleListener == null) {
                Planet.planetArrayList.forEach(planet -> {
                    if(planet.getOrbitRotationAngleListener() != null) {
                        planet.getOrbitRotation().angleProperty().removeListener(planet.getOrbitRotationAngleListener());
                    }
                });

                this.orbitRotationAngleListener = (observable, oldValue, newValue) -> {
                    Point3D newPivotScene = this.getShape().localToScene(Point3D.ZERO);
                    Point3D parentScene = this.getShape().getParent().localToScene(0, 0, 0);
                    Point3D shapeScene = this.getShape().localToScene(0, 0, 0);

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
                    this.primaryBody.getShape().translateXProperty().subtract(this.getShape().translateXProperty()));
            this.orbitRotation.pivotYProperty().bind(
                    this.primaryBody.getShape().translateYProperty().subtract(this.getShape().translateYProperty()));
            this.orbitRotation.pivotZProperty().bind(
                    this.primaryBody.getShape().translateZProperty().subtract(this.getShape().translateZProperty()));
        }

        Point3D shapePoint = this.getShape().localToScene(Point3D.ZERO).add(0, 0, 1);
        this.tiltCorrectionZ.setAxis(this.getShape().sceneToLocal(shapePoint));
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

    public float getOrbitDistance() {
        return orbitDistance;
    }

    public float getOrbitPeriodYear() {
        return orbitPeriodYear;
    }

    public ArrayList<JSONObject> getEphemData() {
        return ephemData;
    }

    public void setEphemData(ArrayList<JSONObject> ephemData) {
        this.ephemData = ephemData;
    }

    public ChangeListener<Number> getOrbitRotationAngleListener() {
        return orbitRotationAngleListener;
    }

    public Rotate getOrbitRotation() {
        return orbitRotation;
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
