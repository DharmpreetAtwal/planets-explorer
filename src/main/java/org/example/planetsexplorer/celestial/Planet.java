package org.example.planetsexplorer.celestial;

import java.util.ArrayList;
import org.json.JSONObject;

public class Planet extends SecondaryBody {
    public static ArrayList<Planet> planetArrayList = new ArrayList<>();

    public Planet(String name, float shapeRadius, PrimaryBody primaryBody, float orbitDistance, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, 3, primaryBody, orbitDistance, orbitPeriodYear, siderealDayHr, obliquityToOrbitDeg);
        planetArrayList.add(this);
    }

    public static Planet getPlanetByName(String name) {
        for(Planet planet: planetArrayList) {
            if(planet.getName().equalsIgnoreCase(name))
                return planet;
        }

        return null;
    }
}