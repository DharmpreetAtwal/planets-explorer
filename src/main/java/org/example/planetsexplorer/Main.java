package org.example.planetsexplorer;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.util.ArrayList;

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

        JSONObject sunInfo = HorizonSystem.getBody("10");
        assert sunInfo != null;
        Planet sun = new Planet("Sun", sunInfo.getFloat("meanRadKM") / 600000,
                0,0,0,
                0.0F, null);

        for(int i=3; i <= 3; i++) {
            ArrayList<JSONObject> ephem = HorizonSystem.getEphemeris(i+"99", "10", "2024-01-01", "2024-12-31", "1 mo");
            JSONObject planetJSON = HorizonSystem.getBody(i+"99");
            float orbitDistance = ephem.get(0).getFloat("qr") / 10000000;

            Planet newPlanet = new Planet(
                    HorizonSystem.idToName(i+"99"),
                    planetJSON.getFloat("meanRadKM") / 10000,
                    planetJSON.getFloat("siderealOrbitDays"),
                    planetJSON.getFloat("siderealDayHr"),
                    planetJSON.getFloat("obliquityToOrbitDeg"),
                    orbitDistance,
                    sun);


            newPlanet.setEphemData(ephem);
            newPlanet.setEphemIndex(HorizonSystem.empherisIndex);

            root.getChildren().addAll(newPlanet.getShape(), newPlanet.getOrbitRing());
        }

        root.getChildren().add(camera.getCamera());

//        sun.animateSecondaryBodies();
        stage.setResizable(false);
        stage.setTitle("DHARM!");
        stage.setScene(mainScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}