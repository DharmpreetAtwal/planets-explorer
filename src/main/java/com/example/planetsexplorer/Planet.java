package com.example.planetsexplorer;

import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class Planet {
    public static ArrayList<Planet> planetArrayList = new ArrayList();
    private Sphere shape;
    private Planet primaryBody = null;
    private float orbitDistance = 0.0F;
    private final ArrayList<Planet> secondaryBodies = new ArrayList();

    public Planet(float shapeRadius, float translateX, float translateY) {
        this.shape = new Sphere((double)shapeRadius, 5);
        this.shape.setMaterial(new PhongMaterial(Color.ORANGE));
        this.shape.setTranslateX((double)translateX);
        this.shape.setTranslateY((double)translateY);
        planetArrayList.add(this);
    }

    public void setOrbitDistance(float distance) {
        this.orbitDistance = distance;
    }

    public void setPrimaryBody(Planet planet) {
        this.primaryBody = planet;
        planet.addSecondaryBody(this);
        this.shape.setTranslateX(this.primaryBody.shape.getTranslateX() + (double)this.orbitDistance);
        this.shape.setTranslateY(this.primaryBody.shape.getTranslateY());
        this.shape.setTranslateZ(this.primaryBody.shape.getTranslateZ());
    }

    private void addSecondaryBody(Planet secondaryBody) {
        this.secondaryBodies.add(secondaryBody);
        secondaryBody.shape.setTranslateX(this.shape.getTranslateX() + (double)secondaryBody.orbitDistance);
        secondaryBody.shape.setTranslateY(this.shape.getTranslateY());
        secondaryBody.shape.setTranslateZ(this.shape.getTranslateZ());
    }

    public Sphere getShape() {
        return this.shape;
    }
}