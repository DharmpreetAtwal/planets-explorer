package org.example.planetsexplorer;

import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.planetsexplorer.celestial.Celestial;
import org.example.planetsexplorer.celestial.Moon;
import org.example.planetsexplorer.celestial.Planet;
import org.example.planetsexplorer.celestial.Sun;
import org.json.JSONObject;

import java.util.ArrayList;

public class Main extends Application {
    public static PlanetsCamera camera = null;
    private final float orbitDistance = 3;
    public static Celestial selectedCelestial = null;

    public void start(Stage stage) throws Exception {
        Group root = new Group();
        SubScene subScene = new SubScene(root, 300, 300, true, null);
        subScene.setFill(Color.ALICEBLUE);

        Group sceneRoot = new Group();
        sceneRoot.getChildren().add(subScene);

        Scene mainScene = new Scene(sceneRoot,300, 300);

        camera = new PlanetsCamera(mainScene);
        subScene.setCamera(camera.getCamera());
        root.getChildren().add(camera.getCamera());

        JSONObject sunInfo = HorizonSystem.getBody("10");
        assert sunInfo != null;
        Sun sun = new Sun(sunInfo.getFloat("meanRadKM") / 600000);

        for(int i=3; i <= 3; i++) createPlanet(root, sceneRoot, sun, i+"99", "10", i);

        createMoon(root, sceneRoot, "301", "399");
//        for(int i=401; i<=402; i++) createMoon(root, ""+i, "499");
//        for(int i=501; i<=572; i++) createMoon(root, ""+i, "599");
//        for(int i=601; i<= 618; i++) createMoon(root, i +"", "699");
//        for(int i=701; i<=717; i++) createMoon(root, i+"", "799");
//        for(int i=801; i<=814; i++) createMoon(root, i+"", "899");

//        Label label = new Label("Hello");
//        label.setTranslateX(50);
//        label.setTranslateY(50);


//        sun.animateSecondaryBodies();
        stage.setResizable(false);
        stage.setTitle("DHARM!");
        stage.setScene(mainScene);
        stage.show();
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


    private void createPlanet(Group root, Group sceneRoot, Sun sun, String planetID, String centerID, int i) throws Exception {
        ArrayList<JSONObject> ephem = HorizonSystem.getEphemeris(planetID, centerID, "2024-01-01", "2024-12-31", "1 mo");
        JSONObject planetJSON = HorizonSystem.getBody(planetID);
//            float orbitDistance = ephem.get(0).getFloat("qr") / 10000000;

        Planet newPlanet = new Planet(
                HorizonSystem.idToName(planetID),
                planetJSON.getFloat("meanRadKM") / 10000,
                sun,
                orbitDistance * i,
                planetJSON.getFloat("siderealOrbitDays"),
                planetJSON.getFloat("siderealDayHr"),
                planetJSON.getFloat("obliquityToOrbitDeg"));

        newPlanet.setEphemData(ephem);
        newPlanet.setEphemIndex(HorizonSystem.empherisIndex);

        root.getChildren().addAll(newPlanet.getShape(), newPlanet.getOrbitRing());
        sceneRoot.getChildren().add(newPlanet.getGroupUI());
    }

    private void createMoon(Group root, Group sceneRoot, String moonID, String planetID) throws Exception {
        Planet planet = Planet.getPlanetByName(HorizonSystem.idToName(planetID));
        JSONObject moonJSON = HorizonSystem.getBody(moonID);
        ArrayList<JSONObject> ephemMoon = HorizonSystem.getEphemeris(moonID, planetID, "2024-01-01", "2024-12-31", "1 mo");
        //        float orbitDistance = ephemMoon.get(0).getFloat("qr") / 10000;

        Moon moon = new Moon(HorizonSystem.idToName(moonID),
                moonJSON.getFloat("meanRadKM") / 10000,
                planet,
                orbitDistance,
                moonJSON.getFloat("siderealOrbitDays"),
                moonJSON.getFloat("siderealDayHr"),
                moonJSON.getFloat("obliquityToOrbitDeg"));

        moon.setEphemData(ephemMoon);
        moon.setEphemIndex(HorizonSystem.empherisIndex);
        root.getChildren().addAll(moon.getShape(), moon.getOrbitRing());
        sceneRoot.getChildren().add(moon.getGroupUI());
    }

    private void testAnimations(Group root, Sun sun) throws Exception {
        for(int i=1; i <= 9; i++) {
            JSONObject planetJSON = HorizonSystem.getBody(i+"99");
            float orbitDistance = i * 15;
            Planet newPlanet = new Planet(
                    HorizonSystem.idToName(i+"99"),
                    planetJSON.getFloat("meanRadKM") / 10000,
                    sun,
                    orbitDistance * i,
                    planetJSON.getFloat("siderealOrbitDays"),
                    planetJSON.getFloat("siderealDayHr"),
                    planetJSON.getFloat("obliquityToOrbitDeg"));

            root.getChildren().addAll(newPlanet.getShape(), newPlanet.getOrbitRing());
        }
    }


    public static void main(String[] args) {
        launch();
    }

}