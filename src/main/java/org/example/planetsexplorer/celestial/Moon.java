package org.example.planetsexplorer.celestial;

import org.example.planetsexplorer.HorizonSystem;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.planetsexplorer.HorizonSystem.pixelKmScale;

public class Moon extends  SecondaryBody {
    public static final ArrayList<Moon> moonArrayList = new ArrayList<>(30);
    public static final HashMap<String, String> idRadiusMap = new HashMap<>(30);
    public static final HashMap<String, String> idOrbitDaysMap = new HashMap<>(30);

    public Moon(String name, String dbID, float shapeRadius, PrimaryBody primaryBody, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, dbID, shapeRadius, primaryBody, orbitPeriodYear, siderealDayHr, obliquityToOrbitDeg);
        this.initializeStartStop(orbitPeriodYear);
        moonArrayList.add(this);
    }

    public static Moon createMoon(String moonID, String planetID)  {
        Planet planet = Planet.getPlanetByName(HorizonSystem.idToName(planetID));
        JSONObject moonJSON = HorizonSystem.getBody(moonID);

        Moon moon = new Moon(HorizonSystem.idToName(moonID),
                moonID,
                moonJSON.getFloat("meanRadKM") / pixelKmScale,
                planet,
                moonJSON.getFloat("siderealOrbitDays"),
                moonJSON.getFloat("siderealDayHr"),
                moonJSON.getFloat("obliquityToOrbitDeg"));

        SecondaryBody.addToStage(moon);
        return moon;
    }

    public static void deleteMoon(String moonID) {
        Moon foundMoon = null;
        for(Moon moon: moonArrayList)
            if(moon.getDbID().equals(moonID))
                foundMoon = moon;

        if(foundMoon != null) {
            SecondaryBody.removeFromStage(foundMoon);
            moonArrayList.remove(foundMoon);
        } else {
            System.err.println("No moon found: " + moonID);
        }
    }

    public static void initializeMoonInfo() {
        try {
            List<String> infoTxt = Files.readAllLines(Paths.get("src/main/java/org/example/planetsexplorer/celestial/moonInfo.txt"));
            for(String line: infoTxt) {
                String[] element  = line.split("\\s+");

                // Data cleaning must be done, rows either have both name and desig, or missing one
                if(element.length == 13) {
                    float radius;
                    try { radius = Float.parseFloat(element[11]) / 2; }
                    catch (NumberFormatException e) { continue; }

                    String id = HorizonSystem.nameToID(element[1]);
                    idOrbitDaysMap.put(id, element[9]);
                    idRadiusMap.put(id, String.valueOf(radius));
                } else if(element.length == 12) {
                    // If element[1] contains numbers, it's a designation
                    // If element[1] contains no numbers, it's a name
                    Pattern numbers = Pattern.compile("[0-9]");
                    Matcher numbersMatcher = numbers.matcher(element[1]);

                    float radius;
                    try { radius = Float.parseFloat(element[10]) / 2; }
                    catch (NumberFormatException e) { continue; }

                    // If element[1] is a NAME
                    String id;
                    if(!numbersMatcher.find()) { id = HorizonSystem.nameToID(element[1]); }
                    // If element[1] is a DESIGNATION
                    else { id = HorizonSystem.designationToId(element[1].substring(2)); }

                    idOrbitDaysMap.put(id, element[8]);
                    idRadiusMap.put(id, String.valueOf(radius));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
