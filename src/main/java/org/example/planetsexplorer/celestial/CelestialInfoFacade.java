package org.example.planetsexplorer.celestial;

import org.json.JSONObject;

import static org.example.planetsexplorer.HorizonSystem.pixelKmScale;

public class CelestialInfoFacade {
    private final JSONObject celestialInfo = new JSONObject();

    public CelestialInfoFacade(float siderealOrbitDays, float siderealDayHr, float obliquityToOrbitDeg, float meanRadKM) {
        celestialInfo.put("siderealOrbitDays", siderealOrbitDays);
        celestialInfo.put("siderealDayHr", siderealDayHr);
        celestialInfo.put("obliquityToOrbitDeg", obliquityToOrbitDeg);
        celestialInfo.put("meanRadKM", meanRadKM);
    }

    public float getSiderealOrbitDays() {
        return celestialInfo.getFloat("siderealOrbitDays");
    }

    public float getSiderealDayHr() {
        return celestialInfo.getFloat("siderealDayHr");
    }

    public float getObliquityToOrbitDeg() {
        return celestialInfo.getFloat("obliquityToOrbitDeg");
    }

    public float getMeanRadKM() {
        return celestialInfo.getFloat("meanRadKM") / pixelKmScale;
    }
}
