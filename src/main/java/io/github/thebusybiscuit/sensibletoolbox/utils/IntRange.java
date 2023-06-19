package io.github.thebusybiscuit.sensibletoolbox.utils;

public class IntRange {

    private final int min;
    private final int max;

    public IntRange(int number) {
        this(number, number);
    }

    public IntRange(int number1, int number2) {
        if (number2 < number1) {
            this.min = number2;
            this.max = number1;
        } else {
            this.min = number1;
            this.max = number2;
        }
    }

    ///////////////////////////
    // Accessors
    ///////////////////////////

    public int getMinimumInt() { return min; }
    public int getMaximumInt() { return max; }


    public boolean containsInteger(int value) {
        return value >= min && value <= max;
    }
}