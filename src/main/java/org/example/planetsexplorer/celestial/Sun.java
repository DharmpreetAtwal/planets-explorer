package org.example.planetsexplorer.celestial;

import org.example.planetsexplorer.Main;

/**
 * The {@code Sun} sits in the middle of simulation and is automatically constructed
 * at the load of the program. {@code Sun} the only body that is constructed on behalf
 * of the user so that a {@link Planet} already has a defined primaryBody
 * before construction
 * @see Main
 */
public class Sun extends PrimaryBody {
    public Sun(float shapeRadius, String dbID) {
        super("Sun", dbID, shapeRadius);
    }
}
