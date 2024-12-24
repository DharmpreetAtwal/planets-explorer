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

/**
 * A {@code Moon} is a special {@link SecondaryBody} that orbits a {@link Planet}.
 * This class adds extra functionality to {@code SecondaryBody} and fills in the
 * incomplete gaps in {@link HorizonSystem}. This class contains static lookup tables
 * that allows {@code HorizonSystem} to assign a radius or orbit period to a Moon it
 * lacks complete data on.
 */
public class Moon extends  SecondaryBody {
    /**
     * A list of all {@code Moon} constructed
     */
    private static final ArrayList<Moon> moonArrayList = new ArrayList<>(30);

    /**
     * A lookup table for a moon's radius
     */
    private static final HashMap<String, String> idRadiusMap = new HashMap<>(30);

    /**
     * A lookup table for a moon's orbit period
     */
    private static final HashMap<String, String> idOrbitDaysMap = new HashMap<>(30);

    /**
     * Construct a Moon, where the primaryBody can only be of type {@code Planet}
     * @param name The unique title.
     * @param dbID The unique database id.
     * @param shapeRadius The radius of the body's shape.
     * @param planet The {@code Planet} this body will rotate around.
     * @param orbitPeriodYear The time in years it takes to complete one whole orbit.
     * @param siderealDayHr The time in years it takes to spin 360° around the central axis
     * @param obliquityToOrbitDeg The rotational tilt of the body.
     */
    private Moon(String name, String dbID, float shapeRadius, Planet planet, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, dbID, shapeRadius, planet, orbitPeriodYear, siderealDayHr, obliquityToOrbitDeg);
        this.initializeStartStop();
        moonArrayList.add(this);
    }

    /**
     * Creates a {@code Moon} by first getting its {@code Planet}, then passing the
     * {@code Planet} to the constructor, and then adding it to the scene.
     * @param moonID The database ID of the {@code Moon}
     * @param planetID The database ID of the {@code Planet}
     */
    public static void createMoon(String moonID, String planetID)  {
        Planet planet = Planet.getPlanetByName(HorizonSystem.idToName(planetID));
        CelestialInfoFacade moonInfo = HorizonSystem.getBody(moonID);
        assert moonInfo != null;
        Moon moon = new Moon(HorizonSystem.idToName(moonID),
                moonID,
                moonInfo.getMeanRadKM(),
                planet,
                moonInfo.getSiderealOrbitDays(),
                moonInfo.getSiderealDayHr(),
                moonInfo.getObliquityToOrbitDeg());

        SecondaryBody.addToStage(moon);
    }

    /**
     * Deletes a {@code Moon} by searching for it, then deleting it from the stage
     * and list of moons.
     * @param moonID The database ID of the {@code Moon}
     * @see Moon#moonArrayList
     */
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

    /**
     * Initializes the lookup tables of {@code Moon}. This class reads through the file
     * {@code planetsexplorer/celestial/moonInfo.txt} to determine the radius and orbit
     * periods of any {@code Moon} with incomplete data in {@code HorizonSystem}.
     *
     * <p> This method is to be called only once in {@code HorizonSystem}.
     *
     * @see Moon#idRadiusMap
     * @see Moon#idOrbitDaysMap
     * @see HorizonSystem#initializeLookupTables()
     */
    public static void initializeMoonInfo() {
        try {
            List<String> infoTxt = Files.readAllLines(Paths.get("src/main/java/org/example/planetsexplorer/celestial/moonInfo.txt"));
            for(String line: infoTxt) {
                String[] row  = line.split("\\s+");

                // Data cleaning must be done, rows either have both name and desig, or missing one
                if(row.length == 13) {
                    // If row contains both name and designation, it will have 13 items in it
                    float radius;
                    try { radius = Float.parseFloat(row[11]) / 2; }
                    catch (NumberFormatException e) { continue; }

                    String id = HorizonSystem.nameToID(row[1]);
                    idOrbitDaysMap.put(id, row[9]);
                    idRadiusMap.put(id, String.valueOf(radius));
                } else if(row.length == 12) {
                    // If row contains only 12 items, it either has name or designation missing
                    // If element[1] contains numbers, it's a designation
                    // If element[1] contains no numbers, it's a name
                    Pattern numbers = Pattern.compile("[0-9]");
                    Matcher numbersMatcher = numbers.matcher(row[1]);

                    String id;
                    // If element[1] is a NAME
                    if(!numbersMatcher.find()) { id = HorizonSystem.nameToID(row[1]); }
                    // If element[1] is a DESIGNATION
                    else { id = HorizonSystem.designationToId(row[1].substring(2)); }
                    idOrbitDaysMap.put(id, row[8]);

                    float radius;
                    try { radius = Float.parseFloat(row[10]) / 2; }
                    catch (NumberFormatException e) { continue; }
                    idRadiusMap.put(id, String.valueOf(radius));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the radius of a {@code Moon} given its database ID.
     * @param id The ID of the {@code Moon}
     * @return The radius of the {@code Moon} in String format.
     */
    public static String idToRadius(String id) {
        return idRadiusMap.get(id);
    }

    /**
     * Return the orbit period in days of a {@code Moon} given its database ID.
     * @param id The ID of the {@code Moon}
     * @return The orbit period in days of the {@code Moon} in String format.
     */
    public static String idToOrbitDays(String id) {
        return idOrbitDaysMap.get(id);
    }
}
