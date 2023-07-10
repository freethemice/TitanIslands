package com.firesoftitan.play.titanbox.islands.enums;

public enum MoveThresholdEnum {
    BLOCKS_7(7); // Threshold for considering a "move"

    private final int distance;
    MoveThresholdEnum(int distance) { this.distance = distance; }

    public int getDistance() {
        return distance;
    }
}
