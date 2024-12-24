package org.example.planetsexplorer.celestial;

import org.json.JSONObject;

import static org.example.planetsexplorer.HorizonSystem.pixelKmScale;

public class EphemerisCoordinateFacade {
    private final JSONObject ephemerisCoordinate = new JSONObject();

    public EphemerisCoordinateFacade(String x, String y, String z, String vx, String vy, String vz) {
        ephemerisCoordinate.put("x", Float.parseFloat(x));
        ephemerisCoordinate.put("y", Float.parseFloat(y));
        ephemerisCoordinate.put("z", Float.parseFloat(z));
        ephemerisCoordinate.put("vx", Float.parseFloat(vx));
        ephemerisCoordinate.put("vy", Float.parseFloat(vy));
        ephemerisCoordinate.put("vz", Float.parseFloat(vz));
    }

    public float getX() {
        return ephemerisCoordinate.getFloat("x") / pixelKmScale;
    }

    public float getY() {
        return ephemerisCoordinate.getFloat("y") / pixelKmScale;
    }

    public float getZ() {
        return ephemerisCoordinate.getFloat("z") / pixelKmScale;
    }

    public float getVx() {
        return ephemerisCoordinate.getFloat("vx");
    }

    public float getVy() {
        return ephemerisCoordinate.getFloat("vy");
    }

    public float getVz() {
        return ephemerisCoordinate.getFloat("vz");
    }
}
