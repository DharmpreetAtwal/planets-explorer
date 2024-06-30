package com.example.planetsexplorer;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.json.JSONObject;

public class Main extends Application {
    public static PlanetsCamera camera = null;

    public void start(Stage stage) throws Exception {
        Group root = new Group();
        SubScene subScene = new SubScene(root, 300, 300, true, null);
        subScene.setFill(Color.ALICEBLUE);

        Group sceneRoot = new Group();
        sceneRoot.getChildren().add(subScene);
        Scene mainScene = new Scene(sceneRoot,300, 300);

        camera = new PlanetsCamera(mainScene);
        subScene.setCamera(camera.getCamera());

        JSONObject sunInfo = HorizonSystem.getBody("10", true, false);
        assert sunInfo != null;
        Planet sun = new Planet(sunInfo.getFloat("meanRadKM") / 600000,
                0,0,0,
                0.0F, null);

        for(int i=1; i<=9; i++) {
            JSONObject planetJSON = HorizonSystem.getBody(i+"99", true, false);
            assert planetJSON != null;

            Planet newPlanet = new Planet(
                    planetJSON.getFloat("meanRadKM") / 10000,
                    planetJSON.getFloat("siderealOrbitDays"),
                    planetJSON.getFloat("siderealDayHr"),
                    planetJSON.getFloat("obliquityToOrbitDeg"),
                    i*15,
                    sun);

            root.getChildren().addAll(newPlanet.getShape(), newPlanet.getOrbitRing());
        }

        root.getChildren().add(camera.getCamera());

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
