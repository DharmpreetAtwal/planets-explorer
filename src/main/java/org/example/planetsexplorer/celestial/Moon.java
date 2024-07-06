package org.example.planetsexplorer.celestial;

public class Moon extends  SecondaryBody {
    public Moon(String name, String dbID, float shapeRadius, PrimaryBody primaryBody, float distance, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, dbID, shapeRadius, primaryBody, distance, orbitPeriodYear, siderealDayHr, obliquityToOrbitDeg);
    }
}
