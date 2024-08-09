package org.example.planetsexplorer.celestial;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import org.example.planetsexplorer.*;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.example.planetsexplorer.HorizonSystem.pixelKmScale;

public class SecondaryBody extends PrimaryBody {
    private final PrimaryBody primaryBody;

    private float orbitDistance;
    private final float orbitPeriodYear;
    private final float siderealDayHr;
    private final float obliquityToOrbitDeg;

    private ArrayList<JSONObject> ephemData = new ArrayList<>();
    private boolean ephemFrozen;
    private int ephemIndex = 0;

    private int ephemStartYear;
    private int ephemStartMonth;
    private int ephemStartDay;
    private int ephemStartHour;
    private int ephemStartMinute;

    private int ephemStopYear;
    private int ephemStopMonth;
    private int ephemStopDay;
    private int ephemStopHour;
    private int ephemStopMinute;
    private StepSize ephemStepSize;

    private final Rotate tiltRotation = new Rotate(0, Rotate.Y_AXIS);
    private final Group orbitRing = new Group();
    private final Cylinder primaryConnection = new Cylinder();
    private final Cylinder velocityVector = new Cylinder();

    public SecondaryBody(String name, String dbID, float sphereRadius, PrimaryBody primaryBody, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, dbID, sphereRadius);
        this.primaryBody = primaryBody;
        this.primaryBody.addSecondaryBody(this);

        this.ephemFrozen = false;
        this.orbitPeriodYear = orbitPeriodYear;
        this.siderealDayHr = siderealDayHr;
        this.obliquityToOrbitDeg = obliquityToOrbitDeg;
        this.tiltRotation.setAngle(-this.obliquityToOrbitDeg);

        this.orbitRing.setStyle(
                """
                    -fx-stroke-width: 2;
                    -fx-stroke-dash-array: 4 4;
                    -fx-fill: transparent;\
                """);
        this.orbitRing.setMouseTransparent(true);

        this.getShape().setTranslateX(this.primaryBody.getShape().getTranslateX() + this.orbitDistance);
        this.getShape().setTranslateY(this.primaryBody.getShape().getTranslateY());
        this.getShape().setTranslateZ(this.primaryBody.getShape().getTranslateZ());
        this.getShape().setMaterial(new PhongMaterial(Color.ORANGE));
        this.getShape().getTransforms().addAll(this.tiltRotation);

        this.primaryConnection.setRadius(sphereRadius / 3);
        this.primaryConnection.setMaterial(new PhongMaterial(Color.BLUE));

        this.velocityVector.setRadius(sphereRadius / 4);
        this.velocityVector.setMaterial(new PhongMaterial(Color.PURPLE));
    }

    public void initializeStartStop(float orbitYear) {
        int years = (int) orbitYear;
        double fracYear = orbitYear - years;

        int fracDays = (int) (fracYear * 365);
        int months = fracDays / 31;
        int days = fracDays % 30;

        double daysLeft = fracYear * 365 - fracDays;
        int hours = (int) (daysLeft * 24);

        double hoursLeft = (daysLeft * 24) - hours;
        int minutes = (int) (hoursLeft * 60);

        this.setEphemStartYear(2024);
        this.setEphemStartMonth(1);
        this.setEphemStartDay(1);
        this.setEphemStartHour(0);
        this.setEphemStartMinute(0);

        this.setEphemStopYear(this.getEphemStartYear() + years);
        this.setEphemStopMonth(this.getEphemStartMonth() + months);
        this.setEphemStopDay(this.getEphemStartDay() + days);
        this.setEphemStopHour(this.getEphemStartHour() + hours);
        this.setEphemStopMinute(this.getEphemStartMinute() + minutes);

        // No ephemeris for target "Pluto" after A.D. 2199-DEC-29 00:00:00.0000 TDB
        if(this.getEphemStopYear() >= 2198) {
            this.setEphemStopYear(2199);
        }

        if(days == 0 && months == 0 && years == 0 && hours <= 3) {
            this.setEphemStepSize(StepSize.MINUTES);
        } else if(days <= 8 && months == 0 && years == 0) {
            this.setEphemStepSize(StepSize.HOURS);
        } else if(months <= 8 && years == 0) {
            this.setEphemStepSize(StepSize.DAYS);
        } else if(years <= 8) {
            this.setEphemStepSize(StepSize.MONTHS);
        } else {
            this.setEphemStepSize(StepSize.YEARS);
        }

        this.initializeEphemeris();
    }

    public void initializeEphemeris() {
        LocalDateTime dateStart = LocalDateTime.of(
                this.ephemStartYear,
                this.ephemStartMonth,
                this.ephemStartDay,
                this.ephemStartHour,
                this.ephemStartMinute);
        LocalDateTime dateStop = LocalDateTime.of(
                this.ephemStopYear,
                this.ephemStopMonth,
                this.ephemStopDay,
                this.ephemStopHour,
                this.ephemStopMinute);
        this.setEphemeris(dateStart,
                dateStop, this.getEphemStepSize());
    }

    public static void addToStage(SecondaryBody secondaryBody) {
        if(!Main.rootScene3D.getChildren().contains(secondaryBody.getShape())) {
            Main.rootScene3D.getChildren().add(secondaryBody.getShape());
            Main.rootScene3D.getChildren().add(secondaryBody.getPrimaryConnection());
            Main.rootScene3D.getChildren().add(secondaryBody.getVelocityVector());
        }

        if(!Main.sceneRoot.getChildren().contains(secondaryBody.getOrbitRing())) {
            Main.sceneRoot.getChildren().add(secondaryBody.getOrbitRing());
            Main.sceneRoot.getChildren().add(secondaryBody.getGroupUI());
            secondaryBody.getGroupUI().toFront();
        }
        PlanetsCamera.updateCameraUI();
    }

    public static void removeFromStage(SecondaryBody secondaryBody) {
        Main.rootScene3D.getChildren().remove(secondaryBody.getShape());
        Main.rootScene3D.getChildren().remove(secondaryBody.getPrimaryConnection());
        Main.rootScene3D.getChildren().remove(secondaryBody.getVelocityVector());

        Main.sceneRoot.getChildren().remove(secondaryBody.getOrbitRing());
        Main.sceneRoot.getChildren().remove(secondaryBody.getGroupUI());
        PlanetsCamera.updateCameraUI();
    }

    public void setEphemeris(LocalDateTime dateStart, LocalDateTime dateStop, StepSize ephemStepSize) {
        ArrayList<JSONObject> ephem;

        this.ephemStartYear = dateStart.getYear();
        this.ephemStartMonth = dateStart.getMonthValue();
        this.ephemStartDay = dateStart.getDayOfMonth();
        this.ephemStartHour = dateStart.getHour();
        this.ephemStartMinute = dateStart.getMinute();

        this.ephemStopYear = dateStop.getYear();
        this.ephemStopMonth = dateStop.getMonthValue();
        this.ephemStopDay = dateStop.getDayOfMonth();
        this.ephemStopHour = dateStop.getHour();
        this.ephemStopMinute = dateStop.getMinute();
        this.ephemStepSize = ephemStepSize;

        try {
            ephem = HorizonSystem.getEphemeris(this.getDbID(), primaryBody.getDbID(),
                    this.getEphemStartDateTimeStamp(),
                    this.getEphemStopDateTimeStamp(),
                    ephemStepSize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.setEphemData(ephem);
        this.updateEphemPosition(true);
    }

    public String getEphemStartDateTimeStamp() {
        return String.format("%02d", this.ephemStartYear) + "-" +
                String.format("%02d", this.ephemStartMonth) + "-" +
                String.format("%02d", this.ephemStartDay) + " " +
                String.format("%02d", this.ephemStartHour) + ":" +
                String.format("%02d", this.ephemStartMinute);
    }

    public String getEphemStopDateTimeStamp() {
        return String.format("%02d", this.ephemStopYear) + "-" +
                String.format("%02d", this.ephemStopMonth) + "-" +
                String.format("%02d", this.ephemStopDay) + " " +
                String.format("%02d", this.ephemStopHour) + ":" +
                String.format("%02d", this.ephemStopMinute);
    }

    public void updateEphemPosition(boolean updateConnectionLine) {
        if(!this.getEphemData().isEmpty()) {
            int newIndex = this.ephemIndex % this.getEphemData().size();
            JSONObject data = this.getEphemData().get(newIndex);

            if(this.primaryBody != null) {
                float x = data.getFloat("x") / pixelKmScale;
                float y = data.getFloat("y") / pixelKmScale;
                float z = data.getFloat("z") / pixelKmScale;

                Point3D primaryPoint = this.getPrimaryBody().getShape().localToScene(Point3D.ZERO);
                this.getShape().setTranslateX(x + primaryPoint.getX());
                this.getShape().setTranslateY(y + primaryPoint.getY());
                this.getShape().setTranslateZ(z + primaryPoint.getZ());
                this.orbitDistance = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

                float vx = (float) (data.getFloat("vx") * this.getShape().getRadius() * 2);
                float vy = (float) (data.getFloat("vy") * this.getShape().getRadius() * 2);
                float vz = (float) (data.getFloat("vz") * this.getShape().getRadius() * 2);

                Point3D startPos = this.getShape().localToScene(Point3D.ZERO);
                Point3D primaryPos = this.primaryBody.getShape().localToScene(Point3D.ZERO);
                
                if(updateConnectionLine) {
                    this.updateConnectionLine(this.primaryConnection,
                            startPos.getX(), startPos.getY(),startPos.getZ(),
                            primaryPos.getX(), primaryPos.getY(), primaryPos.getZ());
                    this.updateConnectionLine(this.velocityVector,
                            startPos.getX(), startPos.getY(), startPos.getZ(),
                            startPos.getX() + vx, startPos.getY() + vy, startPos.getZ() + vz);
                }
            }
        }
    }

    private void updateConnectionLine(Cylinder line, double startX, double startY, double startZ, double endX, double endY, double endZ) {
        double distance = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2) + Math.pow(endZ - startZ, 2));
        line.setHeight(distance);

        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;
        double midZ = (startZ + endZ) / 2;

        line.setTranslateX(midX);
        line.setTranslateY(midY);
        line.setTranslateZ(midZ);

        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double theta = Math.atan(dy / dx);
        double phi = Math.acos(dz / distance);

        double phiDeg = Math.toDegrees(phi) - 90;
        if(dx < 0) phiDeg = phiDeg * -1;

        line.getTransforms().clear();
        line.getTransforms().addAll(
                new Rotate(Math.toDegrees(theta) + 90, Rotate.Z_AXIS),
                new Rotate(phiDeg, Rotate.X_AXIS)
        );
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

    public int getEphemStartYear() {
        return ephemStartYear;
    }

    public int getEphemStartMonth() {
        return ephemStartMonth;
    }

    public int getEphemStartDay() {
        return ephemStartDay;
    }

    public int getEphemStartHour() {
        return ephemStartHour;
    }

    public int getEphemStartMinute() {
        return ephemStartMinute;
    }

    public int getEphemStopYear() {
        return ephemStopYear;
    }

    public int getEphemStopMonth() {
        return ephemStopMonth;
    }

    public int getEphemStopDay() {
        return ephemStopDay;
    }

    public int getEphemStopHour() {
        return ephemStopHour;
    }

    public int getEphemStopMinute() {
        return ephemStopMinute;
    }

    public StepSize getEphemStepSize() {
        return ephemStepSize;
    }

    public boolean isEphemFrozen() {
        return ephemFrozen;
    }

    public void setEphemFrozen(boolean ephemFrozen) {
        this.ephemFrozen = ephemFrozen;
    }

    public int getEphemIndex() {
        return ephemIndex;
    }

    public Cylinder getPrimaryConnection() {
        return primaryConnection;
    }

    public void setEphemIndex(int ephemIndex) {
        this.ephemIndex = ephemIndex;
    }

    public Cylinder getVelocityVector() {
        return velocityVector;
    }

    public void setEphemStartYear(int ephemStartYear) {
        this.ephemStartYear = ephemStartYear;
    }

    public void setEphemStartMonth(int ephemStartMonth) {
        this.ephemStartMonth = ephemStartMonth;
    }

    public void setEphemStartDay(int ephemStartDay) {
        this.ephemStartDay = ephemStartDay;
    }

    public void setEphemStartHour(int ephemStartHour) {
        this.ephemStartHour = ephemStartHour;
    }

    public void setEphemStartMinute(int ephemStartMinute) {
        this.ephemStartMinute = ephemStartMinute;
    }

    public void setEphemStopYear(int ephemStopYear) {
        this.ephemStopYear = ephemStopYear;
    }

    public void setEphemStopMonth(int ephemStopMonth) {
        this.ephemStopMonth = ephemStopMonth;
    }

    public void setEphemStopDay(int ephemStopDay) {
        this.ephemStopDay = ephemStopDay;
    }

    public void setEphemStopHour(int ephemStopHour) {
        this.ephemStopHour = ephemStopHour;
    }

    public void setEphemStopMinute(int ephemStopMinute) {
        this.ephemStopMinute = ephemStopMinute;
    }

    public void setEphemStepSize(StepSize ephemStepSize) {
        this.ephemStepSize = ephemStepSize;
    }
}
