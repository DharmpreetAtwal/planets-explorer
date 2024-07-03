package org.example.planetsexplorer.celestial;

import javafx.scene.shape.Sphere;

public class Celestial {
    private final String name;
    private final float shapeRadius;
    private Sphere shape;

    public Celestial(String name, float shapeRadius) {
        this.name = name;
        this.shapeRadius = shapeRadius;
        this.shape = new Sphere(shapeRadius, 2);
    }

    public String getName() {
        return name;
    }

    public Sphere getShape() {
        return shape;
    }

    public void setShape(Sphere shape) {
        this.shape = shape;
    }

    public float getShapeRadius() {
        return shapeRadius;
    }
}
