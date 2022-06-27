package io.github.thebusybiscuit.sensibletoolbox.utils;

public class IntRange {

    private final int min;
    private final int max;

    public IntRange(int number) {
        this.min = number;
        this.max = number;
    }

    public IntRange(int number1, int number2) {
        super();
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

    public long getMinimumLong() { return min; }
    public long getMaximumLong() { return max; }
    public int getMinimumInt() { return min; }
    public int getMaximumInt() { return max; }

    public boolean containsNumber(Number number) {
        if (number == null) {
            return false;
        }
        return containsInteger(number.intValue());
    }

    public boolean containsInteger(int value) {
        return value >= min && value <= max;
    }

    public int[] toArray() {
        int[] array = new int[max - min + 1];
        for (int i = 0; i < array.length; i++) {
            array[i] = min + i;
        }

        return array;
    }
}