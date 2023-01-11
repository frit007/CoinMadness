package org.coin_madness.model;

import javafx.scene.input.KeyCode;

public enum Direction {
    UP,
    RIGHT,
    DOWN,
    LEFT;

     public static Direction fromKeyCode(KeyCode code) {
        switch (code) {
            case UP: return UP;
            case RIGHT: return RIGHT;
            case DOWN: return DOWN;
            case LEFT: return LEFT;
        }
    }
}
