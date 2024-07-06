package org.example.planetsexplorer.celestial;

public class Moon extends  SecondaryBody {
    public Moon(String name, float sphereRadius, PrimaryBody primaryBody, float distance, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, sphereRadius, primaryBody, distance, orbitPeriodYear, siderealDayHr, obliquityToOrbitDeg);
    }
}
