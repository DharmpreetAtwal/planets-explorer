package com.example.planetsexplorer;

import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    public void start(Stage stage) throws IOException {
        Group root = new Group();

        SubScene subScene = new SubScene(root, 300, 300, true, null);
        subScene.setFill(Color.ALICEBLUE);

        Group sceneRoot = new Group();
        sceneRoot.getChildren().add(subScene);

        Scene mainScene = new Scene(sceneRoot,300, 300);
        PlanetsCamera camera = new PlanetsCamera(mainScene);
        subScene.setCamera(camera.getCamera());

        Planet sun = new Planet(10.0F, 0.0F, 0.0F);
        Planet earth = new Planet(5.0F, 0.0F, 0.0F);
        earth.setOrbitDistance(50.0F);
        earth.setPrimaryBody(sun);

        Planet mars = new Planet(2, 0, 0);
        mars.setOrbitDistance(30);
        mars.setPrimaryBody(sun);

        root.getChildren().addAll(camera.getCamera());

        Planet.planetArrayList.forEach((planet) -> {
            root.getChildren().add(planet.getShape());
            if(planet.getOrbitRing() != null) {
                root.getChildren().add(planet.getOrbitRing());
            }
        });

        sun.animateSecondaryBodies();

        stage.setResizable(false);
        stage.setTitle("DHARM!");
        stage.setScene(mainScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
