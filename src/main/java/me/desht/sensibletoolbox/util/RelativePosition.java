package me.desht.sensibletoolbox.util;

public class RelativePosition {
    private final int front, up, left;

    public RelativePosition(int front, int up, int left) {
        this.front = front;
        this.up = up;
        this.left = left;
    }

    public int getFront() {
        return front;
    }

    public int getUp() {
        return up;
    }

    public int getLeft() {
        return left;
    }
}
