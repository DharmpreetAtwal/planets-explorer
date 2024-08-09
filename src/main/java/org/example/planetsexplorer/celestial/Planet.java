package org.example.planetsexplorer.celestial;

import org.example.planetsexplorer.HorizonSystem;
import org.example.planetsexplorer.Main;
import org.json.JSONObject;

import java.util.ArrayList;

public class Planet extends SecondaryBody {
    public static ArrayList<Planet> planetArrayList = new ArrayList<>();

    public Planet(String name, String dbID, float shapeRadius, PrimaryBody primaryBody, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, dbID, shapeRadius, primaryBody, orbitPeriodYear, siderealDayHr, obliquityToOrbitDeg);
        this.initializeStartStop(orbitPeriodYear);
        planetArrayList.add(this);
    }

    public static Planet createPlanet(String planetID) {
        JSONObject planetJSON;
        try {
            planetJSON = HorizonSystem.getBody(planetID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Planet newPlanet = new Planet(
                HorizonSystem.idNameMap.get(planetID),
                planetID,
                planetJSON.getFloat("meanRadKM") / HorizonSystem.pixelKmScale,
                Main.sun,
                planetJSON.getFloat("siderealOrbitDays"),
                planetJSON.getFloat("siderealDayHr"),
                planetJSON.getFloat("obliquityToOrbitDeg"));

        SecondaryBody.addToStage(newPlanet);
        return newPlanet;
    }

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

    public static Planet getPlanetByName(String name) {
        for(Planet planet: planetArrayList) {
            if(planet.getName().equalsIgnoreCase(name))
                return planet;
        }

        return null;
    }
}