package org.example.planetsexplorer;

public enum StepSize {
    MINUTES("10m"),
    HOURS("60 min"),
    DAYS("1d"),
    MONTHS("1 mo"),
    YEARS("1 year");


    private final String abbrev;
    StepSize(String abbrev) {
        this.abbrev = abbrev;
    }

    @Override
    public String toString() {
        return this.abbrev;
    }
}
