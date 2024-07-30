package org.example.planetsexplorer;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.example.planetsexplorer.celestial.Moon;
import org.example.planetsexplorer.celestial.Planet;
import org.example.planetsexplorer.celestial.SecondaryBody;
import org.example.planetsexplorer.celestial.Sun;
import org.json.JSONObject;

import java.util.ArrayList;

public class Main extends Application {
    public void start(Stage stage) throws Exception {
        Group rootScene3D = new Group();
        SubScene scene3D = new SubScene(rootScene3D, 600, 600, true, null);
        scene3D.setFill(Color.ALICEBLUE);

        Group sceneRoot = new Group();
        sceneRoot.getChildren().add(scene3D);

        Scene mainScene = new Scene(sceneRoot,600, 600);
        PlanetsCamera camera = new PlanetsCamera(mainScene);
        scene3D.setCamera(camera.getCamera());
        rootScene3D.getChildren().add(camera.getCamera());

        JSONObject sunInfo = HorizonSystem.getBody("10");
        Sun sun = new Sun(sunInfo.getFloat("meanRadKM") / HorizonSystem.pixelKmScale, "10");
        rootScene3D.getChildren().add(sun.getShape());
        sceneRoot.getChildren().add(sun.getGroupUI());

        startGetThreads(rootScene3D, sceneRoot, sun);

        stage.setResizable(false);
        stage.setTitle("Planet Explorer!");
        stage.setScene(mainScene);
        stage.show();

        PlanetViewer.initializePlanetViewer();
    }

    private void startGetThreads(Group rootScene3D, Group uiGroup, Sun sun) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<SecondaryBody>> futures = new ArrayList<>();

        for(int i=399; i <= 799; i=i+100) {
            final String planetID = i + "";
            Callable<SecondaryBody> task = () -> Planet.createPlanet(rootScene3D, uiGroup, sun, planetID);
            futures.add(executor.submit(task));
        }

        Callable<SecondaryBody> moonTask = () -> Moon.createMoon(rootScene3D, uiGroup, "301", "399");
        futures.add(executor.submit(moonTask));

        for(int i=401; i<=402; i++) {
            final String moonID = i + "";
            Callable<SecondaryBody> task = () -> Moon.createMoon(rootScene3D, uiGroup, moonID, "499");
            futures.add(executor.submit(task));
        }

//        for(int i=501; i<=572; i++) {
//            final String moonID = i + "";
//            Callable<SecondaryBody> task = () -> Moon.createMoon(rootScene3D, uiGroup, moonID, "599");
//            futures.add(executor.submit(task));
//        }

//        for(int i=601; i<= 609; i++) {
//            final String moonID = i + "";
//            Callable<SecondaryBody> task = () -> Moon.createMoon(rootScene3D, uiGroup, moonID, "699");
//            futures.add(executor.submit(task));
//        }

        for(int i=701; i<=717; i++) {
            final String moonID = i + "";
            Callable<SecondaryBody> task = () -> Moon.createMoon(rootScene3D, uiGroup, moonID, "799");
            futures.add(executor.submit(task));
        }
//
//        for(int i=801; i<=814; i++) {
//            final String moonID = i + "";
//            Callable<SecondaryBody> task = () -> Moon.createMoon(rootScene3D, uiGroup, moonID, "899");
//            futures.add(executor.submit(task));
//        }

        for (Future<SecondaryBody> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
    }

    public static void main(String[] args) {
        launch();
    }

}