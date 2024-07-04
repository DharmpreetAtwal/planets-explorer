package org.example.planetsexplorer.celestial;

import javafx.geometry.Point3D;
import org.json.JSONObject;

public class Moon extends  SecondaryBody {
    public Moon(String name, float sphereRadius, PrimaryBody primaryBody, float distance, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, 1, primaryBody, distance, orbitPeriodYear, siderealDayHr, obliquityToOrbitDeg);
    }
}
