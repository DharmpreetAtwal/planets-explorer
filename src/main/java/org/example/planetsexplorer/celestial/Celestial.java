package org.example.planetsexplorer.celestial;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.shape.Sphere;
import org.example.planetsexplorer.Main;
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

            Main.updateCameraTranslate(shapePos.getX(), shapePos.getY());
            Main.updateCameraPivot(shapePos);
            PlanetsCamera.updateUIPosition();
            Main.selectedCelestial = this;
        });
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
}
