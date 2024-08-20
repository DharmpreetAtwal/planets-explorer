package org.example.planetsexplorer.celestial;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import org.example.planetsexplorer.*;
import static org.example.planetsexplorer.HorizonSystem.pixelKmScale;

import org.json.JSONObject;

import java.time.LocalDateTime;

import java.util.ArrayList;


/**
 * A {@code SecondaryBody} is a {@link Celestial} that orbits a {@link PrimaryBody}.
 * A {@code SecondaryBody} itself may be a {@code PrimaryBody}, that has another
 * {@code SecondaryBody} orbiting around it, like a {@link Moon} orbiting around
 * {@link Planet}.
 *
 * @author Dharmpreet Atwal
 * @see Celestial
 * @see PrimaryBody
 * @see Planet
 * @see Moon
 */
public class SecondaryBody extends PrimaryBody {
    /**
     * The {@code PrimaryBody} this {@code Celestial} orbits around
     */
    private PrimaryBody primaryBody;

    /**
     * The distance between this body and it's {@code PrimaryBody}
     * @see SecondaryBody#primaryBody
     */
    private float orbitDistance;

    /**
     * The time in earth-years it takes for this body to complete a full orbit around its {@code PrimaryBody}.
     *
     * <p> For the {@code Planet} Earth orbiting around the {@code Sun}, this value would be {@code 1.0000174}
     * years.
     * @see SecondaryBody#primaryBody
     */
    private final float orbitPeriodYear;

    /**
     * The time in hours it takes for this body to complete a 360° spin about its own central axis.
     *
     * <p> For the {@code Planet} Earth, this value would be {@code 23.9344695944} hours.
     */
    private final float siderealDayHr;

    /**
     * The rotational tilt of this body relative to the heliocentric elliptical plane.
     * @see SecondaryBody#tiltRotation
     */
    private final float obliquityToOrbitDeg;

    /**
     * A list of the ephemeris data of this body. Each {@code JSONObject} represents a single point
     * in space. A JSONObject contains the displacement components of this body relative to its
     * {@code PrimaryBody} {@code (x, y, z)}, and its instantaneous velocity components
     * {@code (vx, vy, vz)}
     */
    private ArrayList<JSONObject> ephemerisData = new ArrayList<>();

    /**
     * A value to check if updates to this body's displayed ephemeris are disabled or not.
     */
    private boolean ephemerisFrozen;

    /**
     * A local counter independent of the global counter in {@code HorizonSystem}.
     * @see HorizonSystem#ephemerisIndex
     */
    private int ephemerisIndex = 0;

    /**
     * The start of the body's ephemeris date-time range. The point corresponding to
     * this date-time would be {@code ephemerisData.get(0)}
     */
    private LocalDateTime dateStart;

    /**
     * The end of the body's ephemeris date-time range.The point corresponding to
     * this date-time would be {@code ephemerisData.get(ephemerisData.size() - 1)}
     */
    private LocalDateTime dateStop;

    /**
     * The time difference between two sequential ephemeris data points.
     */
    private StepSize ephemerisStepSize;

    /**
     * A transformation that applies the obliquityToOrbitDeg of this body.
     * @see SecondaryBody#obliquityToOrbitDeg
     */
    private final Rotate tiltRotation = new Rotate(0, Rotate.Y_AXIS);

    /**
     * A Node that contains the 2D overlay that represents the 3D orbit path of this body.
     */
    private final Group orbitRing = new Group();

    /**
     * A 3D Node that helps visualize this body's displacement vector from its {@code PrimaryBody}.
     * The cylinder is positioned and rotated so that this body and its {@code PrimaryBody}
     * are situated on opposite ends.
     * @see SecondaryBody#primaryBody
     * @see SecondaryBody#ephemerisData
     */
    private final Cylinder primaryConnection = new Cylinder();

    /**
     * A 3D Node that helps visualize this body's instantaneous velocity vector. One end of the
     * cylinder is played at the center of this body, and the other points out in the direction
     * of the velocity. This cylinder length is not to scale. The length is scaled relative to the
     * radius of the body to ensure the cylinder sticks out of the body's shape.
     * @see SecondaryBody#ephemerisData
     */
    private final Cylinder velocityVector = new Cylinder();

    /**
     * Constructs a {@code SecondaryBody} given all the required fields. This constructor
     * initializes the physical parameters, the Rotation and Translation transformations,
     * the orbit ring's CSS styling, and the material type and colour of the 3D nodes.
     * @param name The unique title.
     * @param dbID The unique database id.
     * @param shapeRadius The radius of the body's shape.
     * @param primaryBody The {@code PrimaryBody} this body will rotate around.
     * @param orbitPeriodYear The time in years it takes to complete one whole orbit.
     * @param siderealDayHr The time in years it takes to spin 360° around the central axis
     * @param obliquityToOrbitDeg The rotational tilt of the body.
     */
    public SecondaryBody(String name, String dbID, float shapeRadius, PrimaryBody primaryBody, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, dbID, shapeRadius);
        this.primaryBody = primaryBody;
        this.primaryBody.addSecondaryBody(this);

        this.ephemerisFrozen = false;
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

        this.primaryConnection.setRadius(shapeRadius / 3);
        this.primaryConnection.setMaterial(new PhongMaterial(Color.BLUE));

        this.velocityVector.setRadius(shapeRadius / 4);
        this.velocityVector.setMaterial(new PhongMaterial(Color.PURPLE));
    }

    /**
     * The default initializer for the {@code dateStart}, {@code dateStop}, and
     * {@code ephemerisStepSize}. The default dateStart is the current system
     * data and time. The dateStop is calculated using the body's
     * {@code orbitPeriodYear}.
     *
     * <p> This initializer also ensures that the year limit of
     * on the database's records is not exceeded.
     *
     * <p> The default {@code ephemerisStepSize} is set so that the resulting
     * ephemeris data has the minimal number of points in it.
     *
     * <p> If delta start/stop is {@code > 8 years}, {@code ephemerisStepSize = YEARS} <br>
     * If delta start/stop is {@code 1 - 8 years}, {@code ephemerisStepSize = MONTHS} <br>
     * If delta start/stop is {@code 1 - 8 month}, {@code ephemerisStepSize = DAYS} <br>
     * If delta start/stop is {@code 1 - 8 days}, {@code ephemerisStepSize = HOURS} <br>
     * If delta start/stop is {@code <= 3 hours}, {@code ephemerisStepSize = MINUTES} <br>
     */
    public void initializeStartStop() {
        int years = (int) this.orbitPeriodYear;
        double fracYear = this.orbitPeriodYear - years;

        int fracDays = (int) (fracYear * 365);
        int months = fracDays / 31;
        int days = fracDays % 30;

        double daysLeft = fracYear * 365 - fracDays;
        int hours = (int) (daysLeft * 24);

        double hoursLeft = (daysLeft * 24) - hours;
        int minutes = (int) (hoursLeft * 60);

        this.dateStart = LocalDateTime.now();
        this.dateStop = this.dateStart.plusYears(
                years).plusMonths(months).plusDays(days).plusHours(hours).plusMinutes(minutes);

        // No ephemeris for target "Pluto" after A.D. 2199-DEC-29 00:00:00.0000 TDB
        if(dateStart.getYear() >= 2198) dateStart = dateStart.withYear(2199);

        if(days == 0 && months == 0 && years == 0 && hours <= 3) {
            this.setEphemerisStepSize(StepSize.MINUTES);
        } else if(days <= 8 && months == 0 && years == 0) {
            this.setEphemerisStepSize(StepSize.HOURS);
        } else if(months <= 8 && years == 0) {
            this.setEphemerisStepSize(StepSize.DAYS);
        } else if(years <= 8) {
            this.setEphemerisStepSize(StepSize.MONTHS);
        } else {
            this.setEphemerisStepSize(StepSize.YEARS);
        }
        this.initializeEphemeris();
    }

    /**
     * Sets the ephemeris of this body using its ephemeris fields
     */
    public void initializeEphemeris() {
        this.setEphemeris(this.dateStart, this.dateStop, this.getEphemerisStepSize());
    }

    /**
     * Adds a {@code SecondaryBody} and all its associated 3D and 2D UI nodes to the
     * scene of {@link Main}, then updates the camera UI.
     * @param secondaryBody The body to add to the scene
     * @see Main
     * @see PlanetsCamera
     */
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

    /**
     * Removes a {@code SecondaryBody} and all its associated 3D and 2D UI nodes from the
     * scene of {@link Main}, then updates the camera UI.
     * @param secondaryBody The body to add to the scene
     * @see Main
     * @see PlanetsCamera
     */
    public static void removeFromStage(SecondaryBody secondaryBody) {
        Main.rootScene3D.getChildren().remove(secondaryBody.getShape());
        Main.rootScene3D.getChildren().remove(secondaryBody.getPrimaryConnection());
        Main.rootScene3D.getChildren().remove(secondaryBody.getVelocityVector());

        Main.sceneRoot.getChildren().remove(secondaryBody.getOrbitRing());
        Main.sceneRoot.getChildren().remove(secondaryBody.getGroupUI());
        PlanetsCamera.updateCameraUI();
    }

    /**
     * Overwrites the current dateStart/Stop and ephemerisStepSize, executes the HTTP request
     * to the database, and stores the returned ephemeris data.
     * @param dateStart The start of the ephemeris date-time range
     * @param dateStop The end of the ephemeris date-time range
     * @param ephemerisStepSize The time-based increment between two sequential ephemeris points
     */
    public void setEphemeris(LocalDateTime dateStart, LocalDateTime dateStop, StepSize ephemerisStepSize) {
        ArrayList<JSONObject> ephemeris;

        this.dateStart = dateStart;
        this.dateStop = dateStop;
        this.ephemerisStepSize = ephemerisStepSize;

        try {
            ephemeris = HorizonSystem.getEphemeris(this.getDbID(),
                    primaryBody.getDbID(),
                    this.getEphemerisStartDateTimeStamp(),
                    this.getEphemerisStopDateTimeStamp(),
                    ephemerisStepSize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.setEphemerisData(ephemeris);
        this.updateEphemerisPosition(true);
    }

    /**
     * Converts {@code dataStart} into a string that can be used in database ephemeris queries.
     * @return {@code dataStart} as a String in format "YYYY-MM-DD HH:MM"
     */
    private String getEphemerisStartDateTimeStamp() {
        return String.format("%02d", this.dateStart.getYear()) + "-" +
                String.format("%02d", this.dateStart.getMonthValue()) + "-" +
                String.format("%02d", this.dateStart.getDayOfMonth()) + " " +
                String.format("%02d", this.dateStart.getHour()) + ":" +
                String.format("%02d", this.dateStart.getMinute());
    }

    /**
     * Converts {@code dateStop} into a string that can be used in database ephemeris queries.
     * @return {@code dateStop} as a String in format "YYYY-MM-DD HH:MM"
     */
    private String getEphemerisStopDateTimeStamp() {
        return String.format("%02d", this.dateStop.getYear()) + "-" +
                String.format("%02d", this.dateStop.getMonthValue()) + "-" +
                String.format("%02d", this.dateStop.getDayOfMonth()) + " " +
                String.format("%02d", this.dateStop.getHour()) + ":" +
                String.format("%02d", this.dateStop.getMinute());
    }

    /**
     * Updates this body's translation and {@code orbitDistance} by using its {@code ephemerisIndex}
     * to determine which ephemeris point to display.
     *
     * <p> Includes an {@code updateConnectionLine} parameter to optionally update the displacement
     * and velocity Cylinder vectors of this body. Should only be false when ephemeris is changed to
     * calculate the 2D projection of the shape onto the screen. Helps reduce unneeded position updates
     * operations that aren't visible to the user.
     * @param updateConnectionLine true when the Cylinder vectors need to be updated, else false
     * @see SecondaryBody#primaryConnection
     * @see SecondaryBody#velocityVector
     */
    public void updateEphemerisPosition(boolean updateConnectionLine) {
        if(!this.getEphemerisData().isEmpty()) {
            int newIndex = this.ephemerisIndex % this.getEphemerisData().size();
            JSONObject data = this.getEphemerisData().get(newIndex);

            if(this.primaryBody != null) {
                float x = data.getFloat("x") / pixelKmScale;
                float y = data.getFloat("y") / pixelKmScale;
                float z = data.getFloat("z") / pixelKmScale;

                Point3D primaryPoint = this.getPrimaryBody().getSceneCoordinates();
                this.getShape().setTranslateX(x + primaryPoint.getX());
                this.getShape().setTranslateY(y + primaryPoint.getY());
                this.getShape().setTranslateZ(z + primaryPoint.getZ());
                this.orbitDistance = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

                if(updateConnectionLine) {
                    float vx = (float) (data.getFloat("vx") * this.getShape().getRadius() * 2);
                    float vy = (float) (data.getFloat("vy") * this.getShape().getRadius() * 2);
                    float vz = (float) (data.getFloat("vz") * this.getShape().getRadius() * 2);

                    Point3D startPos = this.getShape().localToScene(Point3D.ZERO);
                    Point3D primaryPos = this.primaryBody.getShape().localToScene(Point3D.ZERO);
                    Point3D velocityEnd = startPos.add(new Point3D(vx, vy, vz));

                    this.updateConnectionLine(this.primaryConnection, startPos, primaryPos);
                    this.updateConnectionLine(this.velocityVector, startPos, velocityEnd);
                }
            }
        }
    }

    /**
     * Helper method that calculates the length, translation, and rotation of a Cylinder so that its
     * two ends are positioned at a given startPoint and endPoint.
     * @param line The cylinder to be transformed
     * @param startPoint The starting point of the cylinder
     * @param endPoint The ending point of the cylinder
     */
    private void updateConnectionLine(Cylinder line, Point3D startPoint, Point3D endPoint) {
        double distance = Math.sqrt(
                Math.pow(endPoint.getX() - startPoint.getX(), 2) +
                        Math.pow(endPoint.getY() - startPoint.getY(), 2) +
                        Math.pow(endPoint.getZ() - startPoint.getZ(), 2));
        line.setHeight(distance);

        double midX = (startPoint.getX() + endPoint.getX()) / 2;
        double midY = (startPoint.getY() + endPoint.getY()) / 2;
        double midZ = (startPoint.getZ() + endPoint.getZ()) / 2;

        line.setTranslateX(midX);
        line.setTranslateY(midY);
        line.setTranslateZ(midZ);

        double dx = endPoint.getX() - startPoint.getX();
        double dy = endPoint.getY() - startPoint.getY();
        double dz = endPoint.getZ() - startPoint.getZ();
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

    /**
     * Copies another {@code SecondaryBody} ephemeris date range and step size onto this body's.
     * @param secondaryBody The {@code SecondaryBody} whose ephemeris fields are to be copied
     * onto this body's
     */
    public void copyEphemerisDateRange(SecondaryBody secondaryBody) {
        this.setEphemeris(secondaryBody.dateStart, secondaryBody.dateStop, secondaryBody.ephemerisStepSize);
    }

    public float getOrbitDistance() {
        return orbitDistance;
    }

    public float getOrbitPeriodYear() {
        return orbitPeriodYear;
    }

    public ArrayList<JSONObject> getEphemerisData() {
        return ephemerisData;
    }

    public void setEphemerisData(ArrayList<JSONObject> ephemerisData) {
        this.ephemerisData = ephemerisData;
    }

    public Group getOrbitRing() {
        return orbitRing;
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

    public StepSize getEphemerisStepSize() {
        return ephemerisStepSize;
    }

    public boolean isEphemerisFrozen() {
        return ephemerisFrozen;
    }

    public void setEphemerisFrozen(boolean ephemerisFrozen) {
        this.ephemerisFrozen = ephemerisFrozen;
    }

    public int getEphemerisIndex() {
        return ephemerisIndex;
    }

    public Cylinder getPrimaryConnection() {
        return primaryConnection;
    }

    public void setEphemerisIndex(int ephemerisIndex) {
        this.ephemerisIndex = ephemerisIndex;
    }

    public Cylinder getVelocityVector() {
        return velocityVector;
    }

    public void setEphemerisStepSize(StepSize ephemerisStepSize) {
        this.ephemerisStepSize = ephemerisStepSize;
    }

    public void setPrimaryBody(PrimaryBody primaryBody) {
        this.primaryBody = primaryBody;
    }

    public LocalDateTime getDateStart() {
        return dateStart;
    }

    public LocalDateTime getDateStop() {
        return dateStop;
    }
}
