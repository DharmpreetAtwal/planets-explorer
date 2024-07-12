package org.example.planetsexplorer;

import javafx.application.Application;
import javafx.geometry.Point3D;
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
    public static PlanetsCamera camera = null;
    public static int pixelKmScale = 100;

    public void start(Stage stage) throws Exception {
        Group rootScene3D = new Group();
        SubScene scene3D = new SubScene(rootScene3D, 600, 600, true, null);
        scene3D.setFill(Color.ALICEBLUE);

        Group uiGroup = new Group();
        Group mainSceneRoot = new Group();
        mainSceneRoot.getChildren().addAll(scene3D, uiGroup);

        // Main scene is what record keyboard input. must be passed into PlanetsCamera
        Scene mainScene = new Scene(mainSceneRoot,600, 600);
        camera = new PlanetsCamera(mainScene);
        scene3D.setCamera(camera.getCamera());
        rootScene3D.getChildren().add(camera.getCamera());

        JSONObject sunInfo = HorizonSystem.getBody("10");
        assert sunInfo != null;
        Sun sun = new Sun(sunInfo.getFloat("meanRadKM") / 600000, "10");

        startGetThreads(rootScene3D, uiGroup, sun);

        stage.setResizable(false);
        stage.setTitle("DHARM!");
        stage.setScene(mainScene);
        stage.show();

        PlanetViewer pv = new PlanetViewer();
    }

    private void startGetThreads(Group rootScene3D, Group uiGroup, Sun sun) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<Future<SecondaryBody>> futures = new ArrayList<>();

        for(int i=399; i <= 399; i=i+100) {
            final String planetID = i + "";
            Callable<SecondaryBody> task = () -> createPlanet(rootScene3D, uiGroup, sun, planetID, "10");
            futures.add(executor.submit(task));
        }

        Callable<SecondaryBody> moonTask = () -> createMoon(rootScene3D, uiGroup, "301", "399");
        futures.add(executor.submit(moonTask));

//        for(int i=401; i<=402; i++) {
//            final String moonID = i + "";
//            Callable<SecondaryBody> task = () -> createMoon(rootScene3D, uiGroup, moonID, "499");
//            futures.add(executor.submit(task));
//        }

//        for(int i=501; i<=572; i++) {
//            final String moonID = i + "";
//            Callable<SecondaryBody> task = () -> createMoon(rootScene3D, uiGroup, moonID, "599");
//            futures.add(executor.submit(task));
//        }

//        for(int i=601; i<= 609; i++) {
//            final String moonID = i + "";
//            Callable<SecondaryBody> task = () -> createMoon(rootScene3D, uiGroup, moonID, "699");
//            futures.add(executor.submit(task));
//        }
//
//        for(int i=701; i<=717; i++) {
//            final String moonID = i + "";
//            Callable<SecondaryBody> task = () -> createMoon(rootScene3D, uiGroup, moonID, "799");
//            futures.add(executor.submit(task));
//        }
//
//        for(int i=801; i<=814; i++) {
//            final String moonID = i + "";
//            Callable<SecondaryBody> task = () -> createMoon(rootScene3D, uiGroup, moonID, "899");
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


    public static void updateCameraTranslate(double x, double y) {
        Main.camera.getTranslate().setX(x);
        Main.camera.getTranslate().setY(y);
    }

    public static void updateCameraPivot(Point3D pivot) {
        Main.camera.getRotateX().setPivotX(pivot.getX());
        Main.camera.getRotateX().setPivotY(pivot.getY());
        Main.camera.getRotateX().setPivotZ(pivot.getZ());

        Main.camera.getRotateY().setPivotX(pivot.getX());
        Main.camera.getRotateY().setPivotY(pivot.getY());
        Main.camera.getRotateY().setPivotZ(pivot.getZ());

        Main.camera.getRotateZ().setPivotX(pivot.getX());
        Main.camera.getRotateZ().setPivotY(pivot.getY());
        Main.camera.getRotateZ().setPivotZ(pivot.getZ());
    }

    private Planet createPlanet(Group rootScene3D, Group mainSceneRoot, Sun sun, String planetID, String centerID) throws Exception {
        JSONObject planetJSON = HorizonSystem.getBody(planetID);

        Planet newPlanet = new Planet(
                HorizonSystem.idToName(planetID),
                planetID,
                planetJSON.getFloat("meanRadKM") / pixelKmScale,
                sun,
                planetJSON.getFloat("siderealOrbitDays"),
                planetJSON.getFloat("siderealDayHr"),
                planetJSON.getFloat("obliquityToOrbitDeg"));
        newPlanet.setEphemIndex(HorizonSystem.empherisIndex);

        rootScene3D.getChildren().add(newPlanet.getShape());
        mainSceneRoot.getChildren().add(newPlanet.getOrbitRing());
        mainSceneRoot.getChildren().add(newPlanet.getGroupUI());
        newPlanet.getGroupUI().toFront();

        return newPlanet;
    }

    private Moon createMoon(Group root, Group sceneRoot, String moonID, String planetID) throws Exception {
        Planet planet = Planet.getPlanetByName(HorizonSystem.idToName(planetID));
        JSONObject moonJSON = HorizonSystem.getBody(moonID);

        Moon moon = new Moon(HorizonSystem.idToName(moonID),
                moonID,
                moonJSON.getFloat("meanRadKM") / pixelKmScale,
                planet,
                moonJSON.getFloat("siderealOrbitDays"),
                moonJSON.getFloat("siderealDayHr"),
                moonJSON.getFloat("obliquityToOrbitDeg"));
        moon.setEphemIndex(HorizonSystem.empherisIndex);

        root.getChildren().addAll(moon.getShape());
        sceneRoot.getChildren().add(moon.getOrbitRing());
        sceneRoot.getChildren().add(moon.getGroupUI());
        moon.getGroupUI().toFront();

        return moon;
    }

    public static void main(String[] args) {
        launch();
    }

}