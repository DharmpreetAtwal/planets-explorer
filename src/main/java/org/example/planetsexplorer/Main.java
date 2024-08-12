package org.example.planetsexplorer;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.example.planetsexplorer.celestial.Sun;
import org.json.JSONObject;

public class Main extends Application {
    public static final Group rootScene3D = new Group();
    public static final Group sceneRoot = new Group();
    public static Sun sun;

    public void start(Stage stage) throws Exception {
        HorizonSystem.initializeNameIDLookup();
        SubScene scene3D = new SubScene(rootScene3D, 600, 600, true, null);
        scene3D.setFill(Color.ALICEBLUE);
        sceneRoot.getChildren().add(scene3D);

        Scene mainScene = new Scene(sceneRoot,600, 600);
        PlanetsCamera camera = new PlanetsCamera(mainScene);
        scene3D.setCamera(camera.getCamera());
        rootScene3D.getChildren().add(camera.getCamera());

        JSONObject sunInfo = HorizonSystem.getBody("10");
        assert sunInfo != null;
        sun = new Sun(sunInfo.getFloat("meanRadKM") / HorizonSystem.pixelKmScale, "10");
        rootScene3D.getChildren().add(sun.getShape());
        sceneRoot.getChildren().add(sun.getGroupUI());

        stage.setResizable(false);
        stage.setTitle("Planet Explorer!");
        stage.setScene(mainScene);
        stage.show();

        PlanetViewer.initializePlanetViewer();
    }

    public static void main(String[] args) {
        launch();
    }

}