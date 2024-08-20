package org.example.planetsexplorer.celestial;

import org.example.planetsexplorer.HorizonSystem;
import org.example.planetsexplorer.Main;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A {@code Planet} is a special type of {@link SecondaryBody} that orbits the {@link Sun}.
 * This class is functionally identical to {@code SecondaryBody}. It's main purpose is to
 * allow a {@code Planet} to be searched for by name, or deleted by ID.
 *
 * @author Dharmpreet Atwal
 */
public class Planet extends SecondaryBody {
    /**
     * A list of all {@code Planet} constructed
     */
    public static ArrayList<Planet> planetArrayList = new ArrayList<>();

    /**
     * A constructor that takes all the required fields to construct a {@code SecondaryBody}
     * (except for {@code primaryBody}) and passes them on to the super constructor, and adds
     * this body to {@code planetArrayList}.
     * @param name                The unique title.
     * @param dbID                The unique database id.
     * @param shapeRadius         The radius of the body's shape.
     * @param orbitPeriodYear     The time in years it takes to complete one whole orbit.
     * @param siderealDayHr       The time in years it takes to spin 360° around the central axis
     * @param obliquityToOrbitDeg The rotational tilt of the body.
     * @see Planet#planetArrayList
     */
    private Planet(String name, String dbID, float shapeRadius, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, dbID, shapeRadius, Main.sun, orbitPeriodYear, siderealDayHr, obliquityToOrbitDeg);
        this.initializeStartStop();
        planetArrayList.add(this);
    }

    /**
     * Gets the physical parameters of the planet, calls the private constructor, and adds
     * the planet to the stage.
     * @param planetID The ID of the planet to be added.
     */
    public static void createPlanet(String planetID) {
        JSONObject planetJSON;
        try {
            planetJSON = HorizonSystem.getBody(planetID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Planet newPlanet = new Planet(
                HorizonSystem.idToName(planetID),
                planetID,
                planetJSON.getFloat("meanRadKM") / HorizonSystem.pixelKmScale,
                planetJSON.getFloat("siderealOrbitDays"),
                planetJSON.getFloat("siderealDayHr"),
                planetJSON.getFloat("obliquityToOrbitDeg"));

        SecondaryBody.addToStage(newPlanet);
    }

    /**
     * Deletes a {@code Planet} from the Main scene and removes it from {@code planetArrayList}
     * @param planetID The database ID of the planet to remove.
     */
    public static void deletePlanet(String planetID) {
        Planet foundPlanet = null;
        for(Planet planet: planetArrayList)
            if(planet.getDbID().equals(planetID))
                foundPlanet = planet;

        if(foundPlanet != null) {
            SecondaryBody.removeFromStage(foundPlanet);
            planetArrayList.remove(foundPlanet);
        } else {
            System.err.println("No Planet found: " + planetID);
        }
    }

    /**
     * Returns a {@code Planet} by name.
     * @param name The name of the Planet to search for
     * @return The Planet if it exists, else null
     */
    public static Planet getPlanetByName(String name) {
        for(Planet planet: planetArrayList) {
            if(planet.getName().equalsIgnoreCase(name))
                return planet;
        }

        return null;
    }
}