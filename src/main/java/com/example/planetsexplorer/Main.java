package com.example.planetsexplorer;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.json.JSONObject;

public class Main extends Application {
    public void start(Stage stage) throws Exception {
        Group root = new Group();

        SubScene subScene = new SubScene(root, 300, 300, true, null);
        subScene.setFill(Color.ALICEBLUE);

        Group sceneRoot = new Group();
        sceneRoot.getChildren().add(subScene);

        Scene mainScene = new Scene(sceneRoot,300, 300);
        PlanetsCamera camera = new PlanetsCamera(mainScene);
        subScene.setCamera(camera.getCamera());

        root.getChildren().addAll(camera.getCamera());

        JSONObject sunInfo = HorizonSystem.executeGet("https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND='10'&OBJ_DATA='YES'&MAKE_EPHEM='NO'");
        assert sunInfo != null;
        Planet sun = new Planet(sunInfo.getFloat("meanRadKM") / 600000,0, 0.0F, 0.0F);

        for(int i=1; i<=9; i++) {
            JSONObject planetJSON = HorizonSystem.executeGet("https://ssd.jpl.nasa.gov/api/horizons.api?format=json&COMMAND='"+ i + "99'&OBJ_DATA='YES'&MAKE_EPHEM='NO'");
            assert planetJSON != null;

            Planet newPlanet = new Planet(
                    planetJSON.getFloat("meanRadKM") / 1000,
                                planetJSON.getFloat("siderealPeriod"),
                    0, 0);
            newPlanet.setOrbitDistance(i*150);
            newPlanet.setPrimaryBody(sun);
        }

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
