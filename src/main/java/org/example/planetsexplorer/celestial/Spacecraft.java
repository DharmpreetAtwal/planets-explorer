package org.example.planetsexplorer.celestial;

import org.example.planetsexplorer.HorizonSystem;
import org.example.planetsexplorer.Main;
import org.example.planetsexplorer.StepSize;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * A {@code Spacecraft} is any {@link Celestial} that is launched from earth to
 * orbit another {@link PrimaryBody} as a {@link SecondaryBody}. The default
 * {@code PrimaryBody} is the {@link Sun}, but this class as the unique ability
 * to change {@code PrimaryBody}.
 *
 * <p> If a given {@code Spacecraft} never leaves the gravitational field of
 * the Earth, it's recommended that the {@code PrimaryBody} be changed to the
 * Earth. The default {@code PrimaryBody} as the {@code Sun} should be kept
 * if the {@code Spacecraft} travels to another {@link Planet}. This ensures
 * that the orbit path of the {@code Spacecraft} is drawn correctly relative
 * to the correct {@code PrimaryBody}.
 */
public class Spacecraft extends SecondaryBody {
    /**
     * Constructs a {@code Spacecraft} given its name and database ID.
     * @param name The name of the {@code Spacecraft}
     * @param dbID The database ID of the {@code Spacecraft}
     */
    private Spacecraft(String name, String dbID) {
        super(name, dbID, 1, Main.sun, 1, 1, 0);
        String startEphem = HorizonSystem.getSpacecraftStartTimestamp(dbID);
        String endEphem = HorizonSystem.getSpacecraftStopTimestamp(dbID, startEphem);

        String startMonth = startEphem.substring(5, 8);
        String endMonth = endEphem.substring(5, 8);

        LocalDateTime dateStart = LocalDateTime.of(
                Integer.parseInt(startEphem.substring(0, 4)),
                convertMonthAbbreviationToNumber(startMonth),
                Integer.parseInt(startEphem.substring(9, 11)),
                Integer.parseInt(startEphem.substring(12, 14)),
                Integer.parseInt(startEphem.substring(15, 17)));
        LocalDateTime dateStop = LocalDateTime.of(
                Integer.parseInt(endEphem.substring(0, 4)),
                convertMonthAbbreviationToNumber(endMonth),
                Integer.parseInt(endEphem.substring(9, 11)),
                Integer.parseInt(endEphem.substring(12, 14)),
                Integer.parseInt(endEphem.substring(15, 17))
        );
        this.initializeStepSize(dateStart, dateStop);

        // Copy the ephemeris date range of this Spacecraft onto it's primaryBody
        if(this.getPrimaryBody() instanceof SecondaryBody secBody)
            secBody.setEphemeris(dateStart, dateStop, this.getEphemerisStepSize());

        // Copy the ephemeris date range of this Spacecraft onto it's sibling secondaryBodies
        for(SecondaryBody secondaryBody: this.getPrimaryBody().getSecondaryBodies()) {
            secondaryBody.setEphemeris(dateStart, dateStop, this.getEphemerisStepSize());
        }
    }

    /**
     * Acts as a lookup tables that converts a 3-letter month abbreviation into numeric
     * month-of-the-year. This is needed because the {@code HorizonSystem} returns the
     * start/stop month of a {@code Spacecraft} in this 3-letter format.
     * @param month The 3-letter abbreviation of a month (case-insensitive)
     * @return The numeric month-of-the-year
     */
    private int convertMonthAbbreviationToNumber(String month) {
        if (month == null || month.length() != 3)
            throw new IllegalArgumentException("Month must be a three letter string");

        return switch (month.toUpperCase()) {
            case "JAN" -> 1;
            case "FEB" -> 2;
            case "MAR" -> 3;
            case "APR" -> 4;
            case "MAY" -> 5;
            case "JUN" -> 6;
            case "JUL" -> 7;
            case "AUG" -> 8;
            case "SEP" -> 9;
            case "OCT" -> 10;
            case "NOV" -> 11;
            case "DEC" -> 12;
            default -> 0;
        };
    }

    /**
     * Creates a {@code Spacecraft} without leaving an unused variable.
     * @param name The name of the {@code Spacecraft}
     * @param dbID The database ID of the {@code Spacecraft}
     */
    public static void createSpacecraft(String name, String dbID) {
        Spacecraft spacecraft = new Spacecraft(name, dbID);
        SecondaryBody.addToStage(spacecraft);
    }

    /**
     * Deletes a {@code Spacecraft}.
     * @param dbID The database ID of the {@code Spacecraft}
     */
    public static void deleteSpacecraft(String dbID) {
        Spacecraft foundSpacecraft = null;
        for(Celestial celestial: Celestial.celestialArrayList)
            if(celestial.getDbID().equals(dbID) && celestial instanceof Spacecraft spacecraft)
                foundSpacecraft = spacecraft;

        if(foundSpacecraft != null) {
            SecondaryBody.removeFromStage(foundSpacecraft);
        } else {
            System.err.println("No moon found: " + dbID);
        }
    }

    /**
     * Changes the {@code PrimaryBody} of a {@code Spacecraft}
     * @param primaryBody The new {@code PrimaryBody} of this {@code Spacecraft}.
     */
    public void changePrimaryBody(PrimaryBody primaryBody) {
        this.getPrimaryBody().removeSecondaryBody(this);
        this.getPrimaryBody().addSecondaryBody(this);
        this.setPrimaryBody(primaryBody);
    }
}
