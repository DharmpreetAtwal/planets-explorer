package org.example.planetsexplorer.celestial;

import javafx.scene.Group;
import org.example.planetsexplorer.HorizonSystem;
import org.json.JSONObject;

import static org.example.planetsexplorer.Main.pixelKmScale;

public class Moon extends  SecondaryBody {
    public Moon(String name, String dbID, float shapeRadius, PrimaryBody primaryBody, float orbitPeriodYear, float siderealDayHr, float obliquityToOrbitDeg) {
        super(name, dbID, shapeRadius, primaryBody, orbitPeriodYear, siderealDayHr, obliquityToOrbitDeg);
    }

    public static Moon createMoon(Group root, Group sceneRoot, String moonID, String planetID) throws Exception {
        Planet planet = Planet.getPlanetByName(HorizonSystem.idToName(planetID));
        JSONObject moonJSON = HorizonSystem.getBody(moonID);

        Moon moon = new Moon(HorizonSystem.idToName(moonID),
                moonID,
                moonJSON.getFloat("meanRadKM") / pixelKmScale,
                planet,
                moonJSON.getFloat("siderealOrbitDays"),
                moonJSON.getFloat("siderealDayHr"),
                moonJSON.getFloat("obliquityToOrbitDeg"));
//        moon.setEphemIndex(HorizonSystem.empherisIndex);

        root.getChildren().addAll(moon.getShape());
        sceneRoot.getChildren().add(moon.getOrbitRing());
        sceneRoot.getChildren().add(moon.getGroupUI());
        moon.getGroupUI().toFront();

        return moon;
    }
}
