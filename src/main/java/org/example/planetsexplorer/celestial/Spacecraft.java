package org.example.planetsexplorer.celestial;

import org.example.planetsexplorer.HorizonSystem;
import org.example.planetsexplorer.Main;
import org.example.planetsexplorer.StepSize;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Spacecraft extends SecondaryBody {
    public Spacecraft(String name, String dbID) {
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
        this.setEphemerisStepSize(StepSize.YEARS);

        if(dateStart.until(dateStop, ChronoUnit.HOURS) <= 3) {
            this.setEphemerisStepSize(StepSize.MINUTES);
        } else if(dateStart.until(dateStop, ChronoUnit.DAYS) <= 8) {
            this.setEphemerisStepSize(StepSize.HOURS);
        } else if(dateStart.until(dateStop, ChronoUnit.MONTHS) <= 8) {
            this.setEphemerisStepSize(StepSize.DAYS);
        } else if(dateStart.until(dateStop, ChronoUnit.YEARS) <= 8) {
            this.setEphemerisStepSize(StepSize.MONTHS);
        } else {
            this.setEphemerisStepSize(StepSize.YEARS);
        }

        if(this.getPrimaryBody() instanceof SecondaryBody secBody)
            secBody.setEphemeris(dateStart, dateStop, this.getEphemerisStepSize());

        for(SecondaryBody secondaryBody: this.getPrimaryBody().getSecondaryBodies()) {
            secondaryBody.setEphemeris(dateStart, dateStop, this.getEphemerisStepSize());
        }
    }

    private int convertMonthAbbreviationToNumber(String month) {
        if (month == null || month.length() != 3)
            throw new IllegalArgumentException("Month must be a three letter string");

        return switch (month) {
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

    public static Spacecraft createSpacecraft(String name, String dbID) {
        Spacecraft spacecraft = new Spacecraft(name, dbID);
        SecondaryBody.addToStage(spacecraft);
        return spacecraft;
    }

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

    public void changePrimaryBody(PrimaryBody primaryBody) {
        this.getPrimaryBody().removeSecondaryBody(this);
        this.getPrimaryBody().addSecondaryBody(this);
        this.setPrimaryBody(primaryBody);
    }
}
