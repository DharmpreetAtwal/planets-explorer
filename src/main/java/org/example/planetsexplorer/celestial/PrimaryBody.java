package org.example.planetsexplorer.celestial;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;

public class PrimaryBody extends Celestial {
    private final ArrayList<SecondaryBody> secondaryBodies = new ArrayList<>();

    public PrimaryBody(String name, float sphereRadius) {
        super(name, sphereRadius);
    }

    public void animateSecondaryBodies() {
        this.secondaryBodies.forEach(body ->{
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(body.getOrbitRotation().angleProperty(), 0)),
                    new KeyFrame(Duration.seconds(body.getOrbitPeriodYear()), new KeyValue(body.getOrbitRotation().angleProperty(), 360)));

            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        });
    }

    public void addSecondaryBody(SecondaryBody secondaryBody) {
        this.secondaryBodies.add(secondaryBody);
        secondaryBody.getShape().setTranslateX(this.getShape().getTranslateX());
        secondaryBody.getShape().setTranslateY(this.getShape().getTranslateY());
        secondaryBody.getShape().setTranslateZ(this.getShape().getTranslateZ() + (double)secondaryBody.getOrbitDistance());
    }
}
