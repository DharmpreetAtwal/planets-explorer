package org.example.planetsexplorer.celestial;

import java.util.ArrayList;

public class PrimaryBody extends Celestial {
    private final ArrayList<SecondaryBody> secondaryBodies = new ArrayList<>();

    public PrimaryBody(String name, String dbID, float sphereRadius) {
        super(name, dbID, sphereRadius);
    }

    public void addSecondaryBody(SecondaryBody secondaryBody) {
        this.secondaryBodies.add(secondaryBody);
        secondaryBody.getShape().setTranslateX(this.getShape().getTranslateX());
        secondaryBody.getShape().setTranslateY(this.getShape().getTranslateY());
        secondaryBody.getShape().setTranslateZ(this.getShape().getTranslateZ() + (double)secondaryBody.getOrbitDistance());
    }

    public ArrayList<SecondaryBody> getSecondaryBodies() {
        return secondaryBodies;
    }
}
