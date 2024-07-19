package org.example.planetsexplorer.celestial;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.shape.Sphere;
import org.example.planetsexplorer.PlanetViewer;
import org.example.planetsexplorer.PlanetsCamera;

import java.util.ArrayList;

public class Celestial {
    public static ArrayList<Celestial> celestialArrayList = new ArrayList<>();
    private final String name;
    private final String dbID;
    private final Sphere shape;
    private final Group groupUI = new Group();
    private final Label labelName = new Label();

    public Celestial(String name, String dbID, float shapeRadius) {
        this.name = name;
        this.dbID = dbID;
        this.shape = new Sphere(shapeRadius, 2);

        this.labelName.setText(name + " " + dbID);
        this.labelName.setStyle("-fx-background-color: white; -fx-border-color: black;");
        this.groupUI.getChildren().add(this.labelName);
        this.groupUI.setOpacity(0.5);
        initializeUIMouseEvents();

        celestialArrayList.add(this);
    }

    private void initializeUIMouseEvents() {
        this.labelName.setOnMouseClicked(e ->{
            Point3D shapePos = this.shape.localToScene(Point3D.ZERO);

            PlanetsCamera.updateTranslate(shapePos.getX(), shapePos.getY());
            PlanetsCamera.updatePivot(shapePos);

            PlanetViewer.setSelectedCelestial(this);
            PlanetsCamera.updateEphemeris(false);
            PlanetsCamera.updateCameraUI();
        });
    }

    public Point2D getScreenCoordinates() {
        Point3D screenPoint = this.shape.localToScene(Point3D.ZERO, true);
        return new Point2D(screenPoint.getX(), screenPoint.getY());
    }

    public Point3D getSceneCoordinates() {
        return this.shape.localToScene(Point3D.ZERO);
    }

    public String getName() {
        return name;
    }

    public Sphere getShape() {
        return shape;
    }

    public Group getGroupUI() {
        return groupUI;
    }

    public String getDbID() {
        return dbID;
    }
}
