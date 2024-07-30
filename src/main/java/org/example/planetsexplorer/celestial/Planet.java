package org.example.planetsexplorer.celestial;

import javafx.scene.Group;
import org.example.planetsexplorer.HorizonSystem;
import org.json.JSONObject;

import java.util.ArrayList;

public class Planet extends SecondaryBody {
    public static ArrayList<Planet> planetArrayList = new ArrayList<>();

    public Planet(String name, String dbID, float shapeRadius, PrimaryBody primaryBody, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, dbID, shapeRadius, primaryBody, orbitPeriodYear, siderealDayHr, obliquityToOrbitDeg);
        planetArrayList.add(this);
    }

    public static Planet createPlanet(Group rootScene3D, Group mainSceneRoot, Sun sun, String planetID) throws Exception {
        JSONObject planetJSON = HorizonSystem.getBody(planetID);

        Planet newPlanet = new Planet(
                HorizonSystem.idToName(planetID),
                planetID,
                planetJSON.getFloat("meanRadKM") / HorizonSystem.pixelKmScale,
                sun,
                planetJSON.getFloat("siderealOrbitDays"),
                planetJSON.getFloat("siderealDayHr"),
                planetJSON.getFloat("obliquityToOrbitDeg"));

        SecondaryBody.addToStage(newPlanet, rootScene3D, mainSceneRoot);
        return newPlanet;
    }

    public static Planet getPlanetByName(String name) {
        for(Planet planet: planetArrayList) {
            if(planet.getName().equalsIgnoreCase(name))
                return planet;
        }

        return null;
    }
}