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
        SubScene subScene = new SubScene(root, 300, 300);
        subScene.setFill(Color.ALICEBLUE);
        Group sceneRoot = new Group();
        sceneRoot.getChildren().add(subScene);
        Scene mainScene = new Scene(sceneRoot);
        PlanetsCamera camera = new PlanetsCamera(mainScene);
        subScene.setCamera(camera.getCamera());
        Planet sun = new Planet(10.0F, 0.0F, 0.0F);
        Planet earth = new Planet(5.0F, 0.0F, 0.0F);
        earth.setOrbitDistance(10.0F);
        earth.setPrimaryBody(sun);
        root.getChildren().add(camera.getCamera());

        Planet.planetArrayList.forEach((planet) -> {
            root.getChildren().add(planet.getShape());
        });

        stage.setResizable(false);
        stage.setTitle("DHARM!");
        stage.setScene(mainScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(new String[0]);
    }
}
