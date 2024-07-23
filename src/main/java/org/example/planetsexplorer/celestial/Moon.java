package org.example.planetsexplorer.celestial;

import javafx.scene.Group;
import org.example.planetsexplorer.HorizonSystem;
import org.json.JSONObject;

import static org.example.planetsexplorer.HorizonSystem.pixelKmScale;

public class Moon extends  SecondaryBody {
    public Moon(String name, String dbID, float shapeRadius, PrimaryBody primaryBody, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, dbID, shapeRadius, primaryBody, orbitPeriodYear, siderealDayHr, obliquityToOrbitDeg);
    }

    public static Moon createMoon(Group rootScene3D, Group mainSceneRoot, String moonID, String planetID) throws Exception {
        Planet planet = Planet.getPlanetByName(HorizonSystem.idToName(planetID));
        JSONObject moonJSON = HorizonSystem.getBody(moonID);

        Moon moon = new Moon(HorizonSystem.idToName(moonID),
                moonID,
                moonJSON.getFloat("meanRadKM") / pixelKmScale,
                planet,
                moonJSON.getFloat("siderealOrbitDays"),
                moonJSON.getFloat("siderealDayHr"),
                moonJSON.getFloat("obliquityToOrbitDeg"));

        rootScene3D.getChildren().addAll(moon.getShape());
        rootScene3D.getChildren().add(moon.getPrimaryConnection());

        mainSceneRoot.getChildren().add(moon.getOrbitRing());
        mainSceneRoot.getChildren().add(moon.getGroupUI());
        moon.getGroupUI().toFront();

        return moon;
    }
}
