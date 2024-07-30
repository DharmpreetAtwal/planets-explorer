package org.example.planetsexplorer.celestial;

import javafx.scene.Group;
import org.example.planetsexplorer.HorizonSystem;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.planetsexplorer.HorizonSystem.pixelKmScale;

public class Moon extends  SecondaryBody {
    public static final List<String[]> moonInfo = new ArrayList<>();

    public Moon(String name, String dbID, float shapeRadius, PrimaryBody primaryBody, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, dbID, shapeRadius, primaryBody, orbitPeriodYear, siderealDayHr, obliquityToOrbitDeg);
    }

    public static Moon createMoon(Group rootScene3D, Group mainSceneRoot, String moonID, String planetID) throws Exception {
        Planet planet = Planet.getPlanetByName(HorizonSystem.idToName(planetID));
        JSONObject moonJSON = HorizonSystem.getBody(moonID);

        Moon moon = new Moon(HorizonSystem.idToName(moonID),
                moonID,
                moonJSON.getFloat("meanRadKM") / pixelKmScale,
                planet,
                moonJSON.getFloat("siderealOrbitDays"),
                moonJSON.getFloat("siderealDayHr"),
                moonJSON.getFloat("obliquityToOrbitDeg"));

        SecondaryBody.addToStage(moon, rootScene3D, mainSceneRoot);
        return moon;
    }

    private static void initializeMoonInfo() {
        try {
            List<String> infoTxt = Files.readAllLines(Paths.get("src/main/java/org/example/planetsexplorer/celestial/moonInfo.txt"));
            for(String line: infoTxt) {
                String[] element  = line.split("\\s+");

                // Data cleaning must be done, rows either have both name and desig, or missing one
                // Any row missing both is discarded
                String[] moon = new String[5];
                if(element.length == 13) {
                    float radius;
                    try {
                        radius = Float.parseFloat(element[11]) / 2;
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    moon[0] = element[0];
                    moon[1] = element[1];
                    moon[2] = element[2].substring(2);
                    moon[3] = element[9];
                    moon[4] = String.valueOf(radius);
                    moonInfo.add(moon);
                } else if(element.length == 12) {
                    // If element[1] contains numbers, it's a designation
                    // If element[1] contains no numbers, it's a name
                    Pattern numbers = Pattern.compile("[0-9]");
                    Matcher numbersMatcher = numbers.matcher(element[1]);

                    float radius;
                    try {
                        radius = Float.parseFloat(element[10]) / 2;
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    moon[0] = element[0];
                    if(!numbersMatcher.find()) { // If element[1] is a NAME
                        moon[1] = element[1];
                        moon[2] = "";
                    } else { // If element[1] is a DESIGNATION
                        moon[1] = "";
                        moon[2] = element[1].substring(2);
                    }

                    moon[3] = element[8];
                    moon[4] = String.valueOf(radius);
                    moonInfo.add(moon);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getRadiusKM(String id) {
        if(moonInfo.isEmpty()) initializeMoonInfo();
        String radiusKM = "0";
        String name = HorizonSystem.idToName(id);
        String designation = HorizonSystem.idToDesignation(id);

        for(String[] moon: moonInfo) {
            // While looking for row, first check name, if no name, check designation
            if(moon[1].isEmpty()) {
                if(moon[2].equals(designation)) {
                    radiusKM = moon[4];
                    break;
                }
            } else {
                if(moon[1].equals(name)) {
                    radiusKM = moon[4];
                    break;
                }
            }
        }

        return radiusKM;
    }

    public static String getSiderealOrbitDays(String id) {
        if(moonInfo.isEmpty()) initializeMoonInfo();
        String siderealOrbit = "0";
        String name = HorizonSystem.idToName(id);
        String designation = HorizonSystem.idToDesignation(id);

        for(String[] moon: moonInfo) {
            // While looking for row, first check name, if no name, check designation
            if(moon[1].isEmpty()) {
                if(moon[2].equals(designation)) {
                    siderealOrbit = moon[3];
                    break;
                }
            } else {
                if(moon[1].equals(name)) {
                    siderealOrbit = moon[3];
                    break;
                }
            }
        }

        return siderealOrbit;
    }
}
