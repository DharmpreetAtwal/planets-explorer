package org.example.planetsexplorer.celestial;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.example.planetsexplorer.HorizonSystem;
import org.example.planetsexplorer.Main;
import org.example.planetsexplorer.StepSize;
import org.json.JSONObject;

import java.util.ArrayList;

import static org.example.planetsexplorer.Main.pixelKmScale;

public class SecondaryBody extends PrimaryBody {
    private final PrimaryBody primaryBody;

    private float orbitDistance;
    private final float orbitPeriodYear;
    private final float siderealDayHr;
    private final float obliquityToOrbitDeg;

    private ArrayList<JSONObject> ephemData = new ArrayList<>();

    private ChangeListener<Number> orbitRotationAngleListener = null;
    private final Rotate orbitRotation = new Rotate();
    private final Rotate tiltCorrectionZ = new Rotate(0);
    private final Rotate spinRotation = new Rotate();
    private final Rotate tiltRotation = new Rotate(0, Rotate.Y_AXIS);

    private final Group orbitRing = new Group();

    public SecondaryBody(String name, String dbID, float sphereRadius, PrimaryBody primaryBody, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, dbID, sphereRadius);

        ArrayList<JSONObject> ephemMoon = null;
        try {
            int years = (int) orbitPeriodYear;
            float yearFraction = orbitPeriodYear - years;
            int daysFraction = (int) (yearFraction * 365.25);

            int months = daysFraction / 30;
            int days = (daysFraction % 30) + 1;

            int dateStopYear = 2024 + years;
            int dateStopMonth = 1 + months;
            int dateStopDay = 1 + days;

            // No ephemeris for target "Pluto" after A.D. 2199-DEC-29 00:00:00.0000 TDB
            if(dateStopYear >= 2198) {
                dateStopYear = 2199;
            }

            StepSize step;
            if(days <= 1 && months == 0 && years == 0) {
                step = StepSize.MINUTES;
            } else if(days <= 8 && months == 0 && years == 0) {
                step = StepSize.HOURS;
            } else if(months <= 8 && years == 0) {
                step = StepSize.DAYS;
            } else if(years <= 8) {
                step = StepSize.MONTHS;
            } else {
                step = StepSize.YEARS;
            }

            ephemMoon = HorizonSystem.getEphemeris(dbID, primaryBody.getDbID(),
                    String.format("%02d", dateStopYear - years) + "-" +
                            String.format("%02d", dateStopMonth - months) + "-" +
                            String.format("%02d", dateStopDay - days),
                    String.format("%02d", dateStopYear) + "-" +
                            String.format("%02d", dateStopMonth) + "-" +
                            String.format("%02d", dateStopDay),
                    step);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        float orbitDistance = ephemMoon.get(0).getFloat("qr") / pixelKmScale;

        this.setEphemData(ephemMoon);

        this.orbitDistance = orbitDistance;
        this.orbitPeriodYear = orbitPeriodYear;
        this.siderealDayHr = siderealDayHr;
        this.obliquityToOrbitDeg = obliquityToOrbitDeg;
        this.tiltRotation.setAngle(-this.obliquityToOrbitDeg);

        this.primaryBody = primaryBody;

        if(this.primaryBody != null) {
            this.primaryBody.addSecondaryBody(this);

            this.orbitRing.setStyle(
                    "    -fx-stroke-width: 2;\n" +
                    "    -fx-stroke-dash-array: 4 4; /* 4 units filled, 4 units empty */\n" +
                    "    -fx-fill: transparent;");
            this.orbitRing.setMouseTransparent(true);

            this.getShape().setTranslateX(this.primaryBody.getShape().getTranslateX() + this.orbitDistance);
            this.getShape().setTranslateY(this.primaryBody.getShape().getTranslateY());
            this.getShape().setTranslateZ(this.primaryBody.getShape().getTranslateZ());
        }

        this.getShape().setMaterial(new PhongMaterial(Color.ORANGE));
        this.getShape().getTransforms().addAll(this.orbitRotation, this.tiltRotation, this.tiltCorrectionZ, this.spinRotation);

        Point3D shapePoint = this.getShape().localToScene(Point3D.ZERO).add(0, 0, 1);
        this.tiltCorrectionZ.setAxis(this.getShape().sceneToLocal(shapePoint));
        this.tiltCorrectionZ.angleProperty().bind(this.orbitRotation.angleProperty().multiply(-1));
    }

    public void setEphemIndex(int index) {
        if(!this.getEphemData().isEmpty()) {
            JSONObject data = this.getEphemData().get(index);
            this.setEphemerisPosition(data.getFloat("qr") / pixelKmScale,
                    data.getFloat("ma"),
                    data.getFloat("in"));
        }
    }

    private void setEphemerisPosition(float orbitDistance, float meanAnon, float inclin) {
        if(this.primaryBody != null) {
            float x = (float) (orbitDistance * Math.cos(Math.toRadians(inclin)));
            float z  = (float) (orbitDistance * Math.sin(Math.toRadians(inclin)));

            Point3D parentPoint = this.primaryBody.getShape().localToScene(Point3D.ZERO);
            this.getShape().setTranslateX(x + parentPoint.getX());
            this.getShape().setTranslateY(parentPoint.getY());
            this.getShape().setTranslateZ(z  + parentPoint.getZ());

            this.orbitRotation.setPivotX(-x);
            this.orbitRotation.setPivotZ(-z);
            this.orbitRotation.setAngle(meanAnon);
            Point3D newAxis = new Point3D(
                    -Math.sin(Math.toRadians(inclin)),
                    0,
                    Math.cos(Math.toRadians(inclin)));
            this.orbitRotation.setAxis(newAxis);

            this.orbitDistance = orbitDistance;
        }
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

    public Group getOrbitRing() {
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

    public PrimaryBody getPrimaryBody() {
        return primaryBody;
    }

    public float getSiderealDayHr() {
        return siderealDayHr;
    }

    public float getObliquityToOrbitDeg() {
        return obliquityToOrbitDeg;
    }
}
