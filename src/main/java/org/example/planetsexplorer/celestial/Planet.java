package org.example.planetsexplorer.celestial;

import java.util.ArrayList;
import org.json.JSONObject;

public class Planet extends SecondaryBody {
    public static ArrayList<Planet> planetArrayList = new ArrayList<>();

    public Planet(String name, float shapeRadius, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg, float orbitDistance, Planet primaryBody) {
        super(name, shapeRadius, primaryBody, orbitDistance, orbitPeriodYear, siderealDayHr, obliquityToOrbitDeg);
        planetArrayList.add(this);
    }

    public void setEphemIndex(int index) {
        JSONObject data = this.getEphemData().get(index);
        this.setEphemerisPosition(data.getFloat("qr") / 10000000, data.getFloat("ma"));
    }

    private void setEphemerisPosition(float orbitDistance, float meanAnon) {
        float x = (float) Math.cos(Math.toRadians(meanAnon)) * orbitDistance;
        float y = (float) Math.sin(Math.toRadians(meanAnon)) * orbitDistance;

        this.getShape().setTranslateX(x);
        this.getShape().setTranslateY(y);
    }

}