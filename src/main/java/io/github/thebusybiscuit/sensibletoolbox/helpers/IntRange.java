package io.github.thebusybiscuit.sensibletoolbox.helpers;

public class IntRange {
    int minimumInteger;
    int maximumInteger;

    public IntRange(int x) {
        this.minimumInteger = x;
        this.maximumInteger = x;
    }
    public IntRange(int min, int max) {
        this.minimumInteger = min;
        this.maximumInteger = max;
    }
    public boolean containsInteger(int i) {
        return i <= maximumInteger && i >= minimumInteger;
    }

    public int getMinimumInteger() {
        return minimumInteger;
    }

    public void setMinimumInteger(int minimumInteger) {
        this.minimumInteger = minimumInteger;
    }

    public int getMaximumInteger() {
        return maximumInteger;
    }

    public void setMaximumInteger(int maximumInteger) {
        this.maximumInteger = maximumInteger;
    }
}
