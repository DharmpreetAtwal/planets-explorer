package org.example.planetsexplorer.celestial;

import java.util.ArrayList;

/**
 * A {@code PrimaryBody} is any {@code Celestial} that has another {@code Celestial}
 * orbiting about it. A Celestial that orbits another Celestial is called a
 * {@link SecondaryBody}.
 *
 * @author Dharmpreet Atwal
 * @see Celestial
 * @see SecondaryBody
 */
public class PrimaryBody extends Celestial {
    /**
     * The list of {@code SecondaryBody} orbiting this {@code PrimaryBody}
     */
    private final ArrayList<SecondaryBody> secondaryBodies = new ArrayList<>();

    /**
     * Constructs a {@code PrimaryBody} with an identical set of fields to {@code Celestial}
     * @param name The unique title.
     * @param dbID The unique database id.
     * @param shapeRadius The radius of the body's shape.
     */
    public PrimaryBody(String name, String dbID, float shapeRadius) {
        super(name, dbID, shapeRadius);
    }

    /**
     * Adds a {@code SecondaryBody} to this {@code PrimaryBody}
     * @param secondaryBody The body to be added
     */
    public void addSecondaryBody(SecondaryBody secondaryBody) {
        this.secondaryBodies.add(secondaryBody);
        secondaryBody.getShape().setTranslateX(this.getShape().getTranslateX());
        secondaryBody.getShape().setTranslateY(this.getShape().getTranslateY());
        secondaryBody.getShape().setTranslateZ(this.getShape().getTranslateZ() + (double)secondaryBody.getOrbitDistance());
    }

    /**
     * Remove a {@code SecondaryBody} from this {@code PrimaryBody}.
     *
     * <p> Trivial method that exists to provides access to {@code ArrayList.remove()}.
     * Preserves the information hiding of {@code secondaryBodies}.
     * @param removeBody The body to be removed
     */
    public void removeSecondaryBody(SecondaryBody removeBody) {
        this.secondaryBodies.remove(removeBody);
    }

    /**
     * Returns an Array of all the {@code SecondaryBody} that orbit this {@code PrimaryBody}.
     *
     * <p> Trivial method that exists to provide a Collection to iterate over. Preserves
     * information hiding of {@code secondaryBodies}.
     * @return An array that contains
     */
    public SecondaryBody[] getSecondaryBodies() {
        return secondaryBodies.toArray(SecondaryBody[]::new);
    }
}
